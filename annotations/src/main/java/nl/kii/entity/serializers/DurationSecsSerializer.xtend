package nl.kii.entity.serializers

import java.time.Duration
import nl.kii.entity.Serializer

import static extension java.lang.Long.*
import static extension nl.kii.util.TemporalExtensions.*

/** Duration <-> seconds (long) */
class DurationSecsSerializer implements Serializer<Duration, Object> {
	
	override serialize(Duration original) {
		original.secs
	}
	
	override deserialize(Object serialized) {
		switch it:serialized {
			Long: secs
			default:toString.parseLong.secs
		}
	}
}
