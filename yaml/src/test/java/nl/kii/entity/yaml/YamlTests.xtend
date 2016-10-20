package nl.kii.entity.yaml

import java.text.SimpleDateFormat
import nl.kii.entity.User
import nl.kii.util.Minutes
import org.junit.Test

import static org.junit.Assert.*

import static extension nl.kii.entity.yaml.YamlExtensions.*

class YamlTests {
	
	@Test
	def void testYamlDeserializing() {
		val map = '''
			age: 30
			name: "john"
			date_of_birth: 2016-01-01
			best_time: pt20m
			referral:
			  age: 50
		'''.yaml
		
		println('''
			«map»
			
		''')
		
		val user1 = new User(map)
		val user2 = new User [
			age = 30
			name = 'john'
			dateOfBirth = new SimpleDateFormat('yyyy-MM-dd X').parse('2016-01-01 +0000')
			bestTime = new Minutes(20)
			referral = new User [
				age = 50
			]
		]
		println(user1.serialize)
		assertEquals(user2, user1)
	}
	
	@Test
	def void testYamlMapConversion() {
		val yaml = #{
			'age' -> 30,
			'name' -> 'john',
			'referral' -> #{
				'age' -> 40,
				'name' -> 'hans'
			}
		}.yaml
		println(yaml)
		
		assertEquals('''
			name: john
			age: 30
			referral:
			  name: hans
			  age: 40
		'''.toString, yaml)
	}	
	
}

