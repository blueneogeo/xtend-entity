package nl.kii.entity

import static extension nl.kii.util.IterableExtensions.*

import java.util.ArrayList
import java.util.Collection
import java.util.List
import java.util.Map
import nl.kii.async.annotation.Atomic
import nl.kii.observe.Observable
import nl.kii.observe.Publisher

import static nl.kii.entity.ChangeType.*

import static extension java.lang.Integer.*
import nl.kii.util.AssertionException

class EntityList<E> extends ArrayList<E> implements EntityObject {

	// the contained type of the list. this is necessary because we lose
	// type info due to erasure, and we need the type in order to create
	// it from an incoming value
	val Class<E> type
			
	// CONSTRUCTORS

	new(Class<E> type) { 
		super()
		this.type = type
	}

	new(Class<E> type, int size) { 
		super(size)
		this.type = type
	}

	new(Class<E> type, Collection<? extends E> coll) { 
		super(coll)
		this.type = type
	}
	
	def getType() { type }
	
	// IMPLEMENT REACTIVEOBJECT

	
	// WRAP ALL METHODS THAT MODIFY THE LIST TO FIRE A CHANGE EVENT


	override getInstanceType(String... path) {
		switch it : path {
			case null, case length == 0: EntityList
			case length == 1: type
			default: {
				if(EntityObject.isAssignableFrom(type)) {
					(type.newInstance as EntityObject).getInstanceType(path.tail.toList)
				} else throw new NoSuchFieldException('EntityList cannot apply path ' + path.tail + ' to type ' + type)
			}
		}
	}
	
	override getFields() {
		(0..this.size).map[toString].list
	}
	
	override getValue(String key) {
		val i = Integer.parseInt(key)
		this.get(i)
	}
	
	override isValid() {
		true
	}
	
	override validate() throws AssertionException { }
	
	override setValue(String field, Object value) throws NoSuchFieldException {
		try {
			this.add(Integer.parseInt(field), value as E)
		} catch (NumberFormatException e) {
			throw new NoSuchFieldException('EntityList.setValue should have a number as the field')
		} catch (ClassCastException e) {
			throw new NoSuchFieldException('EntityList.setValue cannot be set with a value of type ' + value.class.simpleName)
		}
	}

}
	
	