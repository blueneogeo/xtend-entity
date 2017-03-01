package nl.kii.entity.processors

import nl.kii.util.AssertionException
import nl.kii.util.OptExtensions
import org.eclipse.xtend.lib.macro.TransformationContext
import org.eclipse.xtend.lib.macro.declaration.FieldDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration

import static extension nl.kii.util.OptExtensions.*

class FieldValidationUtil {
	val extension TransformationContext context
	val extension EntityProcessor.Util entityUtil
		
	new(TransformationContext context) {
		this.context = context
		this.entityUtil = new EntityProcessor.Util(context)
		//this.accessorsUtil = new AccessorsProcessor.Util(context)
	}
	
	def void addValidationMethod(MutableClassDeclaration cls, Iterable<? extends FieldDeclaration> fields, Pair<String, String> typeAssertion) {
		if (typeAssertion.defined) cls.addMethod('validateType') [
			exceptions = AssertionException.newTypeReference
			returnType = cls.newSelfTypeReference
			body = '''
				if (!«typeAssertion.key».equals(«typeAssertion.value»))
					throw new «AssertionException»("Field '«typeAssertion.key»' should be '" + «typeAssertion.value» + "', but was '" + «typeAssertion.key» + "'.");
				
				return this;
			'''
		]
		
		cls.addMethod('validate') [
			exceptions = AssertionException.newTypeReference
			returnType = cls.newSelfTypeReference
			body = '''
				«IF cls.extendsEntity»
					super.validate();
					
				«ENDIF»
				«IF typeAssertion.defined»
					validateType();
					
				«ENDIF»
				«FOR f:fields»
					if (!«OptExtensions».defined(«f.simpleName»)) 
						throw new «AssertionException»("Mandatory field '«f.simpleName»' should be defined.");
						
				«ENDFOR»
				return this;
			'''
		]
	}
}

