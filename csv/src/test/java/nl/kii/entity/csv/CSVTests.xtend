package nl.kii.entity.csv

import nl.kii.entity.User
import org.junit.Test

import static extension nl.kii.entity.csv.CSVExtensions.*
import static extension org.junit.Assert.*
import java.text.SimpleDateFormat
import nl.kii.util.Minutes

class CSVTests {
	
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
		
		val user = new User [
			name = 'john'
			age = 30
			dateOfBirth = new SimpleDateFormat('yyyy-MM-dd').parse('2016-01-01')
			bestTime = new Minutes(20)
		]
		
		assertEquals(user, users.head)
	}
	
		
}
