package nl.kii.entity.test

import java.time.Instant
import nl.kii.entity.Serializers
import nl.kii.entity.annotations.Entity
import nl.kii.entity.annotations.Serializer
import nl.kii.entity.annotations.Type

@Entity
abstract class Animal {
	String color
	int weight
	
	def Animal getMother()
	def Animal getFather()
	
	@Serializer(Instant) 
	val static s1 = Serializers.instant
}

@Entity
abstract class Mammal extends Animal {
	@Type String type = 'mammal'
	
	int legs
}

@Entity
class Dog extends Mammal {
	@Type String type
	
	String breed
	
	Dog mother
	Dog father
	
	boolean hasOwner
	//List<Dog> pack
	
	Instant born
}
