package nl.kii.entity.processors

import com.google.common.base.CaseFormat
import java.util.List
import java.util.Map
import nl.kii.entity.Casing
import nl.kii.entity.Entity
import nl.kii.entity.EntityExtensions
import nl.kii.entity.Serializer
import nl.kii.entity.processors.EntityProcessor.Util
import nl.kii.util.MapExtensions
import nl.kii.util.OptExtensions
import org.eclipse.xtend.lib.macro.TransformationContext
import org.eclipse.xtend.lib.macro.declaration.FieldDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration
import org.eclipse.xtend.lib.macro.declaration.TypeReference
import org.eclipse.xtext.xbase.lib.Functions.Function1
import org.eclipse.xtext.xbase.lib.Functions.Function2

import static extension nl.kii.util.IterableExtensions.*
import static extension nl.kii.util.OptExtensions.*

class EntitySerializationUtil {
	val extension TransformationContext context

	val nativeSerializationTypes = #[
		String,
		Integer,
		Float,
		Long,
		Double,
		Boolean,
		List,
		Map
	]

	val outOfTheBoxTypes = nativeSerializationTypes + #[
		Enum,
		Entity
	]
	
	val List<Pair<TypeReference, String>> serializers
	val CaseFormat casing
	
	def getSerializedMapTypeRef() { Map.newTypeReference(string, object) }
	def getDeserializedMapTypeRef() { Map.newTypeReference(string, newWildcardTypeReference) }
	
	def getSupportedTypes() {
		outOfTheBoxTypes.map [ newTypeReference ] + serializers.map [ key ]
	}
	
	val extension EntityProcessor.Util baseUtil
	
	new(TransformationContext context, List<Pair<TypeReference, String>> serializers, Casing casing) {
		this.context = context
		this.baseUtil = new Util(context)
		this.serializers = serializers
		this.casing = switch casing {
			case underscore, case snake:			CaseFormat.LOWER_UNDERSCORE
			case camel, case lowerCamel:			CaseFormat.LOWER_CAMEL
			case dash, case hyphen: 				CaseFormat.LOWER_HYPHEN
			case upperCamel: 						CaseFormat.UPPER_CAMEL
			case upperUnderscore, case upperSnake: 	CaseFormat.UPPER_UNDERSCORE
		}
	}
	
	def isSupported(TypeReference type) {
		val supportedTypes = concat(outOfTheBoxTypes.map [ newTypeReference ], serializers.map [ key ])
		//val typeRef = type.newTypeReference
		
		supportedTypes.exists [ type.extendsType(it) ] || 
		findEnumerationType(type.name).defined /** <-- workaround for compiler not recognizing enums in same source file */ ||
		{
			val cls = findClass(type.name) /** <-- workaround for compiler not recognizing entities in same source file */
			cls.defined && cls.annotations.exists [ annotationTypeDeclaration == nl.kii.entity.annotations.Entity.newAnnotationReference.annotationTypeDeclaration ]
		}
	}
	
	def validateFields(MutableClassDeclaration cls, Iterable<? extends FieldDeclaration> fields) {
		fields.filter [ !type.isSupported ].list.forEach [ 
			addError('''Fields of type «type.name» cannot be serialized, add a Serializer to make the type compatible''')
		]
	}

	val static serializersFieldName = '_serializers'
	def addSerializers(MutableClassDeclaration cls) {
		if (serializers.nullOrEmpty) return
		val wildCardRef = newWildcardTypeReference
		val classTypeRef = Class.newTypeReference(wildCardRef)
		val serializerRef = Serializer.newTypeReference(wildCardRef, Object.newTypeReference)
		
		val pairRef = Pair.newTypeReference
		
		cls.addField(serializersFieldName) [
			primarySourceElement = cls
			
			type = Map.newTypeReference(classTypeRef, serializerRef)
			initializer = ['''
				«CollectionLiterals.newTypeReference.name».newLinkedHashMap(
					«FOR s:serializers SEPARATOR ', '»
						«pairRef.name».of(«s.key.name».class, «s.value»)
					«ENDFOR»
				)
			''']
		]
	}
	

	val static serializeResultName = 'serialized'
	def void addSerializeMethod(MutableClassDeclaration cls, Iterable<? extends FieldDeclaration> fields) {
		cls.addMethod('serialize') [
			val mapTypeRef = serializedMapTypeRef
			primarySourceElement = cls
			addAnnotation(Pure.newAnnotationReference)
			addAnnotation(Override.newAnnotationReference)			
			returnType = serializedMapTypeRef
			body = ['''
				«mapTypeRef» «serializeResultName» = «CollectionLiterals.newTypeReference.name».newLinkedHashMap();
				
				«fields.map [ serializationBody ].join('\n')»
				
				return serialized;
			''']
		]
	}	
	
	def getSerializationBody(extension FieldDeclaration field) '''
		if («OptExtensions.newTypeReference.name».defined(«simpleName»)) {
			«switch f:field.type {
				case nativeSerializationTypes.exists [ f.extendsType(it) ]: '''
					«serializeResultName».put("«field.serializedKeyName»", «simpleName»);
				'''
				case f.extendsType(Enum): '''
					«serializeResultName».put("«field.serializedKeyName»", «simpleName».toString());
				'''
				case serializers.exists [ f.extendsType(key) ]: '''
					«serializeResultName».put("«field.serializedKeyName»", «f.serializer».serialize(«simpleName»));
				'''
				case f.extendsType(Entity): '''
					«serializeResultName».put("«field.serializedKeyName»", «simpleName».serialize());
				'''
			}»
		}
	'''
			
	val static deserializeArgumentName = 'serialized'

	def addDeserializeMethod(MutableClassDeclaration cls, Iterable<? extends FieldDeclaration> fields) {
		cls.addMethod('deserialize') [
			primarySourceElement = cls
			addAnnotation(Override.newAnnotationReference)			
			returnType = cls.newTypeReference
			addParameter(deserializeArgumentName, deserializedMapTypeRef)
			body = ['''
				«fields.map [ 
					'''
						final Object «simpleName» = «deserializeArgumentName».get("«serializedKeyName»");
						if («simpleName» != null) {
							«type.getDeserializationBody('''this.«simpleName» =''', simpleName)»
						}
					'''
				].join('\n')»
				
				return this;
			''']
		]
	}
	
	def CharSequence getDeserializationBody(TypeReference type, String assignment, String valName) '''
		«switch t:type {
			case t.extendsType(String): '''«assignment» «valName».toString();'''
			case t.extendsType(Integer): '''
				if («valName» instanceof Integer) «assignment» (Integer) «valName»;
				else «assignment» Integer.parseInt(«valName».toString());
			'''
			case t.extendsType(Long): '''
				if («valName» instanceof Long) «assignment» (Long) «valName»;
				else this.«valName» = Long.parseLong(«valName».toString());
			'''
			case t.extendsType(Float): '''
				if («valName» instanceof Float) «assignment» (Long) «valName»;
				else «assignment» Float.parseFloat(«valName».toString());
			'''
			case t.extendsType(Double): '''
				if («valName» instanceof Double) «assignment» (Double) «valName»;
				else «assignment» Double.parseDouble(«valName».toString());
			'''
			case t.extendsType(Boolean): '''
				if («valName» instanceof Boolean) «assignment» (Boolean) «valName»;
				else «assignment» Boolean.parseBoolean(«valName».toString());
			'''
			case t.extendsType(Iterable): {
				val entryType = type.actualTypeArguments.head
				'''
					if («valName» instanceof Iterable) {
						final «Function1.newTypeReference(Object.newTypeReference, entryType).cleanTypeName» _function = new «Function1.newTypeReference.cleanTypeName»() {
							public «entryType» apply(final Object entry) {
								«entryType.getDeserializationBody('return', 'entry')»
							}
						};
						«assignment» «IterableExtensions.newTypeReference.name».toList(«IterableExtensions.newTypeReference.name».map((Iterable) «valName», _function));
					}
				'''
			}
			case t.extendsType(Map): {
				val entryTypes = type.actualTypeArguments.get(0) -> type.actualTypeArguments.get(1)
				val entryTypePair = Pair.newTypeReference(entryTypes.key, entryTypes.value)
				'''
					if («valName» instanceof Map) {
						final «Function2.newTypeReference(Object.newTypeReference, Object.newTypeReference, entryTypePair).cleanTypeName» _function = new «Function2.newTypeReference.cleanTypeName»() {
							public «entryTypePair.name» apply(final Object k, final Object v) {
								«entryTypes.key.name» key = null;
								«entryTypes.key.getDeserializationBody('key =', 'k')»
								
								«entryTypes.value.name» value = null;
								«entryTypes.value.getDeserializationBody('value =', 'v')»
								
								return «Pair.newTypeReference.name».of(key, value);
							}
						};
						«assignment» «MapExtensions.newTypeReference.name».map((Map) «valName», _function);
					}
				'''
			}
			case t.extendsType(Enum): '''
				if («valName» instanceof «t.name») «assignment» («t.name») «valName»;
«««					else this.«simpleName» = «field.type».valueOf(«simpleName».toString());
				else «assignment» «EntityExtensions.newTypeReference.name».valueOfCaseInsensitive(«t».class, «valName».toString());
			'''
			case serializers.exists [ t.extendsType(key) ]: '''
				«assignment» «t.serializer».deserialize(«valName»);
			'''
			case t.extendsType(Entity): '''
				«assignment» («t») «EntityExtensions.newTypeReference.name».deserialize((«deserializedMapTypeRef») «valName», «t.name».class);
			'''
		}»
	'''

	def addDeserializeContructor(MutableClassDeclaration cls) {
		cls.addConstructor [
			primarySourceElement = cls
			addParameter(deserializeArgumentName, deserializedMapTypeRef)
			body = ['''
				deserialize(«deserializeArgumentName»);
			''']
		]
	}
	
	def getSerializer(TypeReference fieldType) 
//		'''((«Serializer.newTypeReference(fieldType, Object.newTypeReference).name») «EntityExtensions.newTypeReference.name».get(_serializers, «fieldType.name».class))'''
		'''((«Serializer.newTypeReference(fieldType, Object.newTypeReference).name») «serializersFieldName».get(«fieldType.name».class))'''
	
	def static extendsType(TypeReference type, TypeReference superType) {
		superType.isAssignableFrom(type)
	}
		
	def getSerializedKeyName(extension FieldDeclaration field) {
		simpleName.serializedKeyName
	}

	def getSerializedKeyName(String fieldName) {
		val casing = casing 
		CaseFormat.LOWER_CAMEL.to(casing, fieldName)
	}

//	def getDeserializedKeyName(extension FieldDeclaration field) {
//		val casing = casing 
//		
//		simpleName
//		CaseFormat.LOWER_CAMEL.to(casing, simpleName)
//	}
	

//	val static userDefinedSerializersFieldName = '_userDefinedSerializers'
//	def addSerializers(MutableClassDeclaration cls) {
//		val wildCardRef = newWildcardTypeReference
//		val classTypeRef = Class.newTypeReference(newWildcardTypeReference)
//		val serializerRef = nl.kii.entity.Serializer.newTypeReference(wildCardRef, Object.newTypeReference)
//		
//		val pairRef = Pair.newTypeReference(classTypeRef, serializerRef)
//		//val listSerializersTypeRef = List.newTypeReference(pairRef)
//		val listTypeRef = List.newTypeReference//(pairRef)
//		
//		val userDefinedSerializersField = cls.declaredFields.findFirst [ simpleName == 'serializers' ].option => [
//			simpleName = userDefinedSerializersFieldName
//		]
//		
//		cls.addField('_serializers') [
//			primarySourceElement = cls
//			
////			type = Map.newTypeReference(classTypeRef, serializerRef)
////			initializer = '''
////				«CollectionLiterals.newTypeReference.name».newLinkedHashMap(
////					«FOR s:serializers SEPARATOR ', '»«pairRef.name».of(«
////						s.key.name».class, «s.value»)
////					«ENDFOR»
////				)
////			'''
//			type = listTypeRef
//			val outOfTheBoxSerializersInitBody = '''
//				«CollectionLiterals.newTypeReference.name».newImmutableList(
//					«FOR s:serializers SEPARATOR ', '»«Pair.newTypeReference.simpleName».of(«
//						s.key.name».class, «s.value»)
//					«ENDFOR»
//				)
//			'''
//			
//			initializer = '''
//				«IF userDefinedSerializersField.defined»
//					«IterableExtensions.newTypeReference.name».operator_plus(
//						«CollectionLiterals.newTypeReference.name».newImmutableList(«userDefinedSerializersFieldName»),
//						«outOfTheBoxSerializersInitBody»
//					)
//				«ELSE»
//					«outOfTheBoxSerializersInitBody»
//				«ENDIF»
//			'''
//		]
//	}
	
}
