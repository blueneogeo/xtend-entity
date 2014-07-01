package nl.kii.reactive.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * An @Entity with a field that has a @Ignore annotation will not serialize this field
 */
@Target(ElementType.FIELD)
public @interface Ignore {
}
