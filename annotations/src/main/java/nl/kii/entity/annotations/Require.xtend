package nl.kii.entity.annotations

import java.lang.annotation.Target
import java.lang.annotation.ElementType

/**
 * A @Entity with a field that has a @Require annotation will only validate if the required fields are set.
 */

@Target(ElementType.FIELD)
annotation Require {
	
}
