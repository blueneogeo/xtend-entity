package nl.kii.entity.serializers

import java.time.Duration
import nl.kii.entity.Serializer

/** ISO-8601 period formatting. Example: 20 minutes becomes 'PT20M' */
class DurationSerializer implements Serializer<Duration, Object> {
	
	override serialize(Duration original) {
		original.toString
	}
	
	override deserialize(Object serialized) {
		Duration.parse(serialized.toString)
	}	
}
