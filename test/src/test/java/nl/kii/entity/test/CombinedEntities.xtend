package nl.kii.entity.test

import nl.kii.entity.annotations.Entity
import nl.kii.entity.annotations.Require
import org.junit.Test

class TestReversedReference {
	
	@Test
	def void testAllWorks() {
		val a = new EntityA(new EntityB('test'))
		a.onChange [ ]
		a.b.name = 'test'
		a.b = new EntityB('more')
		a.b.name = 'more2'
	}
	
}

@Entity
class EntityA {
	
	@Require EntityB b
	
}

@Entity
class EntityB {
	
	@Require String name
	
}
