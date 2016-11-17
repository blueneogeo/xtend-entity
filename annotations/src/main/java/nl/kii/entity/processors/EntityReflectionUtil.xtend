package nl.kii.entity.processors

import com.google.common.collect.Sets
import java.util.List
import nl.kii.entity.EntityField
import nl.kii.entity.processors.EntityProcessor.EntityFieldSignature
import org.eclipse.xtend.lib.macro.TransformationContext
import org.eclipse.xtend.lib.macro.declaration.ClassDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration
import org.eclipse.xtend.lib.macro.declaration.TypeReference
import org.eclipse.xtend.lib.macro.declaration.Visibility

class EntityReflectionUtil {
	val extension TransformationContext context
	val extension EntityProcessor.Util baseUtil
	
	new(TransformationContext context) {
		this.context = context
		this.baseUtil = new EntityProcessor.Util(context)
	}
	
	def void populateFieldsClass(MutableClassDeclaration fieldsClass, Iterable<? extends EntityFieldSignature> fields) {		
		fields.forEach [ extension field |
			/** Copy fields to Fields class */
			fieldsClass.addField(field.name) [
				primarySourceElement = declaration
				type = EntityField.newTypeReference
				visibility = Visibility.PUBLIC
				static = true
				final = true
				initializer = '''new «EntityField»("«field.name»", "«field.serializedName»", «field.type.cleanTypeName».class, «required»)'''
				docComment = declaration.docComment
			]
		]
	}
	
	def void addFieldsGetter(MutableClassDeclaration entityClass, ClassDeclaration fieldsClass) {
		val pureAnnotationTypeRef = Pure.newAnnotationReference
		val entityFieldTypeRef = EntityField.newTypeReference
		val entityFieldListTypeRef = List.newTypeReference(entityFieldTypeRef)
		//val collectionsTypeRef = CollectionLiterals.newTypeReference
		
		entityClass.addMethod('getFields') [
			returnType = entityFieldListTypeRef
			addAnnotation(pureAnnotationTypeRef)
			body = '''
				return «entityClass».getEntityFields();
			'''
		]
		
		entityClass.addMethod('getEntityFields') [
			primarySourceElement = entityClass
			returnType = entityFieldListTypeRef
			addAnnotation(pureAnnotationTypeRef)
			static = true
			
			if (entityClass.extendsEntity) body = '''
				return «IterableExtensions».toList(«Sets».union(
					«IterableExtensions».toSet(«entityClass.extendedClass».getEntityFields()),
					«IterableExtensions».toSet(«CollectionLiterals».newImmutableList(«FOR f:fieldsClass.declaredFields SEPARATOR ', '»Fields.«f.simpleName»«ENDFOR»))
				));
			'''
			else body = '''
				return «CollectionLiterals».newImmutableList(«FOR f:fieldsClass.declaredFields SEPARATOR ', '»Fields.«f.simpleName»«ENDFOR»);
			'''
		]
	}
	
	def static cleanTypeName(TypeReference typeRef) {
		typeRef.name.replaceAll('<.+>', '').replaceAll('\\$', '.')
	}
	
}

