package nl.kii.entity

import java.time.Instant
import nl.kii.entity.annotations.Type

@nl.kii.entity.annotations.Entity
abstract class Animal {
	String color
	int weight
	
	def Animal getMother()
	def Animal getFather()
	
	@nl.kii.entity.annotations.Serializer(Instant) 
	val static s1 = Serializers.instant
}

@nl.kii.entity.annotations.Entity
abstract class Mammal extends Animal {
	@Type String type = 'mammal'
	
	int legs
}

@nl.kii.entity.annotations.Entity
class Dog extends Mammal {
	@Type String type
	
	String breed
	
	Dog mother
	Dog father
	
	boolean hasOwner
	//List<Dog> pack
	
	Instant born
}
