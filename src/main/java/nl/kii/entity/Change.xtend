package nl.kii.entity

import java.util.List

import static extension nl.kii.util.IterableExtensions.*
import static extension nl.kii.util.OptExtensions.*

enum ChangeType {
	ADD, 
	UPDATE,
	REMOVE,
	CLEAR
}

/**
 * Represents a change to a reactive entity 
 * <p>
 * @Property action - the ChangeType what was made. it is stored as changeId so it can be sent as json.
 * @Property path - contains a / seperated path, for example '1/name' from #[ 23, #{ name: 'mary' }] refers to 'mary'
 * @Property value - the new value of the item referenced by the path, if any
 */

class Change implements EntityObject {

	val public static PATH_SEPARATOR = '.'

	val long id
	val ChangeType action
	val List<String> path
	val Object value

	// CONSTRUCTOR
	
	new(ChangeType action) {
		this(-1, action, null, null)
	}

	new(ChangeType action, Object value) {
		this(-1, action, null, value)
	}

	new(ChangeType action, int index, Object value) {
		this(-1, action, #['' + index], value)
	}

	new(ChangeType action, String key, Object value) {
		this(-1, action, #[key], value)
	}

	new(ChangeType action, List<String> path, Object value) {
		this(-1, action, path, value)
	}

	new(long id, ChangeType action, List<String> path, Object value) {
		this.id = id
		this.action = action
		this.path = path
		this.value = value
	}

	// GETTERS
	
	def getId() { id }
	def getAction() { action }
	def getValue() { value }
	def getPath() { path ?: #[] }
	
	// CREATE A NEW CHANGE FOR PATH CHANGES ///////////////////////////////////
	
	/** add a part to the path of a change, and return a new change from that */
	def addPath(String addedPath) {
		new Change(id, action, addedPath.concat(getPath), value)
	}
	
	/** remove the first part of the path of a change and create a new change from that */
	def forward() {
		if(path == null || path.length == 0) throw new EntityException('cannot forward a change with an empty path. for change ' + this)
		new Change(id, action, path.tail.list, value)
	}
	
	// ENTITY IMPLEMENTATION //////////////////////////////////////////////////
	
	override validate() {
	}
	
	override toString() '''«action» «path?.join('.')»«IF value.defined && path?.length > 0» = «ENDIF»«IF value.defined»"«value»"«ENDIF»'''
	
	override equals(Object o) {
		switch o {
			Change: o.id == id && o.action == action && o.path == path && o.value == value
			default: true
		}
	}
	
	override hashCode() {
		id.hashCode * action.hashCode * path?.hashCode * value?.hashCode * 37
	}
	
	override Change clone() {
		new Change(id, action, path?.clone, value)
	}
	
}
