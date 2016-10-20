package nl.kii.entity

import nl.kii.entity.annotations.Entity
import nl.kii.entity.annotations.Serializer
import java.time.Instant
import nl.kii.entity.Serializers

@Entity
class Movie extends Video {
	double price
	Instant released
	
	@Serializer(Instant)
	val static s2 = Serializers.instant
}