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
import org.eclipse.xtend.lib.macro.declaration.MutableFieldDeclaration
import org.eclipse.xtend.lib.macro.declaration.TypeReference
import org.eclipse.xtend.lib.macro.declaration.Visibility
import org.eclipse.xtext.xbase.lib.Functions.Function1
import org.eclipse.xtext.xbase.lib.Functions.Function2

import static extension nl.kii.entity.processors.EntityProcessor.Util.*
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
	val extension AccessorsUtil accessorsUtil
	
	new(TransformationContext context, List<Pair<TypeReference, String>> serializers, Casing casing) {
		this.context = context
		this.baseUtil = new Util(context)
		this.accessorsUtil = new AccessorsUtil(context)
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
				
		supportedTypes.exists [ type.extendsType(it) ] || 
		findEnumerationType(type.name).defined /** <-- workaround for compiler not recognizing enums in same source file */ ||
		{
			val cls = findClass(type.name) /** <-- workaround for compiler not recognizing entities in same source file */
			cls.defined && cls.annotations.exists [ annotationTypeDeclaration == nl.kii.entity.annotations.Entity.newAnnotationReference.annotationTypeDeclaration ]
		}
	}
	
	def validateFields(MutableClassDeclaration cls, Iterable<? extends FieldDeclaration> fields) {
		fields
			.map [ it -> type.actualTypeArguments.concat(type).list ]
			.flattenValues
			.filter [ !value.isSupported ]
			.list
			.forEach [
				key.addError('''Fields of type «value.name» cannot be serialized, add a Serializer to make the type compatible''')
			]
	}

	val static serializersName = 'serializers'
	val static String serializersFieldName = serializersName.hiddenName
	val static String serializersGetterName = 'getSerializers'.hiddenName
	
	def MutableFieldDeclaration addSerializers(MutableClassDeclaration cls) {
		//if (serializers.nullOrEmpty) return null
		val wildCardRef = newWildcardTypeReference
		val classTypeRef = Class.newTypeReference(wildCardRef)
		val serializerRef = Serializer.newTypeReference(wildCardRef, object)
		
		val pairRef = Pair.newTypeReference
		
		cls.addField(serializersName) [
			primarySourceElement = cls
			
			type = Map.newTypeReference(classTypeRef, serializerRef)
			initializer = '''
				«nl.kii.util.IterableExtensions.newTypeReference.name».toMap(
					«CollectionLiterals.newTypeReference.name».newImmutableList(
						«FOR s:serializers SEPARATOR ', '»
							«pairRef.name».of(«s.key.name».class, «s.value»)
						«ENDFOR»					
					)
				)
			'''
			
			cls.addGetter(it, false) => [ m |
				m.visibility = Visibility.PROTECTED
				
				m.simpleName = simpleName.hiddenName
				simpleName = simpleName.hiddenName
			]
			
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
			body = '''
				«mapTypeRef» «serializeResultName» = «CollectionLiterals.newTypeReference.name».newLinkedHashMap();
				«IF cls.extendsEntity»
					«serializeResultName».putAll(super.serialize());
				«ENDIF»	
				
				«fields.map [ 
					'''
						final «type» «simpleName» = this.«simpleName»;
						if («OptExtensions.newTypeReference.name».defined(«simpleName»)) {
							«type.getSerializationBody('''«object» serializedValue =''', '''«simpleName»''')»
							«serializeResultName».put("«serializedKeyName»", serializedValue);
						}
					'''
				].join('\n')»
				
				return serialized;
			'''
		]
	}	
	
	def CharSequence getSerializationBody(TypeReference type, String assignment, String valName) '''
		«switch t:type {
			case t.extendsType(Iterable): {
				val entryType = t.actualTypeArguments.head
				'''
					final «Function1.newTypeReference(entryType, object).cleanTypeName» _function = new «Function1.newTypeReference(entryType, object).name.replace('$', '.')»() {
						public «object» apply(final «entryType» entry) {
							«entryType.getSerializationBody('return', 'entry')»
						}
					};
					«assignment» «IterableExtensions.newTypeReference.name».toList(«IterableExtensions.newTypeReference.name».map((Iterable) «valName», _function));
				'''
			}
			case t.extendsType(Map): {
				val entryTypes = type.actualTypeArguments.get(0) -> type.actualTypeArguments.get(1)
				val returnPair = Pair.newTypeReference(object, object)
				'''
					final «Function2.newTypeReference.cleanTypeName» _function = new «Function2.newTypeReference(entryTypes.key, entryTypes.value, returnPair).name.replace('$', '.')»() {
						public «returnPair.name» apply(final «entryTypes.key» k, final «entryTypes.value» v) {
							«entryTypes.key.getSerializationBody('''«object» key =''', 'k')»
							
							«entryTypes.value.getSerializationBody('''«object» value =''', 'v')»
							
							return «Pair.newTypeReference.name».of(key, value);
						}
					};
					«assignment» «MapExtensions.newTypeReference.name».map((Map) «valName», _function);
				'''
			}
			case nativeSerializationTypes.exists [ t.extendsType(it) ]: '''
				«assignment» «valName»;
			'''
			case t.extendsType(Enum): '''
				«assignment» «valName».toString();
			'''
			case serializers.exists [ t.extendsType(key) ]: '''
				«assignment» «t.serializer».serialize(«valName»);
			'''
			case t.extendsType(Entity): '''
				«assignment» «valName».serialize();
			'''
		}»
	'''
			
	val static deserializeArgumentName = 'serialized'

	def addDeserializeMethod(MutableClassDeclaration cls, Iterable<? extends FieldDeclaration> fields) {
		cls.addMethod('deserialize') [
			primarySourceElement = cls
			addAnnotation(Override.newAnnotationReference)			
			returnType = cls.newTypeReference
			addParameter(deserializeArgumentName, deserializedMapTypeRef)
			body = ['''
				«IF cls.extendsEntity»
					super.deserialize(«deserializeArgumentName»);
					
				«ENDIF»
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
				else «assignment» Long.parseLong(«valName».toString());
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
