package nl.kii.entity

import java.util.Map

import static extension nl.kii.util.OptExtensions.*

class EntityExtensions {
	
	def static <T extends Entity> deserialize(Object serialized, Class<T> type) {
		val entity = type.newInstance
		switch serialized {
			Map<String, Object>: entity.deserialize(serialized)
		}
	}

	def static <T extends Entity> create(Class<T> type, Object serialized) {
		serialized.deserialize(type)
	}
	
	def static <K, V> get(Iterable<? extends Pair<K, V>> iterable, K key) {
		iterable.findFirst [ it.key == key ]?.value
	}
	
	def static <T extends Enum<?>> valueOfCaseInsensitive(Class<T> enumType, String value) {
		enumType.enumConstants.findFirst [ name.equalsIgnoreCase(value) ] => [
			if (!defined) throw new IllegalArgumentException('''No enum constant «enumType.canonicalName».«value»''')
		]
	}
		
}