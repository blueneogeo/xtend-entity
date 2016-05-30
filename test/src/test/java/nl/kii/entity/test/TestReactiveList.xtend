package nl.kii.entity.test

import nl.kii.entity.Change
import nl.kii.entity.EntityException
import nl.kii.entity.EntityList
import org.junit.Test

import static nl.kii.entity.ChangeType.*
import static org.junit.Assert.*

import static extension nl.kii.async.stream.StreamExtensions.*
import static extension nl.kii.async.promise.PromiseExtensions.*
import static extension nl.kii.util.JUnitExtensions.*

class TestReactiveList {
	
	@Test
	def void hasCorrectSizeForCopyConstructor() {
		val list = #[ 'hello', 'world' ]
		val entityList = new EntityList(String, list)
		assertEquals(list.size, entityList.size)
	}
	
	
	@Test
	def void supportsChangesOfFullContent() {
		val list = new EntityList<Integer>(Integer)
		
		list.apply(new Change(UPDATE, #[1, 2, 3]))
		assertEquals(3, list.size)
		
		list.apply(new Change(UPDATE, #[3, 4, 5, 6]))
		assertEquals(4, list.size)
		
		list.apply(new Change(CLEAR))
		assertEquals(0, list.size)
		
		try {
			list.apply(new Change(UPDATE, #['a', 'b']))
			fail('should not be able to update an int list with a string list')
		} catch(EntityException e) {
			// good!
		}
		
	}
	
	@Test 
	def void supportsUpdatesOfEntries() {
		val list = new EntityList<String>(String)
		
		try {
			list.apply(new Change(ADD, #[], 1))
			fail('should not be able to put an int value into a string list')
		} catch(EntityException e) {
			// good!
		}
		
		list.apply(new Change(ADD, 'a'))
		list.apply(new Change(ADD, 'b'))
		list.apply(new Change(ADD, 'c'))
		assertArrayEquals(#['a', 'b', 'c'], list)
		
		list.apply(new Change(REMOVE, #['1'], null))
		assertArrayEquals(#['a', 'c'], list)

		list.apply(new Change(UPDATE, #['1'], 'x'))
		assertArrayEquals(#['a', 'x'], list)
	}
	
	@Test
	def void supportsUpdatesWithinEntries() {
		val list = new EntityList(EntityList)
		val sublist = new EntityList(String)
		list.apply(new Change(ADD, sublist))
		
		list.apply(new Change(ADD, 0, 'hello'))
		list.apply(new Change(ADD, 0, 'word'))
		assertArrayEquals(list, #[#['hello', 'word']])

		list.apply(new Change(UPDATE, #['0', '1'], 'world'))
		assertArrayEquals(list, #[#['hello', 'world']])
		
		list.apply(new Change(REMOVE, #['0', '0'], null))
		assertArrayEquals(list, #[#['world']])
		
		try {
			list.apply(new Change(UPDATE, #['0', '0'], 2))
			fail('should not be able to put an int value into the string sublist')
		} catch(EntityException e) {
			// good!
		}
				
		list.apply(new Change(REMOVE, 0, null))
		assertArrayEquals(list, #[])
	}

	@Test
	def void letsListenToChanges() {
		val changes = Change.sink
		val buffered = changes.buffer(100)
		val list = new EntityList<Integer>(int)
		list.onChange [ it >> changes ]
		list.clear
		list.add(1)
		list.add(2)
		list.addAll(#[3, 4]) // produces ADD 3 and ADD 4
		list.remove(1) // removes at 1st index, 2nd place
		changes.complete
		
		buffered.collect.await <=> #[
			new Change(CLEAR),
			new Change(ADD, 1),
			new Change(ADD, 2),
			new Change(ADD, 3),
			new Change(ADD, 4),
			new Change(REMOVE, 1, 2) // removes value 2 from index 1
		]
		list <=> #[1, 3, 4]
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
