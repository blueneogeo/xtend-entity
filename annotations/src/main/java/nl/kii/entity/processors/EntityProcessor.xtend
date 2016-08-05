package nl.kii.entity.processors

import nl.kii.entity.Casing
import nl.kii.entity.Entity
import nl.kii.entity.annotations.Ignore
import nl.kii.entity.annotations.Require
import nl.kii.entity.annotations.Serializer
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor
import org.eclipse.xtend.lib.annotations.ToStringConfiguration
import org.eclipse.xtend.lib.annotations.ToStringProcessor
import org.eclipse.xtend.lib.macro.AbstractClassProcessor
import org.eclipse.xtend.lib.macro.RegisterGlobalsContext
import org.eclipse.xtend.lib.macro.TransformationContext
import org.eclipse.xtend.lib.macro.declaration.ClassDeclaration
import org.eclipse.xtend.lib.macro.declaration.FieldDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration
import org.eclipse.xtend.lib.macro.declaration.TypeReference

import static extension nl.kii.util.IterableExtensions.*
import static extension nl.kii.util.OptExtensions.*

/** 
 * Active Annotation Processor for Entity annotations.
 * @see Entity
 */
class EntityProcessor extends AbstractClassProcessor {
	
	override doRegisterGlobals(ClassDeclaration cls, extension RegisterGlobalsContext context) {
		context.registerClass(cls.entityFieldsClassName)
		
		if (cls.needsInitializerClass)
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
		val initializerClass = findClass(cls.entityInitializerClassName)
		val extension entityInitializerClassUtil = new EntityInitializerClassUtil(context, initializerClass, cls)
		
		cls.primarySourceElement = cls
		
		val entityAnnotation = cls.findAnnotation(nl.kii.entity.annotations.Entity.newTypeReference.type)
		
		/** Implement Entity interface. */
		if (!cls.implementedInterfaces.list.contains(Entity.newTypeReference))
			cls.implementedInterfaces = cls.implementedInterfaces + Entity.newTypeReference
		
		/** Figure out which fields need accessors added. */
		val localAccessorsFields = cls.declaredFields.accessorsFields
		val accessorsFields = cls.allDeclaredFields.accessorsFields
		
		val localSerializeFields = localAccessorsFields.serializeFields
		val serializeFields = accessorsFields.serializeFields
		
		/** Figure out which fields are marked with @Required. */
		val requiredFields = serializeFields.filter [
			findAnnotation(Require.newTypeReference.type).defined
		].list
		
		if (cls.needsAccessors) {
			/** Copy fields declared in super types, to be able to make them private */
			accessorsFields
				.filter [ f | !localAccessorsFields.exists [ f.simpleName == simpleName ] ]
				.forEach [ copyTo(cls) ]
			
			/** Add getters for fields that need them. If @Entity.optionals is set to true, wrap them in an Opt, except for the required fields. */
			val entityNeedsOptionals = entityAnnotation.getBooleanValue('optionals')
			if (entityNeedsOptionals) {
				cls.addGetters(requiredFields, false)
				cls.addGetters(accessorsFields, true)
			} else {
				cls.addGetters(accessorsFields, false)
			}			
		}
		
		if (cls.needsInitializerClass) {
			/** Setup the generated EntityConstructor class, by copying accessors and fields from Entity class. */
			populateInitializerClass(accessorsFields)
			
			/** Integrate generated EntityConstructor class into the Entity class. */
			addInitializerFunctionsToEntity(accessorsFields)

			addConvenienceProcedureInitializer
			addConvenienceNestedEntitySetters		
		}
		
		if (cls.abstract) cls.addEmptyConstructor //else cls.final = true
				
		val serializerTypeRef = Serializer.newAnnotationReference.annotationTypeDeclaration
		val userDefinedSerializersFields = cls.declaredFields.filter [ 
			findAnnotation(serializerTypeRef).defined
		].list
		
		userDefinedSerializersFields.forEach [
			simpleName = '''_«simpleName»'''
		]
		
		val serializers = userDefinedSerializersFields.map [ 
			findAnnotation(serializerTypeRef).newAnnotationReference.getClassValue('value') -> simpleName
		].list
		
		val casing = Casing.valueOf(entityAnnotation.getEnumValue('casing').simpleName)
		val extension serializationUtil = new EntitySerializationUtil(context, serializers, casing)		
		
		if (cls.needsSerializing) cls => [
			addSerializers
			addDeserializeMethod(serializeFields)
			if (!cls.abstract) addDeserializeContructor
			addSerializeMethod(serializeFields)
		]
		
		/** Add static 'Fields' class to the entity, that contains field reflection data */
		val extension reflectionUtil = new EntityReflectionUtil(context) [ getSerializedKeyName ]
		
		val fieldsClass = findClass(cls.entityFieldsClassName)
		fieldsClass.populateFieldsClass(localSerializeFields)
		cls.addFieldsGetter(fieldsClass)

		if (cls.extendsEntity) 
			fieldsClass.extendedClass = cls.extendedClass.getEntityFieldsClassName.newTypeReference

		
		/** Generate a nice toString, equals and hashCode. */
		if (cls.needsToStringEqualsHashCode) cls => [
			if (!hasToString) addToString(serializeFields, new ToStringConfiguration(true, false, false, false))
			if (!hasEquals) addEquals(serializeFields, false)
			if (!hasHashCode) addHashCode(serializeFields, false)
		]
		
		cls.validateFields(localSerializeFields)		
	}
	
	def needsInitializerClass(ClassDeclaration cls) {
		!cls.abstract
	}
	
	def needsAccessors(ClassDeclaration cls) {
		!cls.abstract
	}

	def needsToStringEqualsHashCode(ClassDeclaration cls) {
		!cls.abstract
	}

	def needsSerializing(ClassDeclaration cls) {
		!cls.abstract
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
		
		def allDeclaredFields(ClassDeclaration cls) {
			cls.allDeclaredFields(#[])
		}

		def Iterable<? extends FieldDeclaration> allDeclaredFields(ClassDeclaration cls, Iterable<? extends FieldDeclaration> fields) {
			val result = fields + cls.declaredFields.filter [ f | !fields.exists [ f.simpleName == simpleName ] ]
			if (!cls.extendsEntity) result
			else cls.extendedClass.name.findClass.allDeclaredFields(result)
		}
		
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

