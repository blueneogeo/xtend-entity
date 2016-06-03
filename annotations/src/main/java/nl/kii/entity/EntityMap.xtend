package nl.kii.entity

import java.text.SimpleDateFormat
import java.util.Date
import java.util.HashMap
import java.util.Map
import nl.kii.util.AssertionException

import static extension nl.kii.util.IterableExtensions.*
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

class EntityMap<K, V> extends HashMap<K, V> implements Reactive, EntityObject {
	
	/** Using the standard Javascript date format */
	public static val KEY_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS Z"
	public val KEY_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z")
	
	public enum SupportedTypes {
		String, Integer, Long, Float, Double, Boolean, Date
	}
	
	// the contained type of the map. this is necessary because we lose
	// type info due to erasure, and we need the type in order to create
	// it from an incoming value
	val Class<K> keyType
	val Class<V> type

	// CONSTRUCTORS
	
	new(Class<K> keyType, Class<V> type) { 
		super()
		this.keyType = keyType
		this.type = type
	}

	new(Class<K> keyType, Class<V> type, int size) { 
		super(size)
		this.keyType = keyType
		this.type = type
	}

	new(Class<K> keyType, Class<V> type, Map<? extends K, ? extends V> m) {
		super(m)
		this.type = type
		this.keyType = keyType
	}
	
	private def isSupportedType(Class<K> type) {
	}
	
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
	
	def getType() { type }
	
	override EntityMap<K, V> clone() {
		super.clone as EntityMap<K, V>
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
	
	/** 
	 * Convert a type key to a String.
	 * Uses normal type Type.toString methods, except for Date, which uses
	 * the KEY_DATE_FORMAT as defined in this class.
	 */
	def toPathString(K key) {
		switch keyType {
			case Date: KEY_DATE_FORMATTER.format(key as Date)
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
				case Date: KEY_DATE_FORMATTER.parse(s)
				default: throw new EntityException(this.toString + ' could not convert "' + s + '" to a valid key, since s is not a supported type. Supported types are: ' + EntityMap.SupportedTypes.values.join(', '))
			}
		} catch(Exception e) {
			throw new EntityException(this.toString + ' could not convert "' + s + '" to a valid key.', e)
		}
		
	}
	
	override toString() '''EntityMap<«keyType.simpleName»,«type.simpleName»>(size: «size»)'''
	
	override getFields() {
		this.keySet.map[toString].list
	}
	
	override getValue(String key) {
		super.get(key)
	}
	
	override isValid() { true }
	
	override validate() throws AssertionException {	}
	
	override setValue(String field, Object value) throws NoSuchFieldException {
		try {
			this.put(field as Object as K, value as V)
		} catch (ClassCastException e) {
			throw new NoSuchFieldException('EntityMap<'+ keyType.simpleName + ', ' + type.simpleName + '> cannot be set with a key of type String and a value of type ' + value.class.simpleName)
		}
	}
	
}
