package nl.kii.entity.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import java.util.Map
import nl.kii.entity.Entity
import nl.kii.entity.EntityExtensions

class JacksonExtensions {
	val static MAPPER = new ObjectMapper
	
	def static json(Entity entity) {
		MAPPER.writeValueAsString(entity.serialize)
	}
	
	def static Map<String, Object> map(String json) {
		MAPPER.readValue(json, Map)
	}
	
	def static <T extends Entity> create(Class<T> type, String json) {
		EntityExtensions.create(type, json.map)
	}
	
	def static <T extends Entity> receive(String json, Class<T> type) {
		type.create(json)
	}
	
}