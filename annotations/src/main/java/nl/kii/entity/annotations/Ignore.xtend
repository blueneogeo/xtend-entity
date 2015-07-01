package nl.kii.entity.annotations

import java.lang.annotation.Target
import java.lang.annotation.ElementType

/**
 * An @Entity with a field that has a @Ignore annotation will not serialize this field
 */

@Target(ElementType.FIELD)
annotation Ignore {
	
}