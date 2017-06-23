package nl.kii.entity

import java.util.List
import java.util.Map
import nl.kii.util.AssertionException

/**
 * An Entity has getters, default constructors, extendable serialization support, mandatory fields and a validation method for those fields.
 */
interface Entity {
	
//	/** Returns true if the object data is valid */
//	def boolean isValid()
//
	/** Throws a descriptive exception if the object data is invalid. Fluent, should return {@code this}. */
	def Entity validate() throws AssertionException
	
	/** Returns all fields in this object */
	// def static List<EntityField> getEntityFields() { emptyList }
	// def static List<EntityField> fields() { emptyList }
	
	def List<EntityField> getFields()
	
//	/** Returns the value for the field */
//	def Object get(String fieldName) throws NoSuchFieldException
	
	def Entity deserialize(Map<String, ?> serialized)
	def Map<String, Object> serialize()
}
