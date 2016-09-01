package nl.kii.entity.test

import java.time.Duration
import java.util.List
import nl.kii.entity.Serializers
import nl.kii.entity.annotations.Entity
import nl.kii.entity.annotations.Serializer

@Entity
abstract class Video {
	List<String> actors
	Duration duration
	
	@Serializer(Duration)
	val static s1 = Serializers.duration
}