package nl.kii.entity

import java.util.HashMap
import java.util.Map
import java.util.concurrent.atomic.AtomicReference
import nl.kii.observe.Observable
import nl.kii.observe.Publisher

import static nl.kii.reactive.ChangeType.*

class EntityMap<V> extends HashMap<String, V> implements nl.kii.reactive.Reactive {
	
	// the contained type of the map. this is necessary because we lose
	// type info due to erasure, and we need the type in order to create
	// it from an incoming value
	val Class<V> type
	val boolean isReactive

	transient val _publisher = new AtomicReference<Publisher<nl.kii.reactive.Change>>
	transient var Map<String, =>void> subscriptionEnders = newHashMap
	
	// CONSTRUCTORS
	
	new(Class<V> type) { 
		super()
		this.type = type
		this.isReactive = true
	}

	new(Class<V> type, int size) { 
		super(size)
		this.type = type
		this.isReactive = true
	}

	new(Class<V> type, Map<? extends String, ? extends V> m) {
		this.type = type
		this.isReactive = true
	}
	
	// MAKE THE MAP LISTENABLE

	def private Publisher<nl.kii.reactive.Change> getPublisher() {
		_publisher.get
	}

	def private initPublisher() {
		if(_publisher.get == null) 
			_publisher.set(new Publisher)
	}

	override onChange((nl.kii.reactive.Change)=>void listener) {
		initPublisher
		publisher.onChange(listener)
	}

	def private publish(nl.kii.reactive.Change change) {
		publisher?.apply(change)
	}
	
	// WRAP ALL METHODS THAT MODIFY THE LIST TO FIRE A CHANGE EVENT
	
	def private observe(V element, String key)	{
		switch element {
			Observable<nl.kii.reactive.Change>: element.onChange [ change |
				// propagate the change, but expand the path to the element that was updated
				publish(change.addPath(key))
			]
		}
	}
	
	override V put(String key, V value) {
		// cancel previous value subscription, if any 
		subscriptionEnders.get(key)?.apply
		// set the new value
		val previous = super.put(key, value)
		// observe the new value and store the subscription ender
		val subscriptionEnder = observe(value, key)
		subscriptionEnders.put(key, subscriptionEnder)
		// publish the change
		publish(new nl.kii.reactive.Change(UPDATE, #[key], value))
		// return the previous value
		previous 
	}
	
	override V remove(Object key) {
		// cancel previous value subscription, if any 
		subscriptionEnders.get(key)?.apply
		// remove the value
		val previous = super.remove(key)
		// publish the change
		if(previous != null) publish(new nl.kii.reactive.Change(REMOVE, #[key.toString], previous)) 
		// return the previous value
		previous
	}
	
	override putAll(Map<? extends String, ? extends V> m) {
		for(key : m.keySet)	
			put(key, m.get(key))
	}
	
	override clear() {
		// stop listening to all values
		subscriptionEnders.forEach [ k, v |	v?.apply ]
		subscriptionEnders.clear
		// clear the map
		super.clear
		// publish the change
		publish(new nl.kii.reactive.Change(CLEAR, #[], null))
	}
	
	override apply(nl.kii.reactive.Change change) {
		val wasPublishing = publisher != null && !publisher.publishing
		try {
			publisher?.setPublishing(false)
			switch path : change.path {
				// applies to the whole map
				case path == null,
				case path.size == 0: {
					switch change.action {
						case ADD: throw new nl.kii.reactive.EntityException('map does not support ADD, use UPDATE, for ' + change)
						case UPDATE: {
							if(!(change.value instanceof Map<?,?>)) throw new nl.kii.reactive.EntityException('value is not a map, could not apply ' + change)
							clear
							val map = change.value as Map<String, V>
							if(!map.empty && !map.values.head.class.isAssignableFrom(type)) throw new nl.kii.reactive.EntityException('change value is a list of the wrong type, expecting a List<' + type.name + '> but got a List<' + map.values.head.class.name + '> instead. For ' + change)
							putAll(map)
						}
						case REMOVE: throw new nl.kii.reactive.EntityException('cannot remove, change contains no index: ' + change)
						case CLEAR: clear
					}
				}
				// changes an entry in this map
				case path.size == 1: {
					switch change.action {
						case ADD: throw new nl.kii.reactive.EntityException('map does not support ADD, use UPDATE, for ' + change) 
						case UPDATE: {
							if(!change.value.class.isAssignableFrom(type)) throw new nl.kii.reactive.EntityException('value is not of correct type ' + type.simpleName + ', could not apply ' + change )
							put(change.path.head, change.value as V)
						}
						case REMOVE, case CLEAR: {
							val key = change.path.head
							if(containsKey(key))
								remove(change.path.head)
						}
					}
				}
				// applies to inside an entry in this map 
				case path.size > 1: {
					val value = get(change.path.head)
					if(value == null) throw new nl.kii.reactive.EntityException('path points to an empty value in the map, could not apply ' + change)
					if(!(value instanceof nl.kii.reactive.Reactive)) throw new nl.kii.reactive.EntityException('path points inside an object that is not Reactive, could not apply ' + change)
					val reactive = value as nl.kii.reactive.Reactive
					reactive.apply(change.forward)
				}
			}
		} finally {
			publisher?.setPublishing(wasPublishing)
		}
	}
	
	override nl.kii.reactive.EntityMap<V> clone() {
		super.clone as nl.kii.reactive.EntityMap<V>
	}
	
}
