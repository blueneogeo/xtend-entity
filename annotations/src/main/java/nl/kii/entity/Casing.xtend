package nl.kii.entity

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
	/** FieldName */
	pascal, 
	
	///////////////////

	/** field-name */
	dash, 
	/** field-name */
	hyphen, 
	/** field-name */
	kebab, 
	/** field-name */
	lisp, 

	///////////////////
	
	/** FIELD_NAME */
	upperUnderscore,
	/** FIELD_NAME */
	upperSnake,
	/** FIELD_NAME */
	screamingSnake,
	
	///////////////////
	
	/** field.name */
	dot,
	
	///////////////////
	
	/** no transformation */
	ignore	
}