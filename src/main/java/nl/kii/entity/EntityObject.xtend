package nl.kii.entity

import java.util.List

/**
 * An EntityObject has getters and setters, default constructors, mandatory fields and a validation method for those fields.
 */
interface EntityObject extends Cloneable {
	
	/** 
	 * Get the type of the property given the passed path, allowing you to bypass Java erasure.
	 * Throws an EntityException with a reason if the path is not valid.
	 */
	def Class<?> getInstanceType(List<String> path) throws EntityException
	
	/** 
	 * Throws an EntityException with a reason if the object is not valid
	 */
	def void validate() throws EntityException

}
