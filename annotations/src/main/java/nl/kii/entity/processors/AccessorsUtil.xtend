package nl.kii.entity.processors

import nl.kii.util.Opt
import nl.kii.util.OptExtensions
import org.eclipse.xtend.lib.annotations.AccessorsProcessor
import org.eclipse.xtend.lib.macro.TransformationContext
import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableFieldDeclaration

class AccessorsUtil {
	val extension TransformationContext context
	val extension AccessorsProcessor.Util accessorsUtil
		
	new(TransformationContext context) {
		this.context = context
		this.accessorsUtil = new AccessorsProcessor.Util(context)
	}
	
	def addGetters(MutableClassDeclaration cls, Iterable<? extends MutableFieldDeclaration> fields, boolean optionals) {
		fields.filter [ shouldAddGetter ].forEach [ field |
			//addGetter(getterType?.toVisibility ?: Visibility.PUBLIC)
			cls.addGetter(field, optionals)
		]
	}
		
	def addSetters() {
		throw new UnsupportedOperationException
	}
	
	def addGetter(MutableClassDeclaration cls, MutableFieldDeclaration field, boolean optionals) {
		cls.addMethod(field.getterName) [
			addAnnotation(Pure.newAnnotationReference)
			docComment = field.docComment
			deprecated = field.deprecated
			
			if (optionals) {
				returnType = Opt.newTypeReference(field.type)
				body = '''return «OptExtensions.newTypeReference».option(«field.simpleName»);'''
			}
			else {
				returnType = field.type
				body = '''return «field.simpleName»;'''				
			}
		]
	}
}

