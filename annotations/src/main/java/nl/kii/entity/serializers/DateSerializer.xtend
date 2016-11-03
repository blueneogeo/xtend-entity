package nl.kii.entity.serializers

import java.text.SimpleDateFormat
import java.util.Date
import nl.kii.entity.Serializer

class DateSerializer implements Serializer<Date, Object> {
	val private ThreadLocal<SimpleDateFormat> formatter
	
	new(String dateFormat) {
		this.formatter = new ThreadLocal<SimpleDateFormat> {
			override protected initialValue() {
				new SimpleDateFormat(dateFormat)
			}
		}		
	}
	
	override serialize(Date original) {
		formatter.get.format(original)
	}
	
	override deserialize(Object serialized) {
		switch serialized {
			Date: serialized
			Long: new Date(serialized)
			default: formatter.get.parse(serialized.toString)
		}
	}
}
