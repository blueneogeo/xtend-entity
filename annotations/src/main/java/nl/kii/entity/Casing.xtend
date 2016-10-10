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
	
	///////////////////

	/** field-name */
	dash, 
	/** field-name */
	hyphen, 

	///////////////////
	
	/** FIELD_NAME */
	upperUnderscore,
	/** FIELD_NAME */
	upperSnake,
	
	///////////////////
	
	/** field.name */
	dot
	
}