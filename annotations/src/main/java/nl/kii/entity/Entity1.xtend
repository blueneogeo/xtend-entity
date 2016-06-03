//package nl.kii.entity
//
//import java.util.List
//import java.util.Map
//import nl.kii.util.AssertionException
//
///**
// * An Entity has getters, default constructors, mandatory fields and a validation method for those fields.
// */
//interface Entity extends Cloneable {
//	
//	/** Returns true if the object data is valid */
//	def boolean isValid()
//
//	/** Throws a descriptive exception if the object data is invalid */
//	def void validate() throws AssertionException
//
//	/** Returns all fields in this object */
//	def List<EntityField> getFields()
//	
//	/** Returns the value for the field */
//	def Object get(String fieldName) throws NoSuchFieldException
//	
//	//def Object serialize(String fieldName)
//	//def Object deserialize(String fieldName)
//	
//}
