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
import static extension nl.kii.entity.yaml.YamlExtensions.*
import static extension nl.kii.entity.test.JsonExtensions.*
import static extension nl.kii.util.DateExtensions.*

class EntityTests {
	
	@Test
	def void testEntityImmutability() {
		val user1 = new User [
			age = 20
			name = 'john'
		]
		
//		user1.age = 30 // won't compile
			
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
	def void testCustomizedSerialization() {
		val date = moment(2016, Month.JANUARY.ordinal, 1)
		
		val user = new User [
			age = 30
			name = 'john'
			registered = date
			bestTime = 6.mins
		]
		
		assertEquals(
			#{ 
				'name' -> 'john',
				'age' -> 30,
				'registered' -> '2016-01-01',
				'best_time' -> 'PT6M'
			},
			user.serialize
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
	def void testCustomizedDeserialization() {
		val date = moment(2016, Month.JANUARY.ordinal, 1)
		
		val user1 = #{ 
			'name' -> 'john',
			'age' -> 30,
			'registered' -> '2016-01-01',
			'best_time' -> 'pt6m'
		}
				
		assertEquals(
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
				"parent": {
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
			parent:
				age: 50
		'''.yaml

		val yaml = #{
			'age' -> 30,
			'name' -> 'john',
			'parent' -> #{
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
	
}


class JsonExtensions {
	
	def static JsonObject json(Entity entity) {
		new JsonObject(entity.serialize)
	}
	
	def static <T extends Entity> create(Class<T> type, JsonObject json) {
		type.create(json.fullMap)
	}
	
	def static <T extends Entity> receive(JsonObject json, Class<T> type) {
		type.create(json)
	}
	
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

