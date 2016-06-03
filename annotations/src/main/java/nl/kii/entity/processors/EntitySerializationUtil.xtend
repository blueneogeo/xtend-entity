package nl.kii.entity.processors

import com.google.common.base.CaseFormat
import java.util.List
import java.util.Map
import nl.kii.entity.Entity
import nl.kii.entity.EntityExtensions
import nl.kii.entity.Serializer
import nl.kii.entity.annotations.Casing
import nl.kii.util.OptExtensions
import org.eclipse.xtend.lib.macro.TransformationContext
import org.eclipse.xtend.lib.macro.declaration.FieldDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration
import org.eclipse.xtend.lib.macro.declaration.TypeReference

import static extension nl.kii.util.IterableExtensions.*
import static extension nl.kii.util.OptExtensions.*

class EntitySerializationUtil {
	val extension TransformationContext context

	val nativeSerializationTypes = #[
		String,
		Integer,
		Float,
		Long,
		Double
	]

	val outOfTheBoxTypes = nativeSerializationTypes + #[
		Enum,
		Entity
	]
	
	val List<Pair<TypeReference, String>> serializers
	val CaseFormat casing
	
	def getSerializedMapTypeRef() { Map.newTypeReference(String.newTypeReference, Object.newTypeReference) }
	
	def getSupportedTypes() {
		outOfTheBoxTypes.map [ newTypeReference ] + serializers.map [ key ]
	}
	
	new(TransformationContext context, List<Pair<TypeReference, String>> serializers, Casing casing) {
		this.context = context
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
	
	def validateFields(MutableClassDeclaration cls, List<? extends FieldDeclaration> fields) {
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
	def addSerializeMethod(MutableClassDeclaration cls, List<? extends FieldDeclaration> fields) {
		cls.addMethod('serialize') [
			primarySourceElement = cls
			addAnnotation(Pure.newAnnotationReference)
			addAnnotation(Override.newAnnotationReference)			
			returnType = serializedMapTypeRef
			body = ['''
				«serializedMapTypeRef» «serializeResultName» = «CollectionLiterals.newTypeReference.name».newLinkedHashMap();
				
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

	def addDeserializeMethod(MutableClassDeclaration cls, List<? extends FieldDeclaration> fields) {
		cls.addMethod('deserialize') [
			primarySourceElement = cls
			addAnnotation(Override.newAnnotationReference)			
			returnType = cls.newTypeReference
			addParameter(deserializeArgumentName, serializedMapTypeRef)
			body = ['''
				«fields.map [ deserializationBody ].join('\n')»
				
				return this;
			''']
		]
	}
	
	def addDeserializeContructor(MutableClassDeclaration cls) {
		cls.addConstructor [
			primarySourceElement = cls
			addParameter(deserializeArgumentName, serializedMapTypeRef)
			body = ['''
				deserialize(«deserializeArgumentName»);
			''']
		]
	}
	
	def getDeserializationBody(extension FieldDeclaration field) '''
		final Object «simpleName» = «deserializeArgumentName».get("«field.serializedKeyName»");
		if («simpleName» != null) {
			«switch f:field.type {
				case f.extendsType(String): '''this.«simpleName» = «simpleName».toString();'''
				case f.extendsType(Integer): '''
					if («simpleName» instanceof Integer) this.«simpleName» = (Integer) «simpleName»;
					else this.«simpleName» = Integer.parseInt(«simpleName».toString());
				'''
				case f.extendsType(Long): '''
					if («simpleName» instanceof Long) this.«simpleName» = (Long) «simpleName»;
					else this.«simpleName» = Long.parseLong(«simpleName».toString());
				'''
				case f.extendsType(Float): '''
					if («simpleName» instanceof Float) this.«simpleName» = (Long) «simpleName»;
					else this.«simpleName» = Float.parseFloat(«simpleName».toString());
				'''
				case f.extendsType(Double): '''
					if («simpleName» instanceof Double) this.«simpleName» = (Double) «simpleName»;
					else this.«simpleName» = Double.parseDouble(«simpleName».toString());
				'''
				case f.extendsType(Boolean): '''
					if («simpleName» instanceof Boolean) this.«simpleName» = (Boolean) «simpleName»;
					else this.«simpleName» = Boolean.parseBoolean(«simpleName».toString());
				'''
				case f.extendsType(Enum): '''
					if («simpleName» instanceof «field.type.name») this.«simpleName» = («field.type.name») «simpleName»;
«««					else this.«simpleName» = «field.type».valueOf(«simpleName».toString());
					else this.«simpleName» = «EntityExtensions.newTypeReference.name».valueOfCaseInsensitive(«field.type».class, «simpleName».toString());
				'''
				case serializers.exists [ f.extendsType(key) ]: '''
					this.«simpleName» = «f.serializer».deserialize(«simpleName»);
				'''
				case f.extendsType(Entity): '''
					this.«simpleName» = («f») «EntityExtensions.newTypeReference.name».deserialize(«simpleName», «f.name».class);
				'''
			}»
		}
	'''
	
	def getSerializer(TypeReference fieldType) 
//		'''((«Serializer.newTypeReference(fieldType, Object.newTypeReference).name») «EntityExtensions.newTypeReference.name».get(_serializers, «fieldType.name».class))'''
		'''((«Serializer.newTypeReference(fieldType, Object.newTypeReference).name») «serializersFieldName».get(«fieldType.name».class))'''
	
	def static extendsType(TypeReference type, TypeReference superType) {
		superType.isAssignableFrom(type)
	}

	def extendsType(TypeReference type, Class<?> superType) {
		superType.newTypeReference.isAssignableFrom(type)
	}
	
	def getSerializedKeyName(extension FieldDeclaration field) {
		val casing = casing 
		CaseFormat.LOWER_CAMEL.to(casing, simpleName)
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
