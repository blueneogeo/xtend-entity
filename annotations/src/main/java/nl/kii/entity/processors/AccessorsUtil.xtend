package nl.kii.entity.processors

import java.util.List
import java.util.Map
import nl.kii.util.Opt
import nl.kii.util.OptExtensions
import org.eclipse.xtend.lib.annotations.AccessorsProcessor
import org.eclipse.xtend.lib.macro.TransformationContext
import org.eclipse.xtend.lib.macro.declaration.FieldDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration

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
}

