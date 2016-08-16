package nl.kii.entity.test

import io.vertx.core.json.JsonObject
import java.time.Month
import java.util.Date
import java.util.Map
import nl.kii.entity.Entity
import nl.kii.util.Opt
import org.junit.Test

import static org.junit.Assert.*

import static extension nl.kii.entity.EntityExtensions.*
import static extension nl.kii.entity.csv.CsvExtensions.*
import static extension nl.kii.entity.test.JsonExtensions.*
import static extension nl.kii.entity.yaml.YamlExtensions.*
import static extension nl.kii.util.DateExtensions.*

class EntityTests {
	
	@Test
	def void testEntityImmutability() {
		val user1 = new User [
			age = 20
			name = 'john'
		]
		
		/** user1.age = 30 // won't compile */
			
		val user2 = user1.mutate [
			age = 30
		]
		
		val user3 = user2 >> [
			age = 40
			registered = now
		]
		
		assertEquals('user1 age should be unchanged', 20, user1.age)
		assertEquals('user2 age should have the new value', 30, user2.age)
		assertEquals('mutation should also work with >> operator', 40, user3.age)
		assertEquals('john', user2.name)		
	}
	
	@Test
	def void testOptionals() {
		val location = new Location [
			address = 'Kattenburgerstraat'
			number = 5
		]

		assertTrue(
			'location fields should be optional, since @Entity.optionals=true',
			Opt.isAssignableFrom(location.address.class)
		)
	}

	@Test
	def void testReflection() {
		User.Fields.registered => [
			assertEquals('registered', name)
			assertEquals(Date, type)
		]
	}
	
	@Test
	def void testBasicSerialization() {
		val user = new User [
			age = 30
			name = 'john'
			membership = Membership.free
			profileId = 1234L
		]
		
		assertEquals(
			#{ 
				'name' -> 'john',
				'age' -> 30,
				'membership' -> 'free',
				'profile_id' -> 1234L
			},
			user.serialize
		)
	}

	@Test
	def void testAdvancedSerialization() {
		val date = moment(2016, Month.JANUARY.ordinal, 1)
		
		val user = new User [
			age = 30
			name = 'john'
			registered = date
			bestTime = 6.mins
		]
		
		assertEquals(
			'period serializer should understand the ISO-8601 formatting',
			#{ 
				'name' -> 'john',
				'age' -> 30,
				'registered' -> '2016-01-01',
				'best_time' -> 'PT6M'
			},
			user.serialize
		)
		
		val user2 = new User [
			name = 'john'
			referral = new User [
				name = 'ref'
			]
		]

		assertEquals(
			'nested entity should be serialized as a map',
			#{ 
				'name' -> 'john',
				'referral' -> #{
					'name' -> 'ref'
				}
			},
			user2.serialize
		)
		
		val user3 = new User [
			name = 'john'
			friends = #[
				new User [ name = 'friend1' ],
				new User [ name = 'friend2' ]
			]
		]
		
		assertEquals(
			'nested entity in list should be serialized as a list of maps',
			#{ 
				'name' -> 'john',
				'friends' -> #[
					#{ 'name' -> 'friend1' },
					#{ 'name' -> 'friend2' }
				]
			},
			user3.serialize
		)
	}
	
	@Test
	def void testBasicDeserialization() {
		val user = #{ 
			'name' -> 'john',
			'age' -> 30,
			'membership' -> 'free',
			'profile_id' -> 1234L
		}
		
		assertEquals(
			new User [
				age = 30
				name = 'john'
				membership = Membership.free
				profileId = 1234L
			],
			user.deserialize(User)
		)
	}

	@Test
	def void testAdvancedDeserialization() {
		val date = moment(2016, Month.JANUARY.ordinal, 1)
		
		val user1 = #{ 
			'name' -> 'john',
			'age' -> 30,
			'registered' -> '2016-01-01',
			'best_time' -> 'pt6m'
		}
				
		assertEquals(
			'period serializer should understand the ISO-8601 formatting',
			new User [
				age = 30
				name = 'john'
				registered = date
				bestTime = 6.mins
			],			
			user1.deserialize(User)
		)

		val user2 = #{ 
			'name' -> 'john',
			'registered' -> '2016_01_01'
		}

		assertEquals(
			'other date format should still work, since it is defined like that in the entity',
			new User [
				name = 'john'
				registered = date
			],
			user2.deserialize(User)
		)
	}

	@Test
	def void testJsonDeserializing() {
		val data = newLinkedHashMap('some' -> 'data')
		
		val it = new JsonObject('''
			{
				"age": 30,
				"name": "john",
				"birthday": "2016-01-01",
				"referral": {
					"age": 50
				}
			}
		''').put('json', data)
		
		println(map)
		map.values.forEach[ class ]
		
		println(fullMap)
		
		println(receive(User))
	}

	@Test
	def void testYamlDeserializing() {
		val map = '''
			age: 30
			name: "john"
			birthday: 2016-01-01
			best_time: pt20m
			referral:
				age: 50
		'''.yaml

		val yaml = #{
			'age' -> 30,
			'name' -> 'john',
			'referral' -> #{
				'age' -> 40,
				'name' -> 'hans'
			}
		}.yaml
		
		println('''
			«map»
			
			«yaml»
		''')
		
		val user = new User(map)
		println(user)
	}
	
	@Test
	def void testCsvDeserializing() {
		val csvRecords = '''
			"age","name","date_of_birth","best_time"
			30,"john","2016-01-01","pt20m"
		'''.toString.csv
		
		println('''
			«csvRecords»
		''')
		
		val users = csvRecords.receiveList(User)
		println(users)
	}
	
	@Test
	def void testInheritance() {
		val d1 = new Dog [
			legs = 4
			breed = 'beagle'
			weight = 10_000
		]
		
		assertEquals(4, d1.legs)
		
		assertEquals(
			'fields class and getFields() should inherit',
			#[ Dog.Fields.type, Dog.Fields.born, Dog.Fields.color, Dog.Fields.weight, Dog.Fields.legs, Dog.Fields.breed, Dog.Fields.mother, Dog.Fields.father, Dog.Fields.hasOwner ].sortBy[name],
			d1.fields.sortBy[name]
		)
		
		val d2 = d1 >> [
			breed = 'jack russell'
			weight = 7_000
		]
		
		assertEquals('jack russell', d2.breed)
		assertEquals(7000, d2.weight)
		assertEquals(4, d2.legs)
		
	}
	
	@Test
	def void testCollections() {
		val user1 = new User [ name = 'john' ]
		
		assertEquals(
			'empty list from getter when list is null',
			emptyList,
			user1.purchases
		)

		assertEquals(
			'empty map from getter when map is null',
			emptyMap,
			user1.membershipDurations
		)
		
		assertTrue(
			'mutating map should fail', 
			try { 
				user1.membershipDurations.put(Membership.trial, 7.days) 
				false
			} 
			catch(UnsupportedOperationException e) true
			catch(Exception e) false
		)
		
		assertTrue(
			'mutating list should fail', 
			try { 
				user1.purchases.add(now) 
				false
			} 
			catch(UnsupportedOperationException e) true
			catch(Exception e) false
		)
	}

	@Test
	def void testApplyingConvenienceProcedure() {
		assertEquals(
			someFnUser(new User [ 
				name = 'john'
				age = 30
			]),
			someFnUser [
				name = 'john'
				age = 30
			]
		)
		
		assertEquals(
			someFnDog(new Dog [ 
				breed = 'beagle'
				weight = 10_000
			]),
			someFnDog [
				breed = 'beagle'
				weight = 10_000
			]
		)
	}		
	
	def static someFnUser(User user) { user.serialize }
	def static someFnDog(Dog dog) { dog.serialize }
	
	@Test 
	def void testConvenienceEntitySetter() {
		assertEquals(
			new User [ 
				name = 'john' 
				referral = new User [
					name = 'hans'
				]
			],
			new User [
				name = 'john' 
				referral [
					name = 'hans'
				]
			]
		)
		
		assertEquals(
			new Dog [ 
				breed = 'beagle'
				mother = new Dog [
					color = 'brown'
				]
			],
			new Dog [
				breed = 'beagle'
				mother [
					color = 'brown'
				]
			]
		)
	}			
}

// TODO: move to xtend-vertx
class JsonExtensions {
	
	def static JsonObject json(Entity entity) {
		new JsonObject(entity.serialize as Map<String, Object>)
	}
	
	def static <T extends Entity> create(Class<T> type, JsonObject json) {
		type.create(json.fullMap)
	}
	
	def static <T extends Entity> receive(JsonObject json, Class<T> type) {
		type.create(json)
	}

//	def static <T extends Entity> receive(JsonArray json, Class<T> type) {
//		json.list.receiveList(type)
//	}
	
	def static Map<String, Object> fullMap(JsonObject json) {
		json.fold(newHashMap) [ map, it |
			map.put(key, switch it:value {
					JsonObject: fullMap
					default: it
				}
			)
			map
		]
	}
	
}

