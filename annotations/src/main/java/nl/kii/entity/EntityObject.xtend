package nl.kii.entity

import java.util.List
import nl.kii.util.AssertionException

/**
 * An EntityObject has getters and setters, default constructors, mandatory fields and a validation method for those fields.
 */
interface EntityObject extends Cloneable {
	
	/** Returns true if the object data is valid */
	def boolean isValid()

	/** Throws a descriptive exception if the object data is invalid */
	def void validate() throws AssertionException

	/** Returns all fields in this object */
	def List<String> getFields()
	
	/** Returns the value for the field */
	def Object getValue(String field) throws NoSuchFieldException

	/** Sets the value for the field */
	def void setValue(String field, Object value) throws NoSuchFieldException

	/**
	 * Get the type of the property given the passed path, allowing you to bypass Java erasure.
	 * @throws NoSuchFieldException with a reason if the path is not valid.
	 */
	def Class<?> getInstanceType(String... path) throws NoSuchFieldException
	
}
