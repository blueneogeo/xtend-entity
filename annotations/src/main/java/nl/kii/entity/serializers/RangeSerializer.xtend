package nl.kii.entity.serializers

import com.google.common.collect.Range
import nl.kii.entity.Serializer
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor

import static com.google.common.collect.BoundType.*

import static extension nl.kii.util.NumericExtensions.*
import static extension nl.kii.util.OptExtensions.*
import java.util.regex.Pattern

@FinalFieldsConstructor
class RangeSerializer<C extends Comparable<C>> implements Serializer<Range<C>, Object> {
	val Class<C> type
	val Serializer<C, Object> serializer

	static class Delimiters {
		val public static closed = '..'
		val public static openClosed = '>..'
		val public static closedOpen = '..<'
		val public static open = '>..<'
	}
	
	override deserialize(Object serialized) {
		val value = serialized.toString.trim
		
		val delimiter = value.findDelimiter
		val values = value.split(Pattern.quote(delimiter)).map [ deserializeValue ]
		
		val range = values.get(0) -> values.get(1)
		
		switch delimiter {
			case Delimiters.open: 		Range.open(range.key, range.value)
			case Delimiters.openClosed: Range.openClosed(range.key, range.value)
			case Delimiters.closedOpen: Range.closedOpen(range.key, range.value)
			case Delimiters.closed: 	Range.closed(range.key, range.value)
		}
	}
	
	def C deserializeValue(String value) {
		switch type {
			case Integer: value.parseInt
			case Long: value.parseLong
			case Float: value.parseFloat
			case Double: value.parseDouble
			default: if (serializer.defined) serializer.deserialize(value)
		} as C
	}
	
	def static findDelimiter(String serialized) {
		#[ Delimiters.open, Delimiters.closedOpen, Delimiters.openClosed, Delimiters.closed ]
			.findFirst [ serialized.split(Pattern.quote(it)).size == 2 ]
			.or [ throw new Exception('''could not find range delimiter in range «serialized»''') ]
	}
		
	override serialize(Range<C> original) '''«original.lowerEndpoint»«original.serializeBound»«original.upperEndpoint»'''
	
	def static serializeBound(Range<?> original) {
		boundsChart.get(original.lowerBoundType -> original.upperBoundType)
	}
	
//	def static deserializeDelimiter(String delimiter) {
//		boundsChart.findFirst [ value == delimiter ].key
//	}
	
	val static boundsChart = #{
		(OPEN -> OPEN) 		-> Delimiters.open,
		(OPEN -> CLOSED) 	-> Delimiters.openClosed,
		(CLOSED -> OPEN) 	-> Delimiters.closedOpen,
		(CLOSED -> CLOSED)	-> Delimiters.closed
	}
	
}
