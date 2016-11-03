package nl.kii.entity.serializers

import nl.kii.entity.Serializer
import nl.kii.util.Period

import static extension java.lang.Long.*

/** Period <-> milliseconds (long) */
class PeriodMsSerializer implements Serializer<Period, Object> {
	
	override serialize(Period original) {
		original.ms
	}
	
	override deserialize(Object serialized) {
		switch it:serialized {
			Long: new Period(it)
			default: new Period(serialized.toString.parseLong)
		}
	}
}
