package nl.kii.entity.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * A @Entity with a field that has a @Require annotation will only validate if the required fields are set.
 */
@Target(ElementType.FIELD)
public @interface Require {
}
