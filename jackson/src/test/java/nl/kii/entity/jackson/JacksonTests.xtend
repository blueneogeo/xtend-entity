package nl.kii.entity.jackson

import nl.kii.entity.User
import org.junit.Test

import static org.junit.Assert.*

import static extension nl.kii.entity.jackson.JacksonExtensions.*

class JacksonTests {
	
	@Test
	def void testJacksonDeserializing() {
		val json = '''
			{
				"age": 30,
				"name": "john",
				"interests": [ "cars" ],
				"referral": {
					"age": 50
				}
			}
		'''.toString.map

		val user = new User [
			age = 30
			name = 'john'
			interests = #[ 'cars' ]
			referral = new User [
				age = 50
			]
		]
		
		assertEquals(
			user,
			new User(json)
		)
	}
	
}
