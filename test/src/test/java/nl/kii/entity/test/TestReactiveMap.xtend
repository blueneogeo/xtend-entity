package nl.kii.entity.test

import nl.kii.entity.Change
import nl.kii.entity.EntityException
import nl.kii.entity.EntityList
import nl.kii.entity.EntityMap
import org.junit.Test

import static nl.kii.entity.ChangeType.*
import static org.junit.Assert.*

import static extension nl.kii.async.stream.StreamExtensions.*
import static extension nl.kii.util.JUnitExtensions.*
import static extension nl.kii.async.promise.PromiseExtensions.*

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
	
	@Test
	def void supportsChangesOfFullContent() {
		val map = new EntityMap(String, Integer)
		
		map.apply(new Change(UPDATE, #{'a'->1, 'b'->2, 'c'->3}))
		assertEquals(3, map.size)
		
		map.apply(new Change(UPDATE, #{'a'->3, 'b'->4, 'c'->5, 'd'->6}))
		assertEquals(4, map.size)
		
		map.apply(new Change(CLEAR))
		assertEquals(0, map.size)
		
		try {
			map.apply(new Change(UPDATE, #{'a'->'a', 'b'->'b'}))
			fail('should not be able to update an int map with a string map')
		} catch(EntityException e) {
			// good!
		}
		
	}
	
	@Test 
	def void supportsUpdatesOfEntries() {
		val map = new EntityMap(String, String)
		
		try {
			map.apply(new Change(UPDATE, 'a', 1))
			fail('should not be able to put an int value into a string map')
		} catch(EntityException e) {
			// good!
		}
		
		map.apply(new Change(UPDATE, 'a', 'a'))
		map.apply(new Change(UPDATE, 'b', 'b'))
		map.apply(new Change(UPDATE, 'c', 'c'))
		assertEquals(#{ 'a'->'a', 'b'->'b', 'c'->'c' }, map)

		map.apply(new Change(REMOVE, #['b'], null))
		assertEquals(#{ 'a'->'a', 'c'->'c' }, map)

		map.apply(new Change(UPDATE, #['c'], 'x'))
		assertEquals(#{ 'a'->'a', 'c'->'x' }, map)
	}
	
	@Test
	def void supportsUpdatesWithinEntries() {
		val map = new EntityMap(String, EntityMap)
		val submap = new EntityMap(String, String)
		map.apply(new Change(UPDATE, 'submap', submap))
		
		map.apply(new Change(UPDATE, #['submap', '1'], 'hello'))
		map.apply(new Change(UPDATE, #['submap', '2'], 'word'))
		assertEquals(#{'1'->'hello', '2'->'word'}, submap)

		map.apply(new Change(UPDATE, #['submap', '2'], 'world'))
		assertEquals(#{'1'->'hello', '2'->'world'}, submap)
		
		map.apply(new Change(REMOVE, #['submap', '1'], null))
		assertEquals(#{'2'->'world'}, submap)
		
		try {
			map.apply(new Change(UPDATE, #['submap', '1'], 2))
			fail('should not be able to put an int value into the string submap')
		} catch(EntityException e) {
			// good!
		}
	}

	@Test
	def void letsListenToChanges() {
		val changes = Change.sink
		val buffered = changes.buffer(100)
		val map = new EntityMap(String, int)
		map.onChange [ it >> changes ]
		map.clear
		map.put('a', 1)
		map.put('b', 2)
		map.putAll(#{'c'->3, 'd'->4}) // produces UPDATE 'c'->3 and UPDATE 'd'->4
		map.remove('b')
		changes.complete
		
		buffered.collect.await <=> #[
			new Change(CLEAR),
			new Change(UPDATE, 'a', 1),
			new Change(UPDATE, 'b', 2),
			new Change(UPDATE, 'd', 4), 
			new Change(UPDATE, 'c', 3),
			new Change(REMOVE, 'b', 2)
		]
		map <=> #{'a'->1, 'c'->3, 'd'->4}
	}
	
	@Test
	def void letsListenToChangesWithinEntries() {
		val changes = Change.sink
		val buffered = changes.buffer(100)
		val list = new EntityList(EntityList)
		val sublist = new EntityList(String)
		// pass the list changes into a stream
		list.onChange [ it >> changes ]

		// these will generate changes
		list.add(sublist) // 1
		sublist.add('hello') // 2
		sublist.add('word') // 3
		sublist.set(1, 'world') // 4
		sublist.remove(0) // 5
		list.remove(0) // 6
		changes.complete
		
		// collect what has been streamed and check for the expected changes we caused in the list structure
		buffered.collect.await <=> #[
			new Change(ADD, sublist), // 1
			new Change(ADD, 0, 'hello'), // 2
			new Change(ADD, 0, 'word'), // 3
			new Change(UPDATE, #['0', '1'], 'world'), // 4
			new Change(REMOVE, #['0', '0'], 'hello'), // 5
			new Change(REMOVE, 0, #['world']) // 6
		]
	}
	
}
