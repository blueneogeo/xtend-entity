package nl.kii.entity.processors

import java.util.List
import java.util.Map
import nl.kii.entity.annotations.Require
import nl.kii.entity.processors.AccessorsUtil.GetterOptions.CollectionGetterBehavior
import nl.kii.util.Opt
import nl.kii.util.OptExtensions
import org.eclipse.xtend.lib.annotations.Accessors
import org.eclipse.xtend.lib.annotations.AccessorsProcessor
import org.eclipse.xtend.lib.macro.TransformationContext
import org.eclipse.xtend.lib.macro.declaration.FieldDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableFieldDeclaration
import org.eclipse.xtend.lib.macro.declaration.TypeReference
import org.eclipse.xtend.lib.macro.declaration.Visibility
import org.eclipse.xtend2.lib.StringConcatenationClient

import static extension nl.kii.util.OptExtensions.*

class AccessorsUtil extends AccessorsProcessor.Util {
	val extension TransformationContext context
	//val extension AccessorsProcessor.Util accessorsUtil
		
	new(TransformationContext context) {
		super(context)
		this.context = context
		//this.accessorsUtil = new AccessorsProcessor.Util(context)
	}
	
	def addGetters(MutableClassDeclaration cls, Iterable<? extends FieldDeclaration> fields, (GetterOptions)=>void optionsFn) {
		fields.filter [ shouldAddGetter ].forEach [ field |
			//addGetter(getterType?.toVisibility ?: Visibility.PUBLIC)
			cls.addGetter(field, optionsFn)
		]
	}
	
	def addSetters() {
		throw new UnsupportedOperationException
	}
	
	@Accessors
	static class GetterOptions {
		String getterName
		boolean optionals = false
		CollectionGetterBehavior collections = CollectionGetterBehavior.none
		boolean mutableCollections = false
		
		enum CollectionGetterBehavior {
			returnLazily,
			setLazily,
			none
		}
	}
	
	def addGetter(MutableClassDeclaration cls, FieldDeclaration field, (GetterOptions)=>void optionsFn) {
		val options = new GetterOptions => [ optionsFn.apply(it) ]
		
		cls.addMethod(options.getterName ?: field.getterName) [
			primarySourceElement = field
			
			addAnnotation(Pure.newAnnotationReference)
			docComment = field.docComment
			deprecated = field.deprecated
			
			if (#[ Map, List ].exists [ newTypeReference.isAssignableFrom(field.type) ]) {
				returnType = field.type
				body = '''
					«field.getCollectionGetterBody(options.collections, options.mutableCollections)»
					return «field.simpleName»;
				'''
			} else if (options.optionals) {
				returnType = Opt.newTypeReference(field.type)
				body = '''return «OptExtensions».option(«field.simpleName»);'''
			} else {
				returnType = field.type
				body = '''return «field.simpleName»;'''
			}
		]
	}
	
	def StringConcatenationClient getCollectionGetterBody(FieldDeclaration field, CollectionGetterBehavior collections, boolean mutable) {
		val StringConcatenationClient constructor = 
			if (List.newTypeReference.isAssignableFrom(field.type))
				if (mutable) 
					'''«CollectionLiterals».newLinkedList();'''
				else 
					'''«CollectionLiterals».emptyList();'''
			else if (Map.newTypeReference.isAssignableFrom(field.type)) 
				if (mutable) 
					'''«CollectionLiterals».newHashMap();'''
				else 
					'''«CollectionLiterals».emptyMap();'''
		
		switch collections {
			case setLazily: 
				'''if («field.simpleName» == null) «field.simpleName» = «constructor»;'''
			case returnLazily:
				'''if («field.simpleName» == null) return «constructor»;'''
			default:
				''''''
		}
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
	
	/** Copied from parent, but added docComment, copying of deprecated and fluency support */
	override addSetter(MutableFieldDeclaration field, Visibility visibility) {
		field.validateSetter
		field.declaringType.addMethod(field.setterName) [
			primarySourceElement = field.primarySourceElement
			returnType = field.declaringType.newSelfTypeReference
			val param = addParameter(field.simpleName, field.type.orObject)
			body = '''
				«field.fieldOwner».«field.simpleName» = «param.simpleName»;
				return this;
			'''
			static = field.static
			docComment = field.docComment
			deprecated = field.deprecated
			field.findAnnotation(Require.newTypeReference.type).option => [ require | addAnnotation(require) ]
			it.visibility = visibility
		]
	}
	
	def void addGetter(MutableFieldDeclaration field, String customName, Visibility visibility) {
		field.validateGetter
		field.declaringType.addMethod(customName) [
			primarySourceElement = field.primarySourceElement
			addAnnotation(newAnnotationReference(Pure))
			returnType = field.type.orObject
			body = '''return «field.fieldOwner».«field.simpleName»;'''
			static = field.static
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

