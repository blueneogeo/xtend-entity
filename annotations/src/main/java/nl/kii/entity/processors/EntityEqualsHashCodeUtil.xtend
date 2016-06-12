package nl.kii.entity.processors

import org.eclipse.xtend.lib.annotations.EqualsHashCodeProcessor
import org.eclipse.xtend.lib.macro.TransformationContext
import org.eclipse.xtend.lib.macro.declaration.ClassDeclaration
import org.eclipse.xtend.lib.macro.declaration.FieldDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration

class EntityEqualsHashCodeUtil extends EqualsHashCodeProcessor.Util {
	val extension TransformationContext context
	
	new(TransformationContext context) {
		super(context)
		this.context = context
	}
	
	override void addEquals(MutableClassDeclaration cls, Iterable<? extends FieldDeclaration> includedFields, boolean includeSuper) {
		cls.addMethod('equals') [
			primarySourceElement = cls.primarySourceElement
			returnType = primitiveBoolean
			addAnnotation(newAnnotationReference(Override))
			addAnnotation(newAnnotationReference(Pure))
			addParameter('obj', object)
			body = '''
				if (this == obj)
				  return true;
				if (obj == null)
				  return false;
				if (getClass() != obj.getClass() && getClass().isAssignableFrom(obj.getClass()) && obj.getClass().isAssignableFrom(getClass()))
				  return false;
				«IF includeSuper»
					if (!super.equals(obj))
					  return false;
				«ENDIF»
				«cls.newWildCardSelfTypeReference» other = («cls.newWildCardSelfTypeReference») obj;
				«FOR field : includedFields»
					«field.contributeToEquals»
				«ENDFOR»
				return true;
			'''
		]
	}
	
	def newWildCardSelfTypeReference(ClassDeclaration cls) {
		cls.newTypeReference(cls.typeParameters.map [object.newWildcardTypeReference])
	}
}
