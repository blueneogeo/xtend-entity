package nl.kii.entity.serializers

import java.time.Duration
import nl.kii.entity.Serializer
import nl.kii.util.Period

/** ISO-8601 period formatting. Example: 20 minutes becomes 'PT20M' */
class PeriodSerializer implements Serializer<Period, Object> {
	
	override serialize(Period original) {
		Duration.ofMillis(original.ms).toString
	}
	
	override deserialize(Object serialized) {
		new Period(Duration.parse(serialized.toString).toMillis)
	}	
}
