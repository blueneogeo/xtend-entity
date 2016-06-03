package nl.kii.entity

import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Date
import nl.kii.util.Period

import static extension nl.kii.util.IterableExtensions.*

interface Serializer<O, S> {
	def S serialize(O original)
	def O deserialize(S serialized)
}

class Serializers {
	val public static PERIOD_SERIALIZER = new PeriodSerializer
	
	/** ISO-8601 period formatting */
	def static Serializer<Period, Object> period() {
		PERIOD_SERIALIZER
	}
	
	def static Serializer<Date, Object> date(String... patterns) {
		new MultiSerializer(patterns.map [ new DateSerializer(it) ].list)
	}

	def static Serializer<Instant, Object> instant(String... patterns) {
		new MultiSerializer(patterns.map [ new InstantSerializer(it) ].list)
	}
	
	val static DEFAULT_DATE_FORMAT = 'yyyy-MM-dd HH:mm:ss X'
	
	/** Uses format of 'yyyy-MM-dd HH:mm:ss X' */
	def static Serializer<Date, Object> date() {
		new DateSerializer(DEFAULT_DATE_FORMAT)
	}

	/** Uses format of 'yyyy-MM-dd HH:mm:ss X' */
	def static Serializer<Instant, Object> instant() {
		new InstantSerializer(DEFAULT_DATE_FORMAT)
	}
}


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


class InstantSerializer implements Serializer<Instant, Object> {
	val private DateTimeFormatter formatter
	
	new(String dateFormat) {
		this.formatter = DateTimeFormatter.ofPattern(dateFormat)
	}
	
	override serialize(Instant original) {
		formatter.format(original)
	}
	
	override deserialize(Object serialized) {
		switch it:serialized {
			String: Instant.from(formatter.parse(it))
			Long: Instant.ofEpochMilli(it)
		}
	}	
}

/** ISO-8601 period formatting */
class PeriodSerializer implements Serializer<Period, Object> {
	
	override serialize(Period original) {
		Duration.ofMillis(original.ms).toString
	}
	
	override deserialize(Object serialized) {
		new Period(Duration.parse(serialized.toString).toMillis)
	}	
}


