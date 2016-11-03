package nl.kii.entity.serializers

import java.util.UUID
import nl.kii.entity.Serializer

class UUIDSerializer implements Serializer<UUID, Object> {
	
	override deserialize(Object serialized) {
		UUID.fromString(serialized.toString)
	}
	
	override serialize(UUID original) {
		original.toString
	}
	
}
