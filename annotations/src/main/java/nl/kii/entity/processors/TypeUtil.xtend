package nl.kii.entity.processors

import nl.kii.entity.annotations.Type
import org.eclipse.xtend.lib.macro.TransformationContext
import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration
import org.eclipse.xtend.lib.macro.declaration.Visibility

import static extension nl.kii.util.OptExtensions.*
import static extension nl.kii.entity.processors.EntityProcessor.*
import org.eclipse.xtend.lib.macro.declaration.FieldDeclaration

class TypeUtil {
	val extension TransformationContext context
		
	new(TransformationContext context) {
		this.context = context
	}
	
	def void validateTypeAnnotation(MutableClassDeclaration cls) {
		val typeFields = cls.declaredFields.filter [ findAnnotation(Type.newTypeReference.type).defined ]
		
		switch size:typeFields.size {
			case 1: 
				if (typeFields.head.type != string) cls.addError('@Type field must be a String')
			case size > 1: 
				cls.addError('There can only be one field marked with @Type')
		}
	}
	
	def void addTypeAccessors(MutableClassDeclaration cls, FieldDeclaration typeField, String value) {
		cls.addField(TYPE_CONSTANT) [
			primarySourceElement = typeField
			static = true
			final = true
			visibility = Visibility.PUBLIC
			docComment = '''Returns the value declared in the entity's {@code @Type} annotated field '«typeField.simpleName»' '''
			initializer = '''"«value»"'''
			type = string
		]
		
		cls.addMethod(typeField.simpleName) [
			primarySourceElement = typeField
			static = true
			returnType = string
			docComment = '''Returns the value declared in the entity's {@code @Type} annotated field '«typeField.simpleName»' '''
			body = '''return «TYPE_CONSTANT»;'''
		]
	}
}

