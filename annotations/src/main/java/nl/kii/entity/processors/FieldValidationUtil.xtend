package nl.kii.entity.processors

import nl.kii.util.AssertionException
import nl.kii.util.OptExtensions
import org.eclipse.xtend.lib.macro.TransformationContext
import org.eclipse.xtend.lib.macro.declaration.FieldDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration

import static extension nl.kii.util.OptExtensions.*

class FieldValidationUtil {
	val extension TransformationContext context
	//val extension AccessorsProcessor.Util accessorsUtil
		
	new(TransformationContext context) {
		this.context = context
		//this.accessorsUtil = new AccessorsProcessor.Util(context)
	}
	
	def addValidationMethod(MutableClassDeclaration cls, Iterable<? extends FieldDeclaration> fields, Pair<? extends FieldDeclaration, String> typeAssertion) {
		cls.addMethod('validate') [
			primarySourceElement = cls
			exceptions = AssertionException.newTypeReference
			body = '''
				«IF typeAssertion.defined»
					if (!«typeAssertion.key.simpleName».equals("«typeAssertion.value»"))
						throw new «AssertionException»("Field '«typeAssertion.key.simpleName»' should be '«typeAssertion.value»', but was '" + «typeAssertion.key.simpleName» + "'.");
				«ENDIF»
				«FOR f:fields»
					if (!«OptExtensions».defined(«f.simpleName»)) 
						throw new «AssertionException»("Mandatory field '«f.simpleName»' should be defined.");
				«ENDFOR»
			'''
		]
	}
}

