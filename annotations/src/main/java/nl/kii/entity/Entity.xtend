package nl.kii.entity

import java.util.List
import java.util.Map

/**
 * An Entity has getters, default constructors, extendable serialization support, mandatory fields and a validation method for those fields.
 */
interface Entity {
	
//	/** Returns true if the object data is valid */
//	def boolean isValid()
//
//	/** Throws a descriptive exception if the object data is invalid */
//	def void validate() throws AssertionException

	/** Returns all fields in this object */
	def List<EntityField> getFields()
//	
//	/** Returns the value for the field */
//	def Object get(String fieldName) throws NoSuchFieldException
	
	def Entity deserialize(Map<String, ?> serialized)
	def Map<String, ?> serialize()
}

//abstract class Entity2 implements Map<String, Object> {
//	
//	/** Returns all fields in this object */
//	def List<EntityField> getFields()
//	
//	def Entity deserialize(Map<String, ?> serialized)
//	def Map<String, ?> serialize()
//	
//	override clear() {
//		throw new UnsupportedOperationException
//	}
//	
//	override containsKey(Object key) {
//		get(key) != null
//	}
//	
//	override containsValue(Object value) {
//		throw new UnsupportedOperationException("TODO: auto-generated method stub")
//	}
//	
//	override entrySet() {
//		throw new UnsupportedOperationException("TODO: auto-generated method stub")
//	}
//	
//	override get(Object key) {
//		throw new UnsupportedOperationException("TODO: auto-generated method stub")
//	}
//	
//	override isEmpty() {
//		throw new UnsupportedOperationException("TODO: auto-generated method stub")
//	}
//	
//	override keySet() {
//		throw new UnsupportedOperationException("TODO: auto-generated method stub")
//	}
//	
//	override put(String key, Object value) {
//		throw new UnsupportedOperationException("TODO: auto-generated method stub")
//	}
//	
//	override putAll(Map<? extends String, ?> m) {
//		throw new UnsupportedOperationException("TODO: auto-generated method stub")
//	}
//	
//	override remove(Object key) {
//		throw new UnsupportedOperationException("TODO: auto-generated method stub")
//	}
//	
//	override size() {
//		throw new UnsupportedOperationException("TODO: auto-generated method stub")
//	}
//	
//	override values() {
//		throw new UnsupportedOperationException("TODO: auto-generated method stub")
//	}
//	
//}