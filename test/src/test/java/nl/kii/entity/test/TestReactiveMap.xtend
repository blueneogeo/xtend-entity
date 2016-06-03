package nl.kii.entity.test

import nl.kii.entity.EntityMap
import org.junit.Test

import static org.junit.Assert.*

class TestReactiveMap {
	
	@Test
	def void hasCorrectSizeForCopyConstructor() {
		val map = #{
			'hello' -> 1,
			'world' -> 2
		}
		val entityMap = new EntityMap(String, Integer, map)
		assertEquals(map.size, entityMap.size)
	}
	
}
