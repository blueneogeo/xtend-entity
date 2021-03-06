package nl.kii.entity.processors

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import java.util.List
import java.util.Map
import nl.kii.entity.annotations.Entity
import nl.kii.entity.processors.AccessorsUtil.GetterOptions.CollectionGetterBehavior
import nl.kii.util.Opt
import nl.kii.util.OptExtensions
import org.eclipse.xtend.lib.macro.TransformationContext
import org.eclipse.xtend.lib.macro.declaration.FieldDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration
import org.eclipse.xtend.lib.macro.declaration.TypeReference
import org.eclipse.xtend.lib.macro.declaration.Visibility
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

import static extension nl.kii.util.IterableExtensions.*
import static extension nl.kii.util.OptExtensions.*
import nl.kii.entity.annotations.ToConstructor

class EntityInitializerClassUtil {
	val extension TransformationContext context
	val extension AccessorsUtil accessorsUtil
	val extension EntityProcessor.Util baseUtil
	
	val MutableClassDeclaration initializerClass
	val MutableClassDeclaration entityClass
	
	new(TransformationContext context, MutableClassDeclaration entityInitializerClass, MutableClassDeclaration entityClass) {
		this.context = context
		this.initializerClass = entityInitializerClass
		this.entityClass = entityClass
		
		this.accessorsUtil = new AccessorsUtil(context)
		this.baseUtil = new EntityProcessor.Util(context)
	}
	
	def void populateInitializerClass(Iterable<? extends FieldDeclaration> fields) {
		
		/** Copy fields from Entity into constructor class */
		fields.forEach [ extension field |
			initializerClass.addField(simpleName) [
				type = field.type.wrapperIfPrimitive
				docComment = field.docComment
				field.annotations.forEach [ a | addAnnotation(a) ]
			]
		]
		
		initializerClass => [
			/** Inherit constructor from super entity if necessary */
			if (entityClass.extendsEntity) 
				initializerClass.extendedClass = newTypeReference('''«entityClass.extendedClass.cleanTypeName»Constructor''')
			
			/** Create getters and setters */
			declaredFields.forEach [
				/** Add a 'lenient' getter for API convenience */
				initializerClass.addGetter(it) [ 
					optionals = false 
					collections = CollectionGetterBehavior.setLazily
					mutableCollections = true
				]
				/** Add a normal getter for internal use */
				initializerClass.addGetter(it) [ o | o.getterName = internalGetterName ] 
				
				addSetter(setterType?.toVisibility ?: Visibility.PUBLIC)
			]
			
			/** Create empty constructor in entity constructor class */
			addEmptyConstructor

			/** Create 'entity copy constructor' in entity initializer class to later modify entity */
			addConstructor [ 
				primarySourceElement = initializerClass
				val argName = 'entity'
				addParameter(argName, entityClass.newTypeReference)
				body = '''
					«IF initializerClass.extendedClass.defined && initializerClass.extendedClass != object»
						super(«argName»);
						
					«ENDIF»
					«FOR field : fields»
						this.«field.simpleName» = «
						IF Opt.newTypeReference.isAssignableFrom(entityClass.newTypeReference.findGetter(field).returnType)»«
							OptExtensions.newTypeReference».orNull(«argName».«field.getterName»());«
						ELSE»«
							argName».«field.getterName»();«
						ENDIF»
					«ENDFOR»
				'''
			]
		]
	}
	
	def findGetter(TypeReference cls, FieldDeclaration field) {
		cls.allResolvedMethods
			.map [ declaration ]
			.findFirst [ field.getterName == simpleName ]
	}
	
	def String internalGetterName(FieldDeclaration field) {
		'''_«field.getterName»'''
	}
	
	val static APPLY_CONSTRUCTOR_METHOD_NAME = '_applyConstructorFields'
	def addInitializerFunctionsToEntity(Iterable<? extends FieldDeclaration> fields) {
		val constructorTypeRef = initializerClass.newTypeReference
		
		/** Interal method to apply values from the constructor class on to the entity */
		entityClass.addMethod(APPLY_CONSTRUCTOR_METHOD_NAME) [
			primarySourceElement = entityClass
			visibility = Visibility.PROTECTED
			addParameter('constructor', constructorTypeRef)
			body = '''
				«IF entityClass.extendsEntity»
					super.«APPLY_CONSTRUCTOR_METHOD_NAME»(constructor);
					
				«ENDIF»
				«FOR field : fields SEPARATOR '\n'»
					if (constructor.«field.internalGetterName»() != null)
						this.«field.simpleName» = «IF List.newTypeReference.isAssignableFrom(field.type)»
							«ImmutableList».copyOf(constructor.«field.internalGetterName»());
						«ELSEIF Map.newTypeReference.isAssignableFrom(field.type)»
							«ImmutableMap».copyOf(constructor.«field.internalGetterName»());
						«ELSE»
							constructor.«field.internalGetterName»();
						«ENDIF»
				«ENDFOR»
			'''
		]
		
		/** Add constructor with constructor class argument to entity */
		entityClass.addConstructor [
			primarySourceElement = entityClass
			val argName = 'constructor'
			addParameter(argName, constructorTypeRef)
			body = '''
				«APPLY_CONSTRUCTOR_METHOD_NAME»(constructor);
			'''
		]
		
		val constructorOptionsTypeRef = newTypeReference(nl.kii.entity.Procedure1, constructorTypeRef)
		
		/** Add constructor with constructor class options argument to entity */
		entityClass.addConstructor [
			primarySourceElement = entityClass
			val argName = 'constructorOptions'
			addParameter(argName, constructorOptionsTypeRef)
			body = '''
				«initializerClass» constructor = new «initializerClass»();
				«argName».apply(constructor);
				«APPLY_CONSTRUCTOR_METHOD_NAME»(constructor);
			'''
		]
		
		val requiredFields = fields.requiredFields
		if (!requiredFields.empty) {
			/** Add constructor with required fields and constructor class options as args to entity */
			entityClass.addConstructor [
				primarySourceElement = entityClass
				
				requiredFields.forEach [ f | addParameter(f.simpleName, f.type) ]
				
				val argName = 'constructorOptions'
				addParameter(argName, constructorOptionsTypeRef)
				
				body = '''
					«initializerClass» constructor = new «initializerClass»();
					«FOR f:requiredFields»
						constructor.«f.setterName»(«f.simpleName»);
					«ENDFOR»
					«argName».apply(constructor);
					«APPLY_CONSTRUCTOR_METHOD_NAME»(constructor);
				'''
			]
			
//			/** Add constructor with just required fields as args to entity (if it doesn't confict with the already present serialized map constructor) */
//			if (requiredFields.size != 1 || (!Map.newTypeReference.isAssignableFrom(requiredFields.head.type)))
//				entityClass.addConstructor [
//					primarySourceElement = entityClass
//					
//					requiredFields.forEach [ f | addParameter(f.simpleName, f.type) ]
//					
//					body = ['''
//						«initializerClass.qualifiedName» constructor = new «initializerClass.qualifiedName»();
//						«FOR f:requiredFields»
//							constructor.«f.setterName»(«f.simpleName»);
//						«ENDFOR»
//						applyConstructorFields(constructor);
//					''']
//				]
		}
	}
	
	def addMutationFunctionsToEntity(Iterable<? extends FieldDeclaration> fields) {
		val constructorTypeRef = initializerClass.newTypeReference
		val constructorOptionsTypeRef = nl.kii.entity.Procedure1.newTypeReference(constructorTypeRef)
		val pureAnnotationTypeRef = Pure.newAnnotationReference		
		val entityTypeRef = entityClass.newTypeReference
		
		/** Method to immutably modify entity, returning a new instance of the entity */
		entityClass.addMethod('mutate') [
			primarySourceElement = entityClass
			val argName = 'mutation'
			addParameter(argName, constructorOptionsTypeRef)
			returnType = entityTypeRef
			addAnnotation(pureAnnotationTypeRef)
			body = '''
				«initializerClass» constructor = new «initializerClass»(this);
				«argName».apply(constructor);
				
				return new «entityClass»(constructor);
			'''	
		]
		
		/** Operator shortcut for 'mutate' method */
		entityClass.addMethod('operator_doubleGreaterThan') [
			primarySourceElement = entityClass
			val argName = 'mutation'
			addParameter(argName, constructorOptionsTypeRef)
			returnType = entityTypeRef
			addAnnotation(pureAnnotationTypeRef)
			body = '''
				return mutate(mutation);
			'''	
		]
	}
	
	
	def addConvenienceProcedureInitializer() {
		if (entityClass.abstract) return
		
		val constructorTypeRef = initializerClass.newTypeReference
		
		entityClass.implementedInterfaces = entityClass.implementedInterfaces + Procedure1.newTypeReference(constructorTypeRef)
		
		entityClass.addMethod('apply') [
			addParameter('constructor', constructorTypeRef)
			body = ''' '''
		]
		
		/** Add empty constructor that applies constructing procedure in case {@code apply} is overridden  */
		entityClass.addConstructor [
			primarySourceElement = entityClass
			body = '''
				«initializerClass» constructor = new «initializerClass»();
				apply(constructor);
				«APPLY_CONSTRUCTOR_METHOD_NAME»(constructor);
			'''
		]
	}
	
	def void addConvenienceNestedEntitySetters() {
		initializerClass.declaredFields
			.filter [ type.defined && (type.extendsType(Procedure1) || findClass(type.type.qualifiedName)?.findAnnotation(Entity.newTypeReference.type).defined) ]
			.forEach [ field |
				initializerClass.addMethod(field.simpleName) [ 
					val argName = field.simpleName
					
					addParameter(argName, field.type)
					body = '''
						«field.setterName»(«argName»);
					'''
				]
		]
	}
	
	def void addStaticRequiredFieldsInitializer() {
		if (entityClass.abstract) return
		val requiredFields = entityClass.declaredFields.requiredFields
		if (!requiredFields.empty) entityClass.addMethod('with') [
			static = true
			requiredFields.forEach [ f | addParameter(f.simpleName, f.type) ]
			returnType = entityClass.newTypeReference
			body = '''
				«initializerClass» constructor = new «initializerClass»();
				«FOR f:requiredFields»
					constructor.«f.setterName»(«f.simpleName»);
				«ENDFOR»
				return new «entityClass»(constructor);
			'''
		]
	}
	
	def void moveAnnotatedMethodsToConstrucor() {
		entityClass.declaredMethods
			.filter [ findAnnotation(ToConstructor.newAnnotationReference.annotationTypeDeclaration).defined ]
			//.filter [ returnType == primitiveVoid && parameters.size == 1 && simpleName.isSetter ]
			.forEach [ 
				copyTo(initializerClass)
				remove
			]
	}
	
}

