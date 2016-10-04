package nl.kii.entity.processors

import com.google.common.collect.Sets
import java.util.List
import nl.kii.entity.EntityField
import org.eclipse.xtend.lib.macro.TransformationContext
import org.eclipse.xtend.lib.macro.declaration.ClassDeclaration
import org.eclipse.xtend.lib.macro.declaration.FieldDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration
import org.eclipse.xtend.lib.macro.declaration.TypeReference
import org.eclipse.xtend.lib.macro.declaration.Visibility

class EntityReflectionUtil {
	val extension TransformationContext context
	val extension EntityProcessor.Util baseUtil
	val (String)=>String fieldNameFormatter
	
	new(TransformationContext context, (String)=>String fieldNameFormatter) {
		this.context = context
		this.baseUtil = new EntityProcessor.Util(context)
		this.fieldNameFormatter = fieldNameFormatter
	}
	
	def void populateFieldsClass(MutableClassDeclaration fieldsClass, Iterable<? extends FieldDeclaration> fields) {		
		fields.forEach [ extension field |
			/** Copy fields to Fields class */
			fieldsClass.addField(field.simpleName) [
				primarySourceElement = field
				type = EntityField.newTypeReference
				visibility = Visibility.PUBLIC
				static = true
				final = true
				initializer = '''new «EntityField»("«field.simpleName»", "«fieldNameFormatter.apply(field.simpleName)»", «field.type.cleanTypeName».class)'''
				docComment = field.docComment
			]
		]
	}
	
	def void addFieldsGetter(MutableClassDeclaration entityClass, ClassDeclaration fieldsClass) {
		val pureAnnotationTypeRef = Pure.newAnnotationReference
		val entityFieldTypeRef = EntityField.newTypeReference
		val entityFieldListTypeRef = List.newTypeReference(entityFieldTypeRef)
		//val collectionsTypeRef = CollectionLiterals.newTypeReference
		
		entityClass.addMethod('getFields') [
			primarySourceElement = entityClass
			returnType = entityFieldListTypeRef
			addAnnotation(pureAnnotationTypeRef)
			
			if (entityClass.extendsEntity) body = '''
				return «IterableExtensions».toList(«Sets».union(
					«IterableExtensions».toSet(super.getFields()),
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

