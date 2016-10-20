package nl.kii.entity

import java.time.Duration
import nl.kii.entity.Serializers
import nl.kii.entity.annotations.Entity
import nl.kii.entity.annotations.Serializer

@Entity(casing=dot)
class LogConfiguration {
	String logLevel
	Duration appenderRollover
	Integer appenderMaxMb
	
	@Serializer(Duration) 
	val static sd = Serializers.duration
}
