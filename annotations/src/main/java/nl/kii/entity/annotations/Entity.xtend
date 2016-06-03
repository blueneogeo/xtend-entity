package nl.kii.entity.annotations

import nl.kii.entity.processors.EntityProcessor
import org.eclipse.xtend.lib.macro.Active

@Active(EntityProcessor)
annotation Entity {
	/** 
	 * Whether getters of non-required fields should return {@code Opt<T>} instead of {@code T}.
	 */
	boolean optionals = false
	
	/** 
	 * Serializing field name casing transformation.
	 */
	Casing casing = Casing.underscore

	boolean mutable = false // true not yet supported
	boolean reactive = false // true not yet supported	
}

enum Casing {
	
	/** field_name */
	underscore, 
	/** field_name */
	snake,
	
	///////////////////
	
	/** fieldName */
	camel, 
	/** fieldName */
	lowerCamel, 
	
	///////////////////
	
	/** FieldName */
	upperCamel, 
	
	///////////////////

	/** field-name */
	dash, 
	/** field-name */
	hyphen, 

	///////////////////
	
	/** FIELD_NAME */
	upperUnderscore,
	/** FIELD_NAME */
	upperSnake 
	
}