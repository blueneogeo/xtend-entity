package nl.kii.entity.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import java.util.Map
import nl.kii.entity.Entity
import nl.kii.entity.EntityExtensions
import java.util.List

class JacksonExtensions {
	val public static MAPPER = new ObjectMapper
	
	def static json(Entity entity) {
		MAPPER.writeValueAsString(entity.serialize)
	}
	
	def static Map<String, Object> map(String json) {
		MAPPER.readValue(json, Map)
	}
	
	def static List<Map<String, ?>> list(String jsonArray) {
		MAPPER.readValue(jsonArray, List)
	}
	
	def static <T extends Entity> create(Class<T> type, String json) {
		EntityExtensions.create(type, json.map)
	}
	
	def static <T extends Entity> receive(String json, Class<T> type) {
		type.create(json)
	}
	
	def static <T extends Entity> receiveList(String jsonArray, Class<T> type) {
		EntityExtensions.receiveList(jsonArray.list, type)
	}
	
}