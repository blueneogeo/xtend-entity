package nl.kii.entity

class EntityException extends Exception {
	
	new(String s) { super(s) }
	
	new(String s, Throwable t) { super(s, t) }
	
}