package nl.kii.entity.annotations

import java.lang.annotation.Target
import java.lang.annotation.ElementType

/**
 * Indicates that the annotated method should be moved to the entity's constructor class. 
 */
@Target(ElementType.METHOD)
annotation ToConstructor {
	
}
