package nl.kii.entity.processors

import java.util.List
import java.util.Map
import nl.kii.entity.Change
import nl.kii.entity.EntityException
import nl.kii.entity.EntityList
import nl.kii.entity.EntityMap
import nl.kii.entity.ReactiveObject
import nl.kii.entity.annotations.Entity
import nl.kii.entity.annotations.Ignore
import nl.kii.entity.annotations.Require
import nl.kii.observe.Observable
import org.eclipse.xtend.lib.macro.TransformationContext
import org.eclipse.xtend.lib.macro.TransformationParticipant
import org.eclipse.xtend.lib.macro.declaration.AnnotationTarget
import org.eclipse.xtend.lib.macro.declaration.ClassDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableExecutableDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableFieldDeclaration
import org.eclipse.xtend.lib.macro.declaration.TypeParameterDeclaration
import org.eclipse.xtend.lib.macro.declaration.TypeReference
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

import static org.eclipse.xtend.lib.macro.declaration.Visibility.*

import static extension nl.kii.util.IterableExtensions.*
import static extension nl.kii.util.OptExtensions.*
import nl.kii.entity.EntityObject

/** 
 * Active Annotation Processor for Entity annotations.
 * @see Entity
 */
class EntityProcessor implements TransformationParticipant<MutableClassDeclaration> {
	
	override doTransform(List<? extends MutableClassDeclaration> classes, extension TransformationContext context) {
		
		val changeHandlerType = Procedure1.newTypeReference(Change.newTypeReference)
		val stopObservingType = Procedure0.newTypeReference
		
		// will convert these type to their wrapper types for getter and setter methods
		val typeConversions = #{
			'boolean' -> Boolean,
			'int' -> Integer,
			'long' -> Long,
			'float' -> Float,
			'double' -> Double
		}
		
		for(cls : classes) {

			// Set the class
			val clsType = cls.newTypeReference
			cls.primarySourceElement = cls
			cls.extendedClass = ReactiveObject.newTypeReference
			cls.implementedInterfaces = cls.implementedInterfaces + #[Cloneable.newTypeReference]
			
			// determine which fields we create getters and setters for and which are monitored
			
			val getSetFields = cls.declaredFields.filter [
				!static && !volatile && !simpleName.startsWith('_') && !visibility.in(PROTECTED, DEFAULT)
			]
			
			val requiredFields = getSetFields.filter [
				findAnnotation(Require.newTypeReference.type) != null
			]
			
			val observedFields = getSetFields.filter [
				findAnnotation(Ignore.newTypeReference.type) == null
			]

			val reactiveFields = observedFields.filter [ isReactive(context) ]

			cls.docComment = '''
				«cls.docComment»
				Part of this class source code is autogenerated by the @Entity active annotation.
				Please see @Entity, EntityObject and ReactiveObject for more information on EntityObjects and ReactiveObjects.
				<p>Detected reactive entity fields: «reactiveFields.map[simpleName]»
				<p>Observing fields: «observedFields.map[simpleName]» 
			'''

			// create (empty) listener fields that are filled once someone calls a getter

			for(field : reactiveFields) {
				cls.addField(field.stopObservingFunctionName) [ 
					type = stopObservingType
					primarySourceElement = field
					visibility = PROTECTED
					transient = true
				]
			}

			// create empty constructor
			
			cls.addConstructor [
				docComment= '''
					Create an empty constructor for builders, eg:
					<pre>
						val entity = new «cls.simpleName» => [
							«IF cls.declaredMembers.length > 0»
								«cls.declaredMembers.head.simpleName» = 'test'
							«ENDIF»
						]
					</pre>
				'''
				primarySourceElement = cls
				addClassTypeParameters(cls, context)
				body = ['']
			]
			
			// create constructor for the required fields if neccessary
			
			if(requiredFields.length > 0) {
				cls.addConstructor [
					docComment = '''Create a new «cls.simpleName» for all fields annotated with @Require.'''
					primarySourceElement = cls
					addClassTypeParameters(cls, context)
					for(field : requiredFields)
						addParameter(field.simpleName, field.type)
					body = ['''
						this.setPublishing(false);
						«cls.classTypeParameterAssignmentCode»
						«FOR field : requiredFields»
							this.set«field.simpleName.toFirstUpper»(«field.simpleName»);
						«ENDFOR»
						this.setPublishing(true);
					''']
				]
			}
			
			// create complete constructor if necessary
			
			if(observedFields.length > requiredFields.length ) {
				cls.addConstructor [
					docComment = '''Create a constructor for all fields (except for those annotated with @Ignore).'''
					primarySourceElement = cls
					addClassTypeParameters(cls, context)
					for(field : getSetFields)
						addParameter(field.simpleName, field.type)
					body = ['''
						this.setPublishing(false);
						«cls.classTypeParameterAssignmentCode»
						«FOR field : getSetFields»
							this.set«field.simpleName.toFirstUpper»(«field.simpleName»);
						«ENDFOR»
						this.setPublishing(true);
					''']
				]
			}
			
			// maps and lists must be typed
			
			for(field : observedFields) {
				if(field.type.simpleName.startsWith('List'))
					if(field.type.actualTypeArguments.empty)
						field.addError('Reactive classes may not have untyped Lists')
				if(field.type.simpleName.startsWith('Map'))
					if(field.type.actualTypeArguments.empty)
						field.addError('Reactive classes may not have untyped Maps')
			}
			
			// create the getType methods
			
			cls.addMethod('getInstanceType') [
				docComment = '''Gives the instance access to the getType method.'''
				primarySourceElement = cls
				addParameter('path', List.newTypeReference(string))
				returnType = Class.newTypeReference
				exceptions = EntityException.newTypeReference
				body = ['''
					return getType(path);
				''']
			]
			
			cls.addMethod('getType') [
				docComment = '''
					Gets the class of any field path into the object. Also navigates inner maps and lists.
					This lets you get past erasure, and look into the wrapped types of objects at runtime.
					<p>
					The path is made up of a strings, each the name of a field. An empty path will give the
					type of this object, while a single string will give the type of that field inside this
					class. More strings will navigate recursively into that type. 
					<p>
					For instance, if you have an entity with a field users that is a Map<String, User>, 
					and each user has a name field of type String, then you could get the type of that name 
					field by asking the entity:
					<pre>Entity.getType('users', 'john', 'name') // returns String</pre>
				'''
				primarySourceElement = cls
				static = true
				addParameter('path', List.newTypeReference(string))
				returnType = Class.newTypeReference
				exceptions = EntityException.newTypeReference
				body = ['''
					if(path == null || path.size() == 0) return «clsType.nameWithoutGenerics».class;
					String fieldName = path.get(0);
					«FOR field : getSetFields»
						if(fieldName.equals("«field.simpleName»")) {
							if(path.size() == 1) {
									return «field.type.nameWithoutGenerics».class;
							} else {
								«IF field.type.extendsType(EntityList.newTypeReference) || field.type.extendsType(EntityMap.newTypeReference)»
									«val containedType = field.type.actualTypeArguments.get(0)»
									if(path.size() == 2) return «containedType».class;
									else 
									«IF containedType.extendsType(EntityObject.newTypeReference)»
										return «containedType».getType(path.subList(2, path.size()));
									«ELSE»
										throw new EntityException("path " + path + " does not match structure of «containedType.simpleName»");
									«ENDIF»
								«ELSEIF field.type.extendsType(EntityObject.newTypeReference)»
									return «field.type.simpleName».getType(path.subList(1, path.size()));
								«ELSE»
									throw new EntityException("path " + path + " does not match structure of «field.type.simpleName»"); 
								«ENDIF»
							} 
						}
					«ENDFOR»
					throw new EntityException("could not match path " + path + " on entity «clsType.simpleName»");
				''']
			]
			
			// create the validate method
			
			cls.addMethod('validate') [
				docComment = '''
					Check if the «cls.simpleName» is valid.
					Also recursively checks contained entities within the members of «cls.simpleName».
					@return true if all the fields annotated with @Require have a value.
				'''
				primarySourceElement = cls
				exceptions = EntityException.newTypeReference
				body = ['''
					«FOR field : requiredFields»
						«IF !field.type.primitive»
							if(«field.simpleName»==null) throw new EntityException("«cls.simpleName».«field.simpleName» may not be empty.");
							«IF field.in(reactiveFields)»
								«field.simpleName».validate();
							«ENDIF»
						«ENDIF»
					«ENDFOR»
				''']
			]

			// create getters and setters
			
			for(f : getSetFields) {

				cls.addMethod('get' + f.simpleName.toFirstUpper) [
					docComment = '''
						Get the value of the «cls.simpleName» entity property «f.simpleName».
						@return the found «f.simpleName» or null if not set.
					'''
					primarySourceElement = f
					returnType = typeConversions
						.get(f.type.simpleName)?.newTypeReference
						.or(f.type)
					body = ['''
						«IF f.isEntityMap»
							if(«f.simpleName»==null) {
								«f.newEntityMap('newMap', context)»
								«f.simpleName» = newMap;
							}
						«ELSEIF f.isEntityList»
							if(«f.simpleName»==null) {
								«f.newEntityList('newList', context)»
								«f.simpleName» = newList;
							}
						«ENDIF»
						return «f.simpleName»;
					''']
				]
				
				cls.addMethod('set' + f.simpleName.toFirstUpper) [
					docComment = '''
						Set the value of the «cls.simpleName» entity property «f.simpleName».<p>
						This will trigger a change event for the observers.
					'''
					primarySourceElement = f
					val setterType = typeConversions
						.get(f.type.simpleName)?.newTypeReference
						.or(f.type)
					addParameter('value', setterType)
					body = ['''
						«IF f.in(reactiveFields) » 
							// stop listening to old value
							if(this.«f.simpleName» != null && this.«f.stopObservingFunctionName» != null)
								«f.stopObservingFunctionName».apply();
						«ENDIF»
						// start observing the new value for changes
						«f.assignFieldValue(context)»
						«IF f.in(observedFields)»
							// if we are publishing, publish the change we've made
							if(this.isPublishing()) {
								«IF f.type.primitive || typeConversions.values.map[newTypeReference].toList.contains(f.type)|| f.type.isAssignableFrom(String.newTypeReference)»
									getPublisher().apply(new Change(nl.kii.entity.ChangeType.UPDATE, "«f.simpleName»", value));
								«ELSEIF f.isEntityMap»
									getPublisher().apply(new Change(nl.kii.entity.ChangeType.UPDATE, "«f.simpleName»", ((«f.toEntityMapType(context).name»)this.«f.simpleName»).clone()));
								«ELSEIF f.isEntityList»
									getPublisher().apply(new Change(nl.kii.entity.ChangeType.UPDATE, "«f.simpleName»", ((«f.toEntityListType(context).name»)this.«f.simpleName»).clone()));
								«ELSEIF f.type.extendsType(Cloneable.newTypeReference)»
									getPublisher().apply(new Change(nl.kii.entity.ChangeType.UPDATE, "«f.simpleName»", this.«f.simpleName».clone()));
								«ELSE»
									getPublisher().apply(new Change(nl.kii.entity.ChangeType.UPDATE, "«f.simpleName»", this.«f.simpleName»));
								«ENDIF»
							}
						«ENDIF»
					''']
				]

			}
			
			// create the apply change method

			cls.addMethod('apply') [
				docComment = '''
					Apply a change to the «cls.simpleName».<br/>
					This will not trigger a change for observers of this «cls.simpleName».
					<p>
					The change is not neccessarly applied to this object itself, 
					but depending on the path of the change, be applied to members or members of members.
				'''
				synchronized = true
				addParameter('change', Change.newTypeReference)
				primarySourceElement = cls
				body = ['''
					boolean wasPublishing = this.isPublishing();
					try {
						// do not publish when applying, to prevent update loops
						this.setPublishing(false);
						// determine what to change using the change path
						if(change.getPath() == null || change.getPath().size() == 0) {
							// change applies to this object, check the type
							if(change.getValue() == null)
								throw new NullPointerException("incoming change has no value: " + change);
							if(!(change.getValue() instanceof «clsType.name»)) 
								throw new IllegalArgumentException("incoming change has a value of the wrong type: " + change + ", expected " + this.getClass().getName());
							// assign the all fields directly from the value of the change
							«IF clsType.extendsType(Cloneable.newTypeReference)»
								«clsType.name» value = ((«clsType.name»)change.getValue()).clone();
							«ELSE»
								«clsType.name» value = ((«clsType.name»)change.getValue());
							«ENDIF»
							«FOR field : observedFields»
								«IF !field.type.primitive»
									if(value.«field.simpleName» != null) 
								«ENDIF»
									this.set«field.simpleName.toFirstUpper»(value.«field.simpleName»);
							«ENDFOR»
							try {
								this.validate();
							} catch(EntityException e) {
								throw new IllegalArgumentException("incoming change created an invalid entity: " + change, e);
							}
						} else if(change.getPath().size() == 1) {
							// change applies directly to a field of this object
							String field = change.getPath().get(0);
							«FOR field : observedFields»
							if(field.equals("«field.simpleName»")) {
								switch(change.getAction()) {
									case UPDATE:
										this.set«field.simpleName.toFirstUpper»((«field.type.simpleName»)change.getValue());
										// this.«field.simpleName» = («field.type.simpleName»)change.getValue();
										break;
									case CLEAR:
										«IF !field.type.primitive»
											this.«field.simpleName» = null;
										«ENDIF»
										break;
									default: throw new IllegalArgumentException("cannot update field «field.simpleName» of entity «clsType.simpleName» with " + change + ", must be an UPDATE or CLEAR command");
								}
							}
							«ENDFOR»
						} else {
							// change goes deeper inside of of the fields, propagate the path inside that field
							String field = change.getPath().get(0);
							«FOR field : observedFields»
							if(field.equals("«field.simpleName»")) {
								«IF field.isReactive(context)»
									«field.simpleName».apply(change.forward());
								«ELSE»
									throw new IllegalArgumentException("cannot update field «field.simpleName» of entity «clsType.simpleName» with " + change + ", the field is not Reactive");
								«ENDIF»
							}
							«ENDFOR»
						}
					} finally {
						this.setPublishing(wasPublishing);
					}
				''']
			]
			
			// create a tostring override
			if(cls.declaredMethods.filter[simpleName=='toString'].empty)
				cls.addMethod('toString') [
					// addAnnotation(overrideType.)
					primarySourceElement = cls
					returnType = string
					body = ['''
						return "«cls.simpleName» { "
						«FOR field:getSetFields SEPARATOR ' + ", " '»
							+ "«field.simpleName»: " +
							«IF field.type.isAssignableFrom(string)»
								"'" + this.«field.simpleName» + "'" 
							«ELSE»
								this.«field.simpleName» 
							«ENDIF»
						«ENDFOR»
						+ " }";
					''']
				]
			
			// create equals override
			if(cls.declaredMethods.filter[simpleName=='equals'].empty)
				cls.addMethod('equals') [
					//addAnnotation(overrideType.type)
					primarySourceElement = cls
					addParameter('object', object)
					returnType = primitiveBoolean
					body = ['''
						if(object != null && object instanceof «cls.simpleName») {
							return (
								«FOR field : getSetFields SEPARATOR ' && '»
									«IF field.type.primitive»
										this.«field.simpleName» == «field.simpleName»
									«ELSEIF field.type.extendsType(Map.newTypeReference) || field.type.extendsType(List.newTypeReference)»
										// consider an empty «field.type.simpleName» the same as a null. one of the below must be true:
										(
											// both are null
											(this.«field.simpleName» == null && ((«cls.simpleName») object).«field.simpleName» == null) ||
											// or the this.«field.simpleName» is not null but empty, and object.«field.simpleName» is null 
											(
												this.«field.simpleName» != null && this.«field.simpleName».isEmpty() &&
												((«cls.simpleName») object).«field.simpleName» == null
											) ||
											// or the this.«field.simpleName» is null, and object.«field.simpleName» is not null but empty 
											(
												this.«field.simpleName» == null &&
												((«cls.simpleName») object).«field.simpleName» != null &&
												((«cls.simpleName») object).«field.simpleName».isEmpty()
											) ||
											// or both are not null
											(
												this.«field.simpleName» != null && 
												this.«field.simpleName».equals(((«cls.simpleName») object).«field.simpleName»)
											) 
										)
									«ELSE»
										(
											(this.«field.simpleName» == null && ((«cls.simpleName») object).«field.simpleName» == null) ||
											(
												this.«field.simpleName» != null && 
												this.«field.simpleName».equals(((«cls.simpleName») object).«field.simpleName»)
											) 
										)
									«ENDIF»
								«ENDFOR»
							);
						} else return false;
					''']
				]
			
			// create hashcode override
			if(cls.declaredMethods.filter[simpleName=='hashCode'].empty)
				cls.addMethod('hashCode') [
					// addAnnotation(overrideType.type)
					primarySourceElement = cls
					returnType = primitiveInt
					body = ['''
						return (
							«FOR field : getSetFields SEPARATOR ' + '»
								«IF field.type.primitive»
									(this.«field.simpleName» + "").hashCode()
								«ELSEIF field.type.extendsType(Map.newTypeReference) || field.type.extendsType(List.newTypeReference)»
									((this.«field.simpleName» != null) ?
										(
											this.«field.simpleName».isEmpty() ?
												"null".hashCode()
												: (this.«field.simpleName» + "").hashCode()
										)
										: 0)
								«ELSE»
									((this.«field.simpleName» != null) ?
										(this.«field.simpleName» + "").hashCode()
										: 0)
								«ENDIF»
							«ENDFOR»
						) * 37;
					''']
				]
			
			// create clone override
			if(cls.declaredMethods.filter[simpleName=='clone'].empty)
				cls.addMethod('clone') [
					primarySourceElement = cls
					returnType = clsType
					body = ['''
						try {
							return («clsType.simpleName»)super.clone();
						} catch(CloneNotSupportedException e) {
							return null;
						}
					''']
				]
//					// addAnnotation(overrideType.type)
//					docComment = '''
//						Make a copy of the instance.
//						cloning is important for propagating changes, since changes may have no ties to the original object
//					'''
//					returnType = cls.newTypeReference
//					body = ['''
//						«cls.qualifiedName» instance = new «cls.qualifiedName»();
//						«FOR field : getSetFields»
//							if(this.«field.simpleName» != null) {
//								«IF field.type.primitive || field.type.isAssignableFrom(String.newTypeReference)»
//									instance.set«field.simpleName.toFirstUpper»(this.«field.simpleName»);
//								«ELSEIF field.type.isAssignableFrom(Map.newTypeReference)»
//									instance.set«field.simpleName.toFirstUpper»(((«field.toEntityMapType(context).name»)this.«field.simpleName»).clone());
//								«ELSEIF field.type.isAssignableFrom(List.newTypeReference)»
//									instance.set«field.simpleName.toFirstUpper»(((«field.toEntityListType(context).name»)this.«field.simpleName»).clone());
//								«ELSE»
//									instance.set«field.simpleName.toFirstUpper»(this.«field.simpleName».clone());
//								«ENDIF»
//							}
//						«ENDFOR»
//						return instance;
//					''']

			// convert map fields to entitymap fields

			reactiveFields
				.filter [ type.simpleName.startsWith('Map') ]
				.forEach [
					val key = type.actualTypeArguments.get(0)
					val value = type.actualTypeArguments.get(1)
					if(!key.extendsType(string)) {
						addError('Maps in EntityObjects may only have String as their key')
					} else {
						type = EntityMap.newTypeReference(value)
					}

				]

			// convert lists to entitylists

			reactiveFields
				.filter [ type.simpleName.startsWith('List') ]
				.forEach [
					val typeArg = type.actualTypeArguments.get(0)
					type = EntityList.newTypeReference(typeArg) 
				]

			// helpers

			cls.addMethod('newChangeHandler') [
				visibility = PROTECTED
				docComment = 'creates a listener for propagating to changes on a field to the publisher'
				addParameter('path', string)
				primarySourceElement = cls
				returnType = changeHandlerType
				body = ['''
					final «clsType.simpleName» entity = this;
					return new Procedure1<Change>() {
						public void apply(Change change) {
							//only propagate a change if we can publish
							if(hasPublisher())
								getPublisher().apply(change.addPath(path));
						}
					};
				''']
			]
			
		}
		
	}

	def isReactive(MutableFieldDeclaration field, extension TransformationContext context) {
		val type = field.type.type
		val isEntity = if(type instanceof AnnotationTarget) {
			type.findAnnotation(Entity.newTypeReference.type) != null
		}
		
		// field.type.extendsType(ReactiveObject.newTypeReference) ||
		isEntity ||
		field.type.extendsType(List.newTypeReference) ||
		field.type.extendsType(Map.newTypeReference)
	}
	
	def isEntityList(MutableFieldDeclaration field) {
		field.type.simpleName.startsWith('EntityList')
	}

	def isEntityMap(MutableFieldDeclaration field) {
		field.type.simpleName.startsWith('EntityMap')
	}
	
	def isObservable(MutableFieldDeclaration field, extension TransformationContext context) {
		Observable.newTypeReference(Change.newTypeReference).isAssignableFrom(field.type)
	}
	
	def <T> extendsType(TypeReference type, TypeReference superType) {
		superType.isAssignableFrom(type)
	}

	def assignFieldValue(MutableFieldDeclaration field, extension TransformationContext context) '''
		«IF field.isEntityList»
			// if the list is not already reactive, wrap the list as a reactive list
			«field.newEntityList('newList', context)»;
			if(value != null) newList.addAll(value);
			«field.simpleName» = newList;
			this.«field.getStopObservingFunctionName» = newList.onChange(newChangeHandler("«field.simpleName»"));
		«ELSEIF field.entityMap»
			// if the map is not already listenable, wrap the map as a listenable
			«field.newEntityMap('newMap', context)»;
			if(value != null) newMap.putAll(value);
			«field.simpleName» = newMap;
			this.«field.getStopObservingFunctionName» = newMap.onChange(newChangeHandler("«field.simpleName»"));
		«ELSEIF field.isObservable(context)»
			this.«field.simpleName» = value;
			this.«field.getStopObservingFunctionName» = this.«field.simpleName».onChange(newChangeHandler("«field.simpleName»"));
		«ELSE»
			this.«field.simpleName» = value;
		«ENDIF»
	'''

	def getStopObservingFunctionName(MutableFieldDeclaration field) {
		'stopObserving' + field.simpleName.toFirstUpper + 'Fn'
	}

	def getTypeParamName(TypeParameterDeclaration type, int position) {
		'typeParam' + position
	}
	
	def newEntityList(MutableFieldDeclaration field, String valName, extension TransformationContext context) {
		val typeArg = field.type.actualTypeArguments.get(0)
		val type = EntityList.newTypeReference(typeArg)
		'''final «type.simpleName» «valName» = new «type.name»(«typeArg.simpleName».class);'''
	}
	
	def newEntityMap(MutableFieldDeclaration field, String valName, extension TransformationContext context) {
		val typeArg = field.type.actualTypeArguments.get(0)
		val type = EntityMap.newTypeReference(typeArg)
		'''final «type.simpleName» «valName» = new «type.name»(«typeArg.simpleName».class);'''
	}
	
	def toEntityMapType(MutableFieldDeclaration field, extension TransformationContext context) {
		EntityMap.newTypeReference(field.type.actualTypeArguments.get(0))
	}
	
	def toEntityListType(MutableFieldDeclaration field, extension TransformationContext context) {
		EntityList.newTypeReference(field.type.actualTypeArguments.get(0))
	}
	
	def addClassTypeParameters(MutableExecutableDeclaration constructor, ClassDeclaration cls, extension TransformationContext context) {
		cls.typeParameters.forEach [ param, count | 
			constructor.addParameter(
				param.getTypeParamName(count + 1), 
				Class.newTypeReference(param.newTypeReference)
			)
		]
	}
	
	def getClassTypeParameterAssignmentCode(ClassDeclaration cls) {
		val List<String> list = newLinkedList
		cls.typeParameters.forEach [ param, count |
			val name = param.getTypeParamName(count + 1)
			list.add('''this.«name» = «name»;''')
		]
		list.join('\n')
	}
	
	def getClassTypeParameters(ClassDeclaration cls) {
		val List<String> list = newLinkedList
		cls.typeParameters.forEach [ param, count |
			list.add(param.getTypeParamName(count + 1))
		]
		list
	}
	
	def getNameWithoutGenerics(TypeReference ref) {
		ref.name.replaceAll('<.+>', '')
	}
	
}