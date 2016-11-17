package nl.kii.entity.processors

import com.google.common.base.CaseFormat
import nl.kii.entity.Casing
import nl.kii.entity.Entity
import nl.kii.entity.annotations.Field
import nl.kii.entity.annotations.Ignore
import nl.kii.entity.annotations.Require
import nl.kii.entity.annotations.Serializer
import nl.kii.entity.annotations.Type
import org.eclipse.xtend.lib.annotations.Accessors
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor
import org.eclipse.xtend.lib.annotations.ToStringConfiguration
import org.eclipse.xtend.lib.annotations.ToStringProcessor
import org.eclipse.xtend.lib.macro.AbstractClassProcessor
import org.eclipse.xtend.lib.macro.RegisterGlobalsContext
import org.eclipse.xtend.lib.macro.TransformationContext
import org.eclipse.xtend.lib.macro.declaration.ClassDeclaration
import org.eclipse.xtend.lib.macro.declaration.FieldDeclaration
import org.eclipse.xtend.lib.macro.declaration.MemberDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration
import org.eclipse.xtend.lib.macro.declaration.TypeReference
import org.eclipse.xtend.lib.macro.declaration.Visibility

import static extension nl.kii.entity.processors.AccessorsUtil.*
import static extension nl.kii.entity.processors.EntityProcessor.Util.*
import static extension nl.kii.util.IterableExtensions.*
import static extension nl.kii.util.OptExtensions.*

/** 
 * Active Annotation Processor for Entity annotations.
 * @see Entity
 */
class EntityProcessor extends AbstractClassProcessor {
	
	override doRegisterGlobals(ClassDeclaration cls, extension RegisterGlobalsContext context) {
		context.registerClass(cls.entityFieldsClassName)
		context.registerClass(cls.entityInitializerClassName)
	}
	
	def static String getEntityInitializerClassName(ClassDeclaration cls) '''«cls.qualifiedName»Constructor'''
	def static String getEntityFieldsClassName(ClassDeclaration cls) '''«cls.qualifiedName».Fields'''
	def static String getEntityFieldsClassName(TypeReference cls) '''«cls.name»$Fields'''
		
	override doTransform(MutableClassDeclaration cls, extension TransformationContext context) {
		val extension baseUtil = new Util(context)
		val extension accessorsUtil = new AccessorsUtil(context)
		val extension toStringUtil = new ToStringProcessor.Util(context)
		val extension equalsHashCodeUtil = new EntityEqualsHashCodeUtil(context)
		val extension fieldValidationUtil = new FieldValidationUtil(context)
		val initializerClass = findClass(cls.entityInitializerClassName)
		val extension entityInitializerClassUtil = new EntityInitializerClassUtil(context, initializerClass, cls)
		
		cls.primarySourceElement = cls
		
		val entityAnnotation = cls.findAnnotation(nl.kii.entity.annotations.Entity.newTypeReference.type)
		
		/** Implement Entity interface. */
		if (!cls.implementedInterfaces.list.contains(Entity.newTypeReference))
			cls.implementedInterfaces = cls.implementedInterfaces + Entity.newTypeReference
		
		/** Figure out which fields need accessors added. */
		//val localAccessorsFields = cls.declaredFields.accessorsFields
		val accessorsFields = cls.declaredFields.accessorsFields
		//cls.docComment = '''-- «cls.newTypeReference.allDeclaredFields.map[simpleName]»'''
		
		val serializeFields = accessorsFields.serializeFields
		
		/** Figure out which fields are marked with @Require. */
		val requiredFields = serializeFields.requiredFields
		
//			/** Copy fields declared in super types, to be able to make them private */
//			accessorsFields
//				.filter [ f | !localAccessorsFields.exists [ f.simpleName == simpleName ] ]
//				.forEach [ copyTo(cls) ]
			
		/** Add getters for fields that need them. If @Entity.optionals is set to true, wrap them in an Opt, except for the required fields. */
		val entityNeedsOptionals = entityAnnotation.getBooleanValue('optionals') && !cls.abstract
		if (entityNeedsOptionals) {
			cls.addGetters(requiredFields, false)
			cls.addGetters(accessorsFields, true)
		} else {
			cls.addGetters(accessorsFields, false)
		}
		
		/** Setup the generated EntityConstructor class, by copying accessors and fields from Entity class. */
		populateInitializerClass(accessorsFields)
		
		/** Integrate generated EntityConstructor class into the Entity class. */
		addInitializerFunctionsToEntity(accessorsFields)
		if (!cls.abstract) addMutationFunctionsToEntity(accessorsFields)
		
		addConvenienceProcedureInitializer
		addConvenienceNestedEntitySetters		
		
		if (cls.abstract) cls.addEmptyConstructor //else cls.final = true
		
		
		val serializerTypeRef = Serializer.newAnnotationReference
		val serializerTypeDeclaration = serializerTypeRef.annotationTypeDeclaration
		
		val declaredSerializerFields = cls.declaredFields.filter [ 
			findAnnotation(serializerTypeDeclaration).defined
		].list
		
		declaredSerializerFields.forEach [ f |
			addGetter(f, Visibility.PROTECTED)
			cls.findDeclaredMethod(f.getterName).option => [
				val fieldAnnotation = f.annotations.findFirst [ annotationTypeDeclaration == serializerTypeDeclaration ]
				addAnnotation(fieldAnnotation)
				
				f.simpleName = f.simpleName.hiddenName
				simpleName = simpleName.hiddenName
			]
		]

		val allSerializers = cls.newTypeReference.allResolvedMethods
			.map [ 
				declaration.findAnnotation(serializerTypeDeclaration)?.getClassValue('value') -> 
				'''«IF declaration.declaringType.newTypeReference != cls.newTypeReference»super.«ENDIF»«declaration.simpleName»()'''
			]
			.filter [ key.defined ]
			.toList
			.reverse
			.list
		
		val globalCasing = Casing.valueOf(entityAnnotation.getEnumValue('casing').simpleName)
		
		val entityFieldSignatures = serializeFields.map [
			new EntityFieldSignature => [ s |
				s.declaration = it
				s.name = simpleName
				s.type = type
				s.required = findAnnotation(Require.newTypeReference.type).defined
				s.serializedName = getSerializedName(globalCasing)
			]
		]
		
		val computedFieldSignatures = cls.declaredMethods
			.filter [ findAnnotation(Field.newTypeReference.type).defined ]
			.map [ 
				new EntityFieldSignature => [ s |
					s.declaration = it
					s.name = simpleName
					s.required = false
					s.type = returnType
					val fieldName = if (simpleName.isGetter) simpleName.fieldName else simpleName
					s.serializedName = getSerializedName(fieldName, globalCasing)
				]
			]
		
		if (!computedFieldSignatures.empty) #[ 
				[ EntityFieldSignature it | declaration.simpleName ],
				[ EntityFieldSignature it | name ],
				[ EntityFieldSignature it | serializedName ]
			]
			.forEach [ grouping |
				(computedFieldSignatures + entityFieldSignatures)
					.groupBy(grouping)
					.toPairs
					.filter [ value.size > 1 ]
					.forEach [ group |
						group.value.forEach [ declaration.addError('Field name ambiguity: ' + group.key) ]
					]
			]
		
		val extension serializationUtil = new EntitySerializationUtil(context, allSerializers)
		
		cls.addSerializers
		//if (cls.needsSerializing) 
		cls => [
			addDeserializeMethod(entityFieldSignatures)
			//if (!cls.abstract) 
			addDeserializeContructor
			addSerializeMethod(entityFieldSignatures + computedFieldSignatures)
		]
		
		/** Add static 'Fields' class to the entity, that contains field reflection data */
		val extension reflectionUtil = new EntityReflectionUtil(context)
		
		val fieldsClass = findClass(cls.entityFieldsClassName)
		fieldsClass.populateFieldsClass(entityFieldSignatures)
		cls.addFieldsGetter(fieldsClass)

		if (cls.extendsEntity) 
			fieldsClass.extendedClass = cls.extendedClass.getEntityFieldsClassName.newTypeReference
		
		/** Find @Type annotated field */
		val typeFields = cls.declaredFields.filter [ findAnnotation(Type.newTypeReference.type).defined ]
		cls.addValidationMethod(requiredFields, switch size:typeFields.size {
			case 1: {
				val field = typeFields.head
				
				if (field.type != string) 
					cls.addError('@Type field must be a String')
				
				val defaultTypeName = cls.simpleName.apply(globalCasing)
				if (!field.initializer.defined) 
					field.initializer = '''"«defaultTypeName»"'''
				
				field -> (field.initializer?.toString?.replace('\'', '') ?: defaultTypeName)
			}
			case size > 1: {
				cls.addError('There can only be one field marked with @Type')
				null				
			}
		})	

		
		/** Generate a nice toString, equals and hashCode. */
		if (cls.needsToStringEqualsHashCode) cls => [
			if (!hasToString) addToString(serializeFields, new ToStringConfiguration(true, false, false, false))
			if (!hasEquals) addEquals(serializeFields, false)
			if (!hasHashCode) addHashCode(serializeFields, false)
		]
		
		/** Validate fields for serializability */
		cls.validateFields(entityFieldSignatures)
		
		/** Validate in case of entity extending that the extended entity is marked abstract */
		if (cls.extendsEntity && cls.extendedClass.declaredResolvedMethods.exists [ declaration.simpleName == 'mutate' ]) /** Workaround to find detect abstract super type */
			cls.addError('Extended Entity class must be marked abstract.')
		
	}
	
	def needsToStringEqualsHashCode(ClassDeclaration cls) {
		!cls.abstract
	}
	
	@Accessors
	static class EntityFieldSignature {
		String name
		String serializedName
		TypeReference type
		boolean required
		MemberDeclaration declaration
	}
	
	@FinalFieldsConstructor
	static class Util {
		val extension TransformationContext context
				
		def static extendsType(TypeReference type, TypeReference superType) {
			superType.isAssignableFrom(type)
		}
	
		def Boolean extendsType(TypeReference type, Class<?> superType) {
			superType.newTypeReference.isAssignableFrom(type)
		}
		
		def extendsEntity(ClassDeclaration cls) {
			cls.extendedClass?.extendsType(Entity)
		}
		
		def static apply(String input, Casing casing) {
			switch casing {
				case underscore, case snake:			[ CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, it) ]
				case camel, case lowerCamel:			[ CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_CAMEL, it) ]
				case dash, case hyphen: 				[ CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, it) ] 
				case upperCamel: 						[ CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, it) ]
				case upperUnderscore, case upperSnake: 	[ CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, it) ]
				case dot: 								[ CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, it).replace('-', '.')  ]
				default: [ it ]
			}.apply(input)
		}
		
		/**
		 * Look for @Field annotation in {@code delcaration} or else fall back to global casing.
		 */
		def getSerializedName(MemberDeclaration declaration, String name, Casing globalCasing) {
			val fieldAnnotation = declaration.findAnnotation(Field.newTypeReference.type)
			
			val result = if (fieldAnnotation.defined) {
				val nameValue = fieldAnnotation.getStringValue('name')
				if (!nameValue.nullOrEmpty) nameValue
				else {
					val casingValue = Casing.valueOf(fieldAnnotation.getEnumValue('casing').simpleName)
					if (casingValue != Casing.ignore) name.apply(casingValue)
				}
			} 
			
			result ?: name.apply(globalCasing)
		}
		
		def getSerializedName(MemberDeclaration declaration, Casing globalCasing) {
			getSerializedName(declaration, declaration.simpleName, globalCasing)
		}
		
//		def allDeclaredFields(ClassDeclaration cls) {
//			cls.allDeclaredFields(#[])
//		}
//
//		def private Iterable<? extends FieldDeclaration> allDeclaredFields(ClassDeclaration cls, Iterable<? extends FieldDeclaration> fields) {
//			val result = fields + cls.declaredFields.filter [ f | !fields.exists [ f.simpleName == simpleName ] ]
//			if (!cls.extendsEntity) result
//			else {
//				cls.newTypeReference.declaredResolvedMethods
//				cls.extendedClass.name.findClass.allDeclaredFields(result)
//				
//			}
//		}
		
		def <T extends FieldDeclaration> getAccessorsFields(Iterable<T> fields) {
			fields.filter [
				!static && !volatile && !simpleName.startsWith('_')
			]
		}
		
		/** Figure out which fields also need to be serialized, by filtering out the fields marked with @Ignore. */
		def <T extends FieldDeclaration> getSerializeFields(Iterable<T> fields) {
			fields.filter [
				!transient && !findAnnotation(Ignore.newTypeReference.type).defined
			]
		}
		
		def <T extends FieldDeclaration> getRequiredFields(Iterable<T> fields) {
			fields.filter [
				findAnnotation(Require.newTypeReference.type).defined
			].list
		}

		def cleanTypeName(TypeReference typeRef) {
			typeRef.name.replaceAll('<.+>', '').replaceAll('\\$', '.')
		}	
			
		def copyTo(FieldDeclaration field, MutableClassDeclaration cls) {
			cls.addField(field.simpleName) [
				final = field.final
				static = field.static
				transient = field.transient
				volatile = field.volatile
				initializer = field.initializer
				type = field.type
				docComment = field.docComment
				visibility = field.visibility
				deprecated = field.deprecated
				field.annotations.forEach [ a |
					addAnnotation(a)
				]
			]
		}
		
		def addEmptyConstructor(MutableClassDeclaration cls) {
			cls.addConstructor [
				primarySourceElement = cls
				body = [''' ''']
			] 
		}
		
		def static String getHiddenName(String name) '''_«name»'''
			
}
	
	
		/** Add constructor for all fields */
//			if (cls.needsFinalFieldConstructor || cls.findAnnotation(FinalFieldsConstructor.findTypeGlobally) !== null) {
//				cls.addFinalFieldsConstructor
//			}
//		val extension constructorUtil = new ConstructorUtil(context) [ 
//			declaredFields.filter [
//				!static && !volatile && !simpleName.startsWith('_')
//			] 
//		]
//		val extension equalsHashCodeUtil = new EqualsHashCodeProcessor.Util(context)
//		val extension requiredArgsUtil = new FinalFieldsConstructorProcessor.Util(context)			
	

//		val userDefinedSerializers = cls.declaredFields.findFirst [ simpleName == 'serializers' ]?. ?: emptyList
//			cls.declaredMethods
//			.filter [ simpleName == 'serializer' && parameters.size == 1 && parameters.head.type == ]
//			.map [ parameters.head -> body ]
//			.list

//		val List<Pair<TypeReference, String>> serializers = #[
//			Date.newTypeReference -> '''new «DateSerializer.newTypeReference.name»("yyyy-MM-dd")'''
//		]

	
}


//	
//class ConstructorUtil {
//		extension TransformationContext context
//		val (MutableTypeDeclaration)=>Iterable<? extends MutableFieldDeclaration> fieldsInConstructor
//
//		new(TransformationContext context, (MutableTypeDeclaration)=>Iterable<? extends MutableFieldDeclaration> fieldsInConstructor) {
//			this.context = context
//			this.fieldsInConstructor = fieldsInConstructor
//		}
//
//		def getFields(MutableTypeDeclaration it) {
//			fieldsInConstructor.apply(it)
//		}
//
//		def needsFieldConstructor(MutableClassDeclaration it) {
//			!hasSameFieldsConstructor
//			&& (primarySourceElement as ClassDeclaration).declaredConstructors.isEmpty
//		}
//
//		def hasSameFieldsConstructor(MutableTypeDeclaration cls) {
//			val expectedTypes = cls.fieldsConstructorArgumentTypes
//			cls.declaredConstructors.exists [
//				parameters.map[type].toList == expectedTypes
//			]
//		}
//		
//		def getFieldsConstructorArgumentTypes(MutableTypeDeclaration cls) {
//			val types = newArrayList
//			if (cls.superConstructor !== null) {
//				types += cls.superConstructor.resolvedParameters.map[resolvedType]
//			}
//			types += cls.fields.map[type]
//			types
//		}
//		
//		def String getConstructorAlreadyExistsMessage(MutableTypeDeclaration it) {
//			'''Cannot create FieldsConstructor as a constructor with the signature "new(«fieldsConstructorArgumentTypes.join(",")»)" already exists.'''
//		}
//
//		def addFinalFieldsConstructor(MutableClassDeclaration it) {
//			addConstructor [
//				primarySourceElement = declaringType.primarySourceElement
//				makeFieldsConstructor
//			]
//		}
//		
//		static val EMPTY_BODY = Pattern.compile("(\\{(\\s*\\})?)?")
//
//		def makeFieldsConstructor(MutableConstructorDeclaration it) {
////			if (declaringType.fieldsConstructorArgumentTypes.empty) {
////				val anno = findAnnotation(FinalFieldsConstructor.findTypeGlobally)
////				anno.addWarning('''There are no final fields, this annotation has no effect''')
////				return
////			}
//			if (declaringType.hasSameFieldsConstructor) {
//				addError(declaringType.constructorAlreadyExistsMessage)
//				return
//			}
//			if (!parameters.empty) {
//				addError('Parameter list must be empty')
//			}
//			if (body !== null && !EMPTY_BODY.matcher(body.toString).matches) {
//				addError('Body must be empty')
//			}
//			val superParameters = declaringType.superConstructor?.resolvedParameters ?: #[]
//			superParameters.forEach [ p |
//				addParameter(p.declaration.simpleName, p.resolvedType)
//			]
//			val fieldToParameter = newHashMap
//			declaringType.fields.forEach [ p |
//				p.markAsInitializedBy(it)
//				val param = addParameter(p.simpleName, p.type.orObject)
//				fieldToParameter.put(p, param)
//			]
//			body = '''
//				super(«superParameters.join(',')[declaration.simpleName]»);
//				«FOR arg : declaringType.fields»
//					this.«arg.simpleName» = «fieldToParameter.get(arg).simpleName»;
//				«ENDFOR»
//			'''
//		}
//
//		def getSuperConstructor(TypeDeclaration it) {
//			if (it instanceof ClassDeclaration) {
//				if (extendedClass == object || extendedClass == null)
//					return null;
//				return extendedClass.declaredResolvedConstructors.head
//			} else {
//				return null
//			}
//		}
//		
//		private def orObject(TypeReference ref) {
//			if (ref === null) object else ref
//		}
//	
//}

