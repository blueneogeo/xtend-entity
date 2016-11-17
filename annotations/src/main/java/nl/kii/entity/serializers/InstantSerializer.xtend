package nl.kii.entity.serializers

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import nl.kii.entity.Serializer
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor

@FinalFieldsConstructor
class InstantSerializer implements Serializer<Instant, Object> {
	val DateTimeFormatter formatter
	
	new(String dateFormat) {
		this(dateFormat, Locale.US)
	}

	new(String dateFormat, Locale locale) {
		this.formatter = DateTimeFormatter.ofPattern(dateFormat, locale).withZone(ZoneOffset.UTC)
	}
	
	override serialize(Instant original) {
		formatter.format(original)
	}
	
	override deserialize(Object serialized) {
		switch it:serialized {
			String: Instant.from(formatter.parse(it))
			Long: Instant.ofEpochMilli(it)
			default: Instant.from(formatter.parse(toString))
		}
	}	
}
