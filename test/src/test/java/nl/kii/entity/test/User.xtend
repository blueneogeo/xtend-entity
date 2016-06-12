package nl.kii.entity.test

import java.time.Instant
import java.util.Date
import nl.kii.entity.Serializers
import nl.kii.entity.annotations.Entity
import nl.kii.entity.annotations.Require
import nl.kii.entity.annotations.Serializer
import nl.kii.util.Period

@Entity(casing=underscore)
class User {
	@Require String name
	User parent
	Integer age
	
	Date dateOfBirth
	Date registered
	
	Period bestTime
	//List<String> sports
	Location location
	Membership membership
	
	Long profileId
	
	
	@Serializer(Period) 
	val static s2 = Serializers.period

	@Serializer(Date) 
	val static s1 = Serializers.date('yyyy-MM-dd', 'yyyy_MM_dd')
	
	@Serializer(Instant) 
	val static s3 = Serializers.instant
	
//	val static dateFormat = #[ 'yyyy-MM-dd' ]	
//	val static serializers = #[
//		Period -> Serializers.period,
//		Instant -> Serializers.instant('')
//	]	
}

enum Membership {
	premium, 
	trial,
	free
}

@Entity(optionals=true)
class Location {
	String address
	Integer number
}



@Entity
abstract class Animal {
	String color
	int weight
	
	// Date discovered
}

@Entity
abstract class Mammal extends Animal {
	int legs
}

@Entity
class Dog extends Mammal {
	String breed
}






//@Entity class Thing implements Reactive {
//	String title
//	Date created
//	
//	def void setTitle(String title) {
//		_observers.notify('title', this.title, title)
//		
//		this.title = title
//	}
//	
//	
//	// CODE THAT WILL GET ADDED WHEN SPECIFYING reactive=true
//	
//	def observe((Change)=>void observer) {
//		fields.map [ name ].forEach [ observe(it, observer) ]
//	}
//		
//	def observe(EntityField field, (Change)=>void observer) {
//		observe(field.name, observer)
//	}
//
//	override observe(String field, (Change)=>void observer) {
//		_observers.observe(field, observer)
//	}
//	
//	val protected _observers = new Observers
//}
//
//class Observers {
//	val private Map<String, List<WeakReference<(Change)=>void>>> observers = newLinkedHashMap
//	
//	def <T> notify(String field, T oldValue, T newValue) {
//		val observers = observers.get(field)
//		if (observers.nullOrEmpty || oldValue == newValue) return
//		
//		val change = new Change(field, newValue, oldValue)
//		
//		observers.map [ get ]
//			.filterNull
//			.forEach [ apply(change) ]
//	}
//	
//	def observe(String field, (Change)=>void observer) {
//		val fieldObservers = observers.get(field) ?: newLinkedList
//		fieldObservers.add(new WeakReference(observer))
//		observers.put(field, fieldObservers)
//	}
//}
//
//interface Reactive {
//	def void observe(String field, (Change)=>void observer)	
//}
//
//@Data
//class Change {
//	String field
//	Object newValue
//	Object oldValue
//}
//
//class Consumer {
//	def test() {
//		val thing = new Thing => [
//			
//		]
//		
//		thing.observe(Fields.title) [  ]
//		
//		
//	}
//	
//}






////	val static Map<Pair<? extends Class<?>, ? extends Class<?>>, Serializer<?, ?>> serializers = #{
////		(Date -> String) -> new DateSerializer('yyyy-MM-dd'),
////		(Instant -> String) -> new InstantSerializer('yyyy-MM-dd')
////	}
//
//	val static Map<Class<?>, Serializer<?, Object>> serializers = #{
//		Date -> new DateSerializer('yyyy-MM-dd'),
//		Instant -> new InstantSerializer('yyyy-MM-dd')
//	}
//
////
////	def getFormatter(Date date) {
////		
////	}
//	
//
//	
//	override getFields() {
//		
//	}
//	
//	def serialize() {
//		val serialized = newHashMap => [
//			
//		]
//		
//		if (OptExtensions.defined(title)) {
//			serialized.put('title', title)			
//		}
//
//		if (OptExtensions.defined(created)) {
//			val serializer = serializers.get(Date) as DateSerializer
//			serialized.put('created', serializer.serialize(created))
//		}
//
//	}
//
//	override deserialize(Map<String, Object> serialized) {
//		//super.init(serialized)
//		
//		val title = serialized.get("title")
//		if (title != null) {
//			this.title = title.deserializeString
//		}
//
//		val created = serialized.get("created")
//		if (created != null) {
//			val serializer = serializers.get(Date) as DateSerializer
//			this.created = serializer.deserialize(created)
//		}
//
//		val instant = serialized.get("instant")
//		if (instant != null) {
//			val serializer = serializers.get(Instant) as InstantSerializer
//			this.instant = serializer.deserialize(instant)
//		}
//		
//		val casing = serialized.get("casing")
//		if (casing != null) {
//			this.casing = deserializeCaseFormat(casing)
//		}
//		
//		this
//	}
//	
//	def deserializeInteger(Object serialized) {
//		if (serialized instanceof Integer) serialized
//		else Integer.parseInt(serialized.toString)
//	}
//
//	def deserializeString(Object serialized) {
//		serialized.toString
//	}
//	
//	def deserializeCaseFormat(Object serialized) {
//		if (serialized instanceof Integer) CaseFormat.enumConstants.get(serialized)
//		else CaseFormat.valueOf(casing.toString)
//	}	
//
//}

//class EntitySerializer implements Serializer<nl.kii.entity.Entity, Map<String, Object>> {
//		
//	override serialize(nl.kii.entity.Entity original) {
//		original.serialize
//	}
//	
//	override deserialize(Map<String, Object> serialized) {
//		serialized.deserialize
//	}	
//}


