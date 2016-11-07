package nl.kii.entity.serializers

import nl.kii.entity.Serializer

class ClassSerializer implements Serializer<Class<?>, Object> {
	
	override deserialize(Object serialized) {
		Class.forName(serialized.toString)
	}
	
	override serialize(Class<?> original) {
		original.name
	}
	
}
