package nl.kii.entity

import java.util.List
import java.util.Map
import nl.kii.entity.processors.EntityProcessor
import org.eclipse.xtend.lib.annotations.Data

import static extension nl.kii.entity.EntityExtensions.*
import static extension nl.kii.util.IterableExtensions.*
import static extension nl.kii.util.OptExtensions.*

class EntityExtensions {

//	def static <T extends Entity> deserialize(Object serialized, Class<T> type) {
//		val entity = type.newInstance
//		switch serialized {
//			Map<String, Object>: entity.deserialize(serialized)
//		}
//	}	
	
	def static <T extends Entity> deserialize(Map<String, ?> serialized, Class<T> type) {
		val entity = type.newInstance
//		switch serialized {
//			Map<String, Object>: entity.deserialize(serialized)
//		}
		entity.deserialize(serialized) as T
	}
	
	def static <T extends Entity> T create(Class<T> type, Map<String, ?> serialized) {
		serialized.deserialize(type)
	}
	
	def static <T extends Entity> T receive(Map<String, ?> serialized, Class<T> type) {
		type.create(serialized)
	}

	def static <T extends Entity> receiveList(List<Map<String, ?>> serialized, Class<T> type) {
		serialized.map [ receive(type) ]
	}
		
	def static <K, V> get(Iterable<? extends Pair<K, V>> iterable, K key) {
		iterable.findFirst [ it.key == key ]?.value
	}
	
	def static <T extends Enum<?>> valueOfCaseInsensitive(Class<T> enumType, String value) {
		enumType.enumConstants.findFirst [ name.equalsIgnoreCase(value) ] => [
			if (!defined) throw new IllegalArgumentException('''No enum constant «enumType.canonicalName».«value»''')
		]
	}
	
	/**
	 * Returns a map of the value declared in the {@code entities}' {@code @Type} fields if set and the entity class,
	 */
	def static <T extends Entity> typeMappings(Class<? extends T>... entityTypes) { new TypeMappings(entityTypes) }
	
	/** Returns the value declared in the entity's {@code @Type} field, or null if not set. */
	def static getTypeFieldValue(Class<? extends Entity> type) {
		type.getField(EntityProcessor.TYPE_CONSTANT)?.get(null) as String
	}
	
	/** Returns the value declared in the entity's {@code @Type} field, or null if not set. */
	def static getTypeFieldName(Class<? extends Entity> type) {
		val fieldClass = Class.forName(EntityProcessor.getEntityFieldsClassName(type.name))
		val field = fieldClass.getField(EntityProcessor.TYPE_FIELD_CONSTANT)?.get(null) as EntityField
		field?.formattedName
	}
	
	def static <T extends Entity> receive(Map<String, ?> serialized, TypeMappings<T> typeMappings) {
		val type = typeMappings.getType(serialized)
		serialized.receive(type)
	}
}

@Data
class TypeMappings<T extends Entity> {
	/** Index of name and corresponding type */
	val Map<String, Class<? extends T>> mappings
	
	/** Entity field name to match on when parsing */
	val String typeField
	
	new(Class<? extends T>... types) {
		if (types.empty) throw new Exception('Specified types cannot be empty')
		
		this.mappings = types.map [ typeFieldValue -> it ].toMap
		if (this.mappings.containsKey(null)) 
			throw new Exception('Every specified type has to have a field annotated with @Type')
		
		val typeFields = types.map [ typeFieldName -> it ]
		val typeFieldPossibilities = typeFields.groupBy [ key ].toPairs => [
			if (size > 1) 
				throw new Exception('''Specified types have to have the same @Type-annotated field name. Found: «typeFields.map [ key ].distinct.join(', ')»''')
			if (empty || head.key.nullOrEmpty) 
				throw new Exception('''Specified types do not contain @Type-annotated field: «typeFields.filter [ key.nullOrEmpty ].map [ value.name ].join(', ')»''')
		]
		this.typeField = typeFieldPossibilities.head.key
	}
	
	def Class<? extends T> get(String name) {
		mappings.get(name)
	}
	
	def Class<? extends T> getType(Map<String, ?> entity) {
		val type = entity.get(typeField)
		if (!type.defined) throw new Exception('''Passed in object does not have field '«typeField»' defined''')
		
		val matchedType = get(type.toString)
		if (!matchedType.defined) throw new Exception('''No matching type found for for type field '«type»' ''')
		matchedType
	}
	
	def T convert(Map<String, ?> entity) {
		val type = getType(entity)
		entity.receive(type)
	}
}