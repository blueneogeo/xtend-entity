package nl.kii.entity

import java.time.Duration
import java.util.List
import nl.kii.entity.Serializers
import nl.kii.entity.annotations.Entity
import nl.kii.entity.annotations.Serializer
import nl.kii.entity.annotations.Type

@Entity
abstract class Video {
	@Type String type
	
	List<String> actors
	Duration duration
	
	@Serializer(Duration)
	val static s1 = Serializers.duration
}