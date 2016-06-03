package nl.kii.entity

import java.util.ArrayList
import java.util.Collection
import nl.kii.util.AssertionException
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

import static extension nl.kii.util.IterableExtensions.*

class EntityList<E> extends ArrayList<E> implements Reactive, EntityObject {

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


	override apply(Change p) {
		// silent
	}
	
	override isPublishing() {
		false
	}
	
	override onChange(Procedure1<? super Change> observeFn) {
		// silent
	}
	
	override setPublishing(boolean publish) {
		// silent
	}	
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
	
	