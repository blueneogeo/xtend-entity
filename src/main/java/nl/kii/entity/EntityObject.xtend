package nl.kii.entity

/**
 * An EntityObject has getters and setters, default constructors, mandatory fields and a validation method for those fields.
 */
interface EntityObject extends Cloneable {
	
	/** Throws an EntityException with a reason if the object is not valid */
	def void validate() throws EntityException

}
