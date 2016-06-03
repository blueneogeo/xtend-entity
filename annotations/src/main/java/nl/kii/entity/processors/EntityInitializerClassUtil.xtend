package nl.kii.entity.processors

import nl.kii.entity.Entity
import nl.kii.util.Opt
import nl.kii.util.OptExtensions
import org.eclipse.xtend.lib.annotations.AccessorsProcessor
import org.eclipse.xtend.lib.macro.TransformationContext
import org.eclipse.xtend.lib.macro.declaration.FieldDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration
import org.eclipse.xtend.lib.macro.declaration.Visibility
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

import static extension nl.kii.util.IterableExtensions.*
import static extension nl.kii.util.OptExtensions.*

class EntityInitializerClassUtil {
	val extension TransformationContext context
	val extension AccessorsProcessor.Util accessorsUtil
	
	val MutableClassDeclaration initializerClass
	val MutableClassDeclaration entityClass
	
	new(TransformationContext context, MutableClassDeclaration entityInitializerClass, MutableClassDeclaration entityClass) {
		this.context = context
		this.initializerClass = entityInitializerClass
		this.entityClass = entityClass
		
		this.accessorsUtil = new AccessorsProcessor.Util(context)
	}
	
	def populateInitializerClass(Iterable<? extends FieldDeclaration> fields) {
				
		/** Copy fields from Entity into constructor class */
		fields.forEach [ extension field |
			initializerClass.addField(simpleName) [
				type = field.type
				docComment = field.docComment
			]
		]
		
		/** Create getters and setters */
		initializerClass => [
			declaredFields.forEach [
				addGetter(getterType?.toVisibility ?: Visibility.PUBLIC)
				addSetter(setterType?.toVisibility ?: Visibility.PUBLIC)
			]
			
			/** Create empty constructor in entity constructor class */
			addConstructor [ 
				primarySourceElement = initializerClass
				body = ''' '''
			]
			
			/** Create 'entity copy constructor' in entity initializer class to later modify entity */
			addConstructor [ 
				primarySourceElement = initializerClass
				val argName = 'entity'
				addParameter(argName, entityClass.newTypeReference)
				body = '''
					«FOR field : fields»
						this.«field.simpleName» = «
						IF Opt.newTypeReference.isAssignableFrom(entityClass.findDeclaredMethod(field.getterName).returnType)»«
							OptExtensions.newTypeReference».orNull(«argName».«field.getterName»());«
						ELSE»«
							argName».«field.getterName»();«
						ENDIF»
					«ENDFOR»
				'''
			]
		]
	}
	
	
	def addInitializerFunctionsToEntity(Iterable<? extends FieldDeclaration> fields) {
		val pureAnnotationTypeRef = Pure.newAnnotationReference		
		val entityTypeRef = entityClass.newTypeReference
		val constructorTypeRef = initializerClass.newTypeReference
		val constructorOptionsTypeRef = newTypeReference(Procedure1, constructorTypeRef)


		/** Interal method to apply values from the constructor class on to the entity */
		entityClass.addMethod('applyConstructorFields') [
			primarySourceElement = entityClass
			visibility = Visibility.PRIVATE
			addParameter('constructor', constructorTypeRef)
			body = '''
				«FOR field : fields»
					«IF !field.type.primitive»if (constructor.«field.getterName»() != null) 
						«ENDIF»this.«field.simpleName» = constructor.«field.getterName»();
					
				«ENDFOR»
			'''
		]
		
		/** Add constructor with constructor class argument to entity */
		entityClass.addConstructor [
			primarySourceElement = entityClass
			val argName = 'constructor'
			addParameter(argName, constructorTypeRef)
			body = '''
				applyConstructorFields(constructor);
			'''
		]
		
	
		/** Add constructor with constructor class options argument to entity */
		entityClass.addConstructor [
			primarySourceElement = entityClass
			val argName = 'constructorOptions'
			addParameter(argName, constructorOptionsTypeRef)
			body = '''
				«initializerClass.qualifiedName» constructor = new «initializerClass.qualifiedName»();
				«argName».apply(constructor);
				applyConstructorFields(constructor);
			'''
		]
				
						
		/** Method to immutably modify entity, returning a new instance of the entity */
		entityClass.addMethod('mutate') [
			primarySourceElement = entityClass
			val argName = 'mutation'
			addParameter(argName, constructorOptionsTypeRef)
			returnType = entityTypeRef
			addAnnotation(pureAnnotationTypeRef)
			body = ['''
				«initializerClass.qualifiedName» constructor = new «initializerClass.qualifiedName»(this);
				«argName».apply(constructor);
				
				return new «entityClass.qualifiedName»(constructor);
			''']		
		]

		/** Operator shortcut for 'mutate' method */
		entityClass.addMethod('operator_doubleGreaterThan') [
			primarySourceElement = entityClass
			val argName = 'mutation'
			addParameter(argName, constructorOptionsTypeRef)
			returnType = entityTypeRef
			addAnnotation(pureAnnotationTypeRef)
			body = ['''
				return mutate(mutation);
			''']		
		]
	}
	
	def addConvenienceNestedEntitySetters() {
		val entityTypeRef = Entity.newTypeReference
		initializerClass.declaredFields
			.filter [ entityTypeRef.isAssignableFrom(type) ]
			.list
			.forEach [ field |
				val constructorTypeRef = if (field.type == entityClass.newTypeReference) initializerClass else findClass('''«field.type.name»Constructor''')
				if (constructorTypeRef.defined) 
					initializerClass.addMethod(field.simpleName) [ 
						val argName = '''«field.simpleName»Constructor'''
						
						val argTypeRef = Procedure1.newTypeReference(constructorTypeRef.newTypeReference)
						addParameter(argName, argTypeRef)
						body = '''
							«field.setterName»(new «field.type»(«argName»));
						'''
					]
				
			]
	}
	
}

