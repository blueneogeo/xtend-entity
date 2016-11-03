package nl.kii.entity

import java.time.Duration
import java.time.Instant
import java.util.Date
import java.util.UUID
import nl.kii.entity.serializers.DateSerializer
import nl.kii.entity.serializers.DirectSerializer
import nl.kii.entity.serializers.DurationMsSerializer
import nl.kii.entity.serializers.DurationSecsSerializer
import nl.kii.entity.serializers.DurationSerializer
import nl.kii.entity.serializers.InstantMsSerializer
import nl.kii.entity.serializers.InstantSerializer
import nl.kii.entity.serializers.PeriodMsSerializer
import nl.kii.entity.serializers.PeriodSerializer
import nl.kii.entity.serializers.UUIDSerializer
import nl.kii.util.Period

import static extension nl.kii.util.IterableExtensions.*

interface Serializer<O, S> {
	def S serialize(O original)
	def O deserialize(S serialized)
}

class Serializers {
	val public static PERIOD_SERIALIZER = new PeriodSerializer
	val public static PERIOD_MS_SERIALIZER = new PeriodMsSerializer
	val public static DURATION_SERIALIZER = new DurationSerializer
	val public static DURATION_SECS_SERIALIZER = new DurationSecsSerializer
	val public static DURATION_MS_SERIALIZER = new DurationMsSerializer
	val public static INSTANT_MS_SERIALIZER = new InstantMsSerializer
	val public static UUID_SERIALIZER = new UUIDSerializer
	val public static DIRECT_SERIALIZER = new DirectSerializer
	
	/** ISO-8601 period formatting */
	def static Serializer<Period, Object> period() {
		PERIOD_SERIALIZER
	}

	/** Period <-> milliseconds formatting */
	def static Serializer<Period, Object> periodMs() {
		PERIOD_MS_SERIALIZER
	}
	
	/** ISO-8601 period formatting */
	def static Serializer<Duration, Object> duration() {
		DURATION_SERIALIZER
	}

	/** Duration <-> milliseconds formatting */
	def static Serializer<Duration, Object> durationMs() {
		DURATION_MS_SERIALIZER
	}
	
	/** Duration <-> seconds formatting */
	def static Serializer<Duration, Object> durationSecs() {
		DURATION_SECS_SERIALIZER
	}
	
	def static Serializer<UUID, Object> uuid() {
		UUID_SERIALIZER
	}

	def static Serializer<Object, Object> direct() {
		DIRECT_SERIALIZER
	}
		
	def static Serializer<Date, Object> date(String... patterns) {
		new MultiSerializer(patterns.map [ new DateSerializer(it) ].list)
	}

	def static Serializer<Instant, Object> instant(String... patterns) {
		new MultiSerializer(patterns.map [ new InstantSerializer(it) ].list)
	}
	
	val public static DEFAULT_INSTANT_FORMAT = 'yyyy-MM-dd HH:mm:ss Z'
	val public static DEFAULT_DATE_FORMAT = 'yyyy-MM-dd HH:mm:ss X'
	
	/** Uses format of 'yyyy-MM-dd HH:mm:ss X' */
	def static Serializer<Date, Object> date() {
		new DateSerializer(DEFAULT_DATE_FORMAT)
	}

	/** Uses format of 'yyyy-MM-dd HH:mm:ss Z' */
	def static Serializer<Instant, Object> instant() {
		new InstantSerializer(DEFAULT_INSTANT_FORMAT)
	}

	/** Uses milliseconds sinds epoch */
	def static Serializer<Instant, Object> instantMs() {
		INSTANT_MS_SERIALIZER
	}
	
}
