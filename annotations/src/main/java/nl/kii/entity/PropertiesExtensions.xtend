package nl.kii.entity

import java.util.List
import java.util.Properties

import static extension nl.kii.util.IterableExtensions.*
import java.io.StringReader

class PropertiesExtensions {
	
	def static <T extends Entity> T create(Class<T> type, Properties properties) {
		EntityExtensions.create(type, properties.stringPropertyNames.map [ toLowerCase -> properties.getProperty(it) ].toMap)
	}
	
	def static <T extends Entity> receive(Properties properties, Class<T> type) {
		type.create(properties)
	}
	
	def static <T extends Entity> receiveList(List<Properties> properties, Class<T> type) {
		properties.map [ receive(type) ]
	}
	
	def static properties(Entity entity) {
		new Properties => [ putAll(entity.serialize) ]
	}
		
	def static properties(String properties) {
		new Properties => [ load(new StringReader(properties)) ]
	}
	
}