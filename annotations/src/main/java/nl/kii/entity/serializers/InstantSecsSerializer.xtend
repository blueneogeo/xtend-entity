package nl.kii.entity.serializers

import java.time.Instant
import nl.kii.entity.Serializer

import static extension java.lang.Long.*
import static extension nl.kii.util.TemporalExtensions.*

class InstantSecsSerializer implements Serializer<Instant, Object> {
	
	override serialize(Instant original) {
		original.toEpochMilli.ms.secs
	}
	
	override deserialize(Object serialized) {
		switch it:serialized {
			Long: Instant.ofEpochMilli(secs.ms)
			default: Instant.ofEpochMilli(toString.parseLong.secs.ms)
		}
	}
}
