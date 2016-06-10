package nl.kii.entity.yaml

import java.util.Map
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.DumperOptions.FlowStyle
import org.yaml.snakeyaml.Yaml

class YamlExtensions {
	def static yaml(String string) {
		new Yaml().load(string.replace('\t', ' ')) as Map<String, Object>
	}
	
	def static yaml(Map<String, Object> map, DumperOptions options) {
		new Yaml(options).dump(map)
	}
	
	def static yaml(Map<String, Object> map, (DumperOptions)=>void options) {
		yaml(map, new DumperOptions => [ options.apply(it) ])
	}
	
	def static yaml(Map<String, Object> map) {
		yaml(map, DUMP_OPTIONS)
	}
	
	val static DUMP_OPTIONS = new DumperOptions => [ 
		defaultFlowStyle = FlowStyle.BLOCK
		prettyFlow = true
	]

	def static yaml(CharSequence chars) {
		yaml(chars.toString)
	}
	
	def static loadYaml(String string) {
		yaml(string)
	}
	
	def static dumpYaml(Map<String, Object> map) {
		yaml(map)
	}	
}