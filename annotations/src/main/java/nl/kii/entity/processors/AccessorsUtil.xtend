package nl.kii.entity.processors

import java.util.List
import java.util.Map
import nl.kii.util.Opt
import nl.kii.util.OptExtensions
import org.eclipse.xtend.lib.annotations.AccessorsProcessor
import org.eclipse.xtend.lib.macro.TransformationContext
import org.eclipse.xtend.lib.macro.declaration.FieldDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration
import org.eclipse.xtend.lib.macro.declaration.TypeReference
import org.eclipse.xtend.lib.macro.declaration.MutableFieldDeclaration
import org.eclipse.xtend.lib.macro.declaration.Visibility
import nl.kii.entity.annotations.Require
import static extension nl.kii.util.OptExtensions.*

class AccessorsUtil extends AccessorsProcessor.Util {
	val extension TransformationContext context
	//val extension AccessorsProcessor.Util accessorsUtil
		
	new(TransformationContext context) {
		super(context)
		this.context = context
		//this.accessorsUtil = new AccessorsProcessor.Util(context)
	}
	
	def addGetters(MutableClassDeclaration cls, Iterable<? extends FieldDeclaration> fields, boolean optionals) {
		fields.filter [ shouldAddGetter ].forEach [ field |
			//addGetter(getterType?.toVisibility ?: Visibility.PUBLIC)
			cls.addGetter(field, optionals)
		]
	}
		
	def addSetters() {
		throw new UnsupportedOperationException
	}
	
	def addGetter(MutableClassDeclaration cls, FieldDeclaration field, boolean optionals) {
		cls.addMethod(field.getterName) [
			primarySourceElement = field
			
			addAnnotation(Pure.newAnnotationReference)
			docComment = field.docComment
			deprecated = field.deprecated
			
			if (Map.newTypeReference.isAssignableFrom(field.type)) {
				returnType = field.type
				body = '''
					if («field.simpleName» == null) return «CollectionLiterals».emptyMap();
					return «field.simpleName»;
				'''
			} else if (List.newTypeReference.isAssignableFrom(field.type)) {
				returnType = field.type
				body = '''
					if («field.simpleName» == null) return «CollectionLiterals».emptyList();
					return «field.simpleName»;
				'''
			} else if (optionals) {
				returnType = Opt.newTypeReference(field.type)
				body = '''return «OptExtensions».option(«field.simpleName»);'''
			} else {
				returnType = field.type
				body = '''return «field.simpleName»;'''
			}
		]
	}
	
	def static isGetter(String methodName) {
		methodName.startsWith('get') && methodName.length > 3 && Character.isUpperCase(methodName.charAt(3))
	}
	
	def static isSetter(String methodName) {
		methodName.startsWith('set') && methodName.length > 3 && Character.isUpperCase(methodName.charAt(3))
	}
		
	def static fieldNameFromGetter(String getterName) {
		getterName.substring(3).toFirstLower
	}
	
	/** Copied from parent, with small modification to skip 'is'-setters */	
	override getPossibleGetterNames(FieldDeclaration it) {
			val names = newArrayList
			// common case: a boolean field already starts with 'is'. Allow field name as getter method name
			if (type.orObject.isBooleanType && simpleName.startsWith('is') && simpleName.length>2 && Character.isUpperCase(simpleName.charAt(2))) {
				names += simpleName
			}
			names.addAll(#["get"].map[prefix|prefix + simpleName.toFirstUpper])
			return names
	}
	
	/** Copied from parent, but added docComment and deprecated copying */
	override addSetter(MutableFieldDeclaration field, Visibility visibility) {
		field.validateSetter
		field.declaringType.addMethod(field.setterName) [
			primarySourceElement = field.primarySourceElement
			returnType = primitiveVoid
			val param = addParameter(field.simpleName, field.type.orObject)
			body = '''«field.fieldOwner».«field.simpleName» = «param.simpleName»;'''
			static = field.static
			docComment = field.docComment
			deprecated = field.deprecated
			field.findAnnotation(Require.newTypeReference.type).option => [ require | addAnnotation(require) ]
			it.visibility = visibility
		]
	}
	
	/** Copied from parent */	
	def private fieldOwner(MutableFieldDeclaration it) {
		if(static) declaringType.newTypeReference else "this"
	}
	
	/** Copied from parent */
	def private orObject(TypeReference ref) {
		if (ref === null) object else ref
	}
}

