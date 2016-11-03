package nl.kii.entity.serializers

import java.time.Instant
import nl.kii.entity.Serializer

import static extension java.lang.Long.*

class InstantMsSerializer implements Serializer<Instant, Object> {
	
	override serialize(Instant original) {
		original.toEpochMilli
	}
	
	override deserialize(Object serialized) {
		switch it:serialized {
			Long: Instant.ofEpochMilli(it)
			default: Instant.ofEpochMilli(toString.parseLong)
		}
	}
}
