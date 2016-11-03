package nl.kii.entity.serializers

import java.time.Duration
import nl.kii.entity.Serializer

import static extension java.lang.Long.*
import static extension nl.kii.util.TemporalExtensions.*

/** Duration <-> milliseconds (long) */
class DurationMsSerializer implements Serializer<Duration, Object> {
	
	override serialize(Duration original) {
		original.ms
	}
	
	override deserialize(Object serialized) {
		switch it:serialized {
			Long: ms
			default:toString.parseLong.ms
		}
	}
}
