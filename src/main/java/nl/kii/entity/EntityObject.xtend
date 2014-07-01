package nl.kii.entity

/**
 * An EntityObject has getters and setters, default constructors, mandatory fields and a validation method for those fields.
 */
interface EntityObject extends Cloneable {
	
	/** @return true if all fields annotated with @Require have a value */
	def boolean isValid()

}
