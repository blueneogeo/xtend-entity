package nl.kii.entity

import java.util.HashMap
import java.util.Map
import nl.kii.async.annotation.Atomic
import nl.kii.observe.Observable
import nl.kii.observe.Publisher

import static nl.kii.entity.ChangeType.*
import java.util.List
import java.util.Date
import java.text.DateFormat
import java.text.SimpleDateFormat

class EntityMap<K, V> extends HashMap<K, V> implements Reactive, EntityObject {
	
	/** Using the standard Javascript date format */
	public static val DateFormat KEY_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z")
	
	public enum SupportedTypes {
		String, Integer, Long, Float, Double, Boolean, Date
	}
	
	// the contained type of the map. this is necessary because we lose
	// type info due to erasure, and we need the type in order to create
	// it from an incoming value
	val Class<K> keyType
	val Class<V> type
	val boolean isReactive

	@Atomic transient Publisher<Change> publisher
	@Atomic transient Map<K, =>void> subscriptionEnders
	
	// CONSTRUCTORS
	
	new(Class<K> keyType, Class<V> type) { 
		super()
		this.keyType = keyType
		this.type = type
		this.isReactive = true
		this.subscriptionEnders = newHashMap
	}

	new(Class<K> keyType, Class<V> type, int size) { 
		super(size)
		this.keyType = keyType
		this.type = type
		this.isReactive = true
	}

	new(Class<K> keyType, Class<V> type, Map<? extends String, ? extends V> m) {
		this.type = type
		this.keyType = keyType
		this.isReactive = true
	}
	
	private def isSupportedType(Class<K> type) {
		
	}
	
	def getType() { type }
	
	// MAKE THE MAP LISTENABLE

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
	
	def private observe(V element, K key)	{
		switch element {
			Observable<Change>: element.onChange [ change |
				// propagate the change, but expand the path to the element that was updated
				publish(change.addPath(key.toPathString))
			]
		}
	}
	
	override V put(K key, V value) {
		// cancel previous value subscription, if any 
		subscriptionEnders.get(key)?.apply
		// set the new value
		val previous = super.put(key, value)
		// observe the new value and store the subscription ender
		val subscriptionEnder = observe(value, key)
		subscriptionEnders.put(key, subscriptionEnder)
		// publish the change
		publish(new Change(UPDATE, #[toPathString(key)], value))
		// return the previous value
		previous 
	}
	
	override V remove(Object key) {
		if(!keyType.isAssignableFrom(key.class))
			throw new EntityException(this.toString + ' cannot remove key ' + key + ' since it is not a ' + key.class.simpleName)
		// cancel previous value subscription, if any 
		subscriptionEnders.get(key)?.apply
		// remove the value
		val previous = super.remove(key)
		// publish the change
		if(previous != null) publish(new Change(REMOVE, #[toPathString(key as K)], previous)) 
		// return the previous value
		previous
	}
	
	override putAll(Map<? extends K, ? extends V> m) {
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
		publish(new Change(CLEAR, #[], null))
	}
	
	override apply(Change change) {
		val wasPublishing = publisher != null && !publisher.publishing
		try {
			publisher?.setPublishing(false)
			switch path : change.path {
				// applies to the whole map
				case path == null,
				case path.size == 0: {
					switch change.action {
						case ADD: throw new EntityException('map does not support ADD, use UPDATE, for ' + change)
						case UPDATE: {
							if(!(change.value instanceof Map<?,?>)) throw new EntityException('value is not a map, could not apply ' + change)
							clear
							val map = change.value as Map<K, V>
							if(!map.empty && !map.values.head.class.isAssignableFrom(type)) throw new EntityException('change value is a list of the wrong type, expecting a List<' + type.name + '> but got a List<' + map.values.head.class.name + '> instead. For ' + change)
							putAll(map)
						}
						case REMOVE: throw new EntityException('cannot remove, change contains no index: ' + change)
						case CLEAR: clear
					}
				}
				// changes an entry in this map
				case path.size == 1: {
					switch change.action {
						case ADD: throw new EntityException('map does not support ADD, use UPDATE, for ' + change) 
						case UPDATE: {
							val key = change.path.head
							if(!key.class.isAssignableFrom(keyType)) throw new EntityException('key is not of correct type ' + type.simpleName + ', could not apply ' + change)
							if(!change.value.class.isAssignableFrom(type)) throw new EntityException('value is not of correct type ' + type.simpleName + ', could not apply ' + change)
							put(change.path.head.toKeyType as K, change.value as V)
						}
						case REMOVE, case CLEAR: {
							val key = change.path.head
							if(!key.class.isAssignableFrom(keyType)) throw new EntityException('key is not of correct type ' + type.simpleName + ', could not apply ' + change)
							val mapKey = key.toKeyType
							if(containsKey(mapKey))
								remove(mapKey)
						}
					}
				}
				// applies to inside an entry in this map 
				case path.size > 1: {
					val key = change.path.head
					if(!key.class.isAssignableFrom(keyType)) throw new EntityException('key is not of correct type ' + type.simpleName + ', could not apply ' + change)
					val value = get(key.toKeyType)
					if(value == null) throw new EntityException('path points to an empty value in the map, could not apply ' + change)
					if(!(value instanceof Reactive)) throw new EntityException('path points inside an object that is not Reactive, could not apply ' + change)
					val reactive = value as Reactive
					reactive.apply(change.forward)
				}
			}
		} finally {
			publisher?.setPublishing(wasPublishing)
		}
	}
	
	override EntityMap<K, V> clone() {
		super.clone as EntityMap<K, V>
	}
	
	override validate() {
	}
	
	override isValid() {
		true
	}
	
	override equals(Object o) {
		super.equals(o)
	}
	
	override hashCode() {
		super.hashCode()
	}
	
	override getInstanceType(List<String> path) throws EntityException {
		switch it : path {
			case null, case length == 0: EntityList
			case length == 1: type
			default: {
				if(EntityObject.isAssignableFrom(type)) {
					(type.newInstance as EntityObject).getInstanceType(path.tail.toList)
				} else throw new EntityException('EntityList cannot apply path ' + path.tail + ' to type ' + type)
			}
		}
	}
	
	/** 
	 * Convert a type key to a String.
	 * Uses normal type Type.toString methods, except for Date, which uses
	 * the KEY_DATE_FORMAT as defined in this class.
	 */
	def toPathString(K key) {
		switch keyType {
			case Date: KEY_DATE_FORMAT.format(key as Date)
			default: key.toString
		}
	}
	
	/** 
	 * Try to convert a string to one of the supported key types.
	 * Uses normal Type.parseType methods, except for Date, which uses
	 * the KEY_DATE_FORMAT as defined in this class.
	 */
	def toKeyType(String s) {
		try {
			switch keyType {
				case String: s
				case Integer: Integer.parseInt(s)
				case Float: Float.parseFloat(s)
				case Double: Double.parseDouble(s)
				case Long: Long.parseLong(s)
				case Boolean: Boolean.parseBoolean(s)
				case Date: KEY_DATE_FORMAT.parse(s)
				default: throw new EntityException(this.toString + ' could not convert "' + s + '" to a valid key, since s is not a supported type. Supported types are: ' + EntityMap.SupportedTypes.values.join(', '))
			}
		} catch(Exception e) {
			throw new EntityException(this.toString + ' could not convert "' + s + '" to a valid key.', e)
		}
		
	}
	
	override toString() '''EntityMap<«keyType.simpleName»,«type.simpleName»>(size: «size»)'''
	
}
