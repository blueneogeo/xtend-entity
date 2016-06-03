package nl.kii.entity.test

import nl.kii.entity.EntityList
import org.junit.Test

import static org.junit.Assert.*

class TestReactiveList {
	
	@Test
	def void hasCorrectSizeForCopyConstructor() {
		val list = #[ 'hello', 'world' ]
		val entityList = new EntityList(String, list)
		assertEquals(list.size, entityList.size)
	}
	
}
