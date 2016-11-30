package nl.kii.entity

import java.util.List
import nl.kii.util.Opt

import static extension nl.kii.util.IterableExtensions.*
import static extension nl.kii.util.OptExtensions.*

class MultiSerializer<O, S> implements Serializer<O, S> {
	val List<? extends Serializer<O, S>> serializers
	
	new(List<? extends Serializer<O, S>> serializers) {
		if (serializers.nullOrEmpty) throw new IllegalArgumentException('serializers must be present')
		this.serializers = serializers
	}
 	
	override deserialize(S serialized) {
		serializers.match [ deserialize(serialized) ] ?: [ serializers.head.deserialize(serialized) ]
	}
	
	override serialize(O original) {
		serializers.match [ serialize(original) ] ?: [ serializers.head.serialize(original) ]
	}
	
	def private <T> Opt<T> match(List<? extends Serializer<O, S>> serializers, Function1<Serializer<O, S>, T> operation) {		
		val serializer = serializers.head
		if (!serializer.defined) none
		else {
			val result = attempt [ operation.apply(serializer) ]
			
			if (result.defined) result
			else serializers.tail.list.match(operation)
		}
	}
	
}

interface Function1<P1, R> {
	def R apply(P1 p1)
}