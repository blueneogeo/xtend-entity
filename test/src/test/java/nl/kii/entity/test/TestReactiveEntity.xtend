package nl.kii.entity.test

import nl.kii.entity.Change
import nl.kii.entity.EntityList
import nl.kii.util.AssertionException
import org.junit.Test

import static nl.kii.entity.ChangeType.*
import static org.junit.Assert.*

import static extension nl.kii.stream.StreamExtensions.*

class TestReactiveEntity {

	@Test
	def void testGetType() {
		assertEquals(RelationShip, RelationShip.getType(#[]))
		assertEquals(User, RelationShip.getType(#['user']))
		assertEquals(User, RelationShip.getType(#['friends', 'chris']))
		assertEquals(String, RelationShip.getType(#['friends', '0', 'name']))
	}

	@Test
	def void testGetTypeOfEntityList() {
		val list = new EntityList<User>(User)
		assertEquals(EntityList, list.getInstanceType(#[]))
		assertEquals(User, list.getInstanceType(#['test']))
		assertEquals(String, list.getInstanceType(#['test', 'name']))
	}

	@Test
	def void testEntityChangeListening() {
		val changes = Change.stream

		val u = new User('Christian')
		val stop = u.onChange[it >> changes]

		u.name = 'John' // creates change
		u.name = 'Mary' // creates change

		u.apply(new Change(UPDATE, #['name'], 'Floris')) // quiet update
		assertEquals('Floris', u.name)

		val u2 = new User('Jane')
		u.apply(new Change(UPDATE, #[], u2)) // quiet update
		assertEquals('Jane', u.name)

		u.parent = new User('Marid') // creates change
		u.parent.name = 'Madrid' // creates change

		stop.apply

		u.name = 'no longer works'

		u.validate

		u.name = null
		
		try {
			u.validate
			fail('validate should throw error')
		} catch(AssertionException e) {
			// expected
		}

		// quiet update. may not update the change made earlier to Simone as well!
		u.apply(new Change(UPDATE, #['parent', 'name'], 'Simone'))
		assertEquals('Simone', u.parent.name)

		changes.finish
		changes.collect.then [
			assertArrayEquals(
				#[
					new Change(UPDATE, #['name'], 'John'),
					new Change(UPDATE, #['name'], 'Mary'),
					new Change(UPDATE, #['parent'], new User('Marid')),
					new Change(UPDATE, #['parent', 'name'], 'Madrid')
				], it)
		]

	}

	@Test
	def void testChangeEquals() {
		val c1 = new Change(UPDATE, #['name'], 'John')
		val c2 = new Change(UPDATE, #['name'], 'John')
		assertTrue(c1 == c2)
		val c3 = new Change(UPDATE)
		assertFalse(c1 == c3)
	}

	@Test
	def void testReactiveEquals() {
		val u1 = new User('Christian')
		val u2 = new User('Christian')
		assertTrue(u1 == u2)
		u1.parent = new User('Gerard')
		assertFalse(u1 == u2)
		u2.parent = new User('Gerard')
		assertTrue(u1 == u2)
		u2.parent = u1.parent
		assertTrue(u1 == u2)
		
		// maps and lists should also equal
		val r1 = new RelationShip(u1)
		val r2 = new RelationShip(u2)
		r1.friends // creates a new list automatically
		r2.relations // creates a new map automatically
		// even with the empty list and map, the equals should work
		assertTrue(r1 == r2)
	}

	@Test
	def void testMap() {
		val u1 = new User('Christian')
		val u2 = new User('Eli')
		val r = new RelationShip(u1)
		r.relations = newHashMap
		r.relations.put('friend1', u1)
		r.relations.put('friend2', u2)
		u1.name = 'Christiaan'
		u1.name = null
		r.relations.clear
		r.relations = #{'f1' -> u1, 'f2' -> u2}
		u1.apply(new Change(UPDATE, #['name'], 'Chris'))

		// println(r)
		u1.parent = u2

		// u2.parent = u1 // cycling references cause many problems!
	}
	
	@Test
	def void testAutoCreateListsAndMaps() {
		val r = new RelationShip
		// this should not give errors, since lists and maps are generated on demand!
		r.friends += new User('Chris')
		assertEquals(#[new User('Chris')], r.friends)
		r.relations.put('Chris', new User('Chris'))
		assertEquals(#{'Chris'->new User('Chris')}, r.relations)
		// also notice how we can use equals, based purely on content!
	}
	
	@Test
	def void testNullSettingWhenPublishing() {
		val it = new User('Chris')
		println(it)
		assertEquals(name, 'Chris')
			
		onChange [ 
			switch path.head {
				case 'name': {
					println('name value has been changed to: ' + value)
					assertEquals(value, null)
				}
				case 'birthday': {
					println('birthday value has been changed to: ' + value)
					assertEquals(value, null)
				}
			}
		]
		
		// String
		name = null
		
		// Date
		birthday = null
	}
	
}
