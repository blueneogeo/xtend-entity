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

class EntityList<E> extends ArrayList<E> implements Reactive, EntityObject {

	// the contained type of the list. this is necessary because we lose
	// type info due to erasure, and we need the type in order to create
	// it from an incoming value
	val Class<E> type
	val boolean isReactive
	
	@Atomic transient Publisher<Change> publisher
	@Atomic transient var Map<Integer, =>void> subscriptionEnders
		
	// CONSTRUCTORS

	new(Class<E> type) { 
		super()
		this.type = type
		isReactive = true
		subscriptionEnders = newHashMap
	}

	new(Class<E> type, int size) { 
		super(size)
		this.type = type
		isReactive = true
		subscriptionEnders = newHashMap
	}

	new(Class<E> type, Collection<? extends E> coll) { 
		super(coll)
		this.type = type
		isReactive = true
		subscriptionEnders = newHashMap
	}
	
	def getType() { type }
	
	// MAKE THE LIST LISTENABLE

	def private publish(Change change) {
		publisher?.apply(change)
	}

	// IMPLEMENT REACTIVEOBJECT

	override onChange((Change)=>void listener) {
		if(publisher == null) publisher = new Publisher
		publisher.onChange(listener)
	}
	
	override setPublishing(boolean publish) {
		if(publisher == null && !publish) publisher = new Publisher
		if(publisher != null) publisher.publishing = publish
	}
	
	override isPublishing() {
		publisher != null && publisher.publishing
	}
	
	// WRAP ALL METHODS THAT MODIFY THE LIST TO FIRE A CHANGE EVENT

	def private observe(E element)	{
		switch element {
			Observable<Change>: element.onChange [ change |
				// propagate the change, but expand the path to the element that was updated
				val path = element.indexOf.toString
				publish(change.addPath(path))
			]
		}
	}

	override E set(int index, E value) {
		// cancel previous value subscription, if any 
		subscriptionEnders.get(index)?.apply
		// set the new value
		val previous = super.set(index, value)
		// observe the new value and store the subscription ender
		val subscriptionEnder = observe(value)
		subscriptionEnders.put(index, subscriptionEnder)
		// publish the change
		publish(new Change(UPDATE, index, value))
		// return the previous value
		previous		
	}
	
	override boolean add(E element) {
		val success = super.add(element)
		if(!success) return false 
		// observe the new value and store the subscription ender
		val subscriptionEnder = observe(element)
		val index = element.indexOf
		subscriptionEnders.put(index, subscriptionEnder)
		// publish the change
		publish(new Change(ADD, element))
		// return the previous value
		true
	}
	
	override void add(int index, E value) {
		// cancel previous value subscription, if any 
		subscriptionEnders.get(index)?.apply
		// set the new value
		super.add(index, value)
		// observe the new value and store the subscription ender
		val subscriptionEnder = observe(value)
		subscriptionEnders.put(index, subscriptionEnder)
		// publish the change
		publish(new Change(UPDATE, index, value))
	}
	
	override E remove(int index) {
		// cancel previous value subscription, if any 
		subscriptionEnders.get(index)?.apply
		// remove the value
		val previous = super.remove(index)
		// publish the change
		if(previous != null) publish(new Change(REMOVE, index, previous)) 
		// return the previous value
		previous
	}
	
	override boolean remove(Object o) {
		val index = indexOf(o)
		if(index < 0) return false
		// cancel previous value subscription, if any 
		subscriptionEnders.get(index)?.apply
		// remove the value
		val success = super.remove(o)
		if(!success) return false
		publish(new Change(REMOVE, index, o))
		true
	}
	
	override void clear() {
		// stop listening to all values
		subscriptionEnders.forEach [ k, v |	v?.apply ]
		subscriptionEnders.clear
		// clear the list
		super.clear
		// publish the change
		publish(new Change(CLEAR))
	}
	
	override boolean addAll(Collection<? extends E> c) {
		for(it : c) {
			add(it)
		}
		true
	}
	
	override boolean removeAll(Collection<?> c) {
		for(it : c) {
			remove(it)
		}
		true
	}
	
	@Deprecated
	override boolean addAll(int index, Collection<? extends E> c) {
		throw new UnsupportedOperationException
	}
	
	@Deprecated
	override boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException
	}
	
	private def getIndex(Change change) {
		try { 
			change.path.head.parseInt
		} catch(Exception e) {
			throw new EntityException('could not parse list index from change path, for ' + change)
		}
	}
	
	override apply(Change change) {
		val wasPublishing = publisher != null && !publisher.publishing
		try {
			publisher?.setPublishing(false)
			switch path: change.path {
				// applies to the whole list
				case null,
				case path.size == 0: {
					switch change.action {
						case ADD: {
							if(!change.value.class.isAssignableFrom(type)) 
								throw new EntityException('value is not of correct type ' + type.simpleName + ', could not apply ' + change )
							add(change.value as E)
						}
						case UPDATE: {
							if(!(change.value instanceof List<?>)) 
								throw new EntityException('value is not a list, could not apply ' + change)
							val list = change.value as List<E>
							if(!list.empty && !list.head.class.isAssignableFrom(type)) throw new EntityException('change value is a list of the wrong type, expecting a List<' + type.name + '> but got a List<' + list.head.class.name + '> instead. For ' + change)
							clear
							addAll(list)
						}
						case REMOVE: {
							throw new EntityException('cannot remove, change contains no index: ' + change)
						}
						case CLEAR: clear
					}
				}
				// changes an entry in this list
				case path.size == 1: {
					switch change.action {
						case ADD: {
							val value = get(change.index)
							if(value == null) throw new EntityException('path points to an empty value in the map, could not apply ' + change)
							change.applyToValue(value)
						} 
						case UPDATE: {
							if(!change.value.class.isAssignableFrom(type)) throw new EntityException('value is not of correct type ' + type.simpleName + ', could not apply ' + change )
							set(change.index, change.value as E)
						}
						case REMOVE, case CLEAR: {
							val index = change.index
							if(get(index) != null)
								remove(index)
						}
					}
				}
				// applies to inside an entry in this list 
				case path.size > 1: {
					val value = get(change.index)
					if(value == null) throw new EntityException('path points to an empty value in the map, could not apply ' + change)
					change.applyToValue(value)
				}
			}
		} finally {
			publisher?.setPublishing(wasPublishing)
		}
	}
	
	private def applyToValue(Change change, E value) {
		if(!(value instanceof Reactive)) throw new EntityException('path points inside an object that is not Reactive, could not apply ' + change)
		val reactive = value as Reactive
		reactive.apply(change.forward)
	} 

	override EntityList<E> clone() {
		super.clone as EntityList<E>
	}
	
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

}
	
	