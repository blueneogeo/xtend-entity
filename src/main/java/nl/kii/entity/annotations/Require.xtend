package nl.kii.entity.annotations

import java.lang.annotation.Target
import java.lang.annotation.ElementType

/**
 * An @Entity with a field that has a @Match annotation can only be transformed from json into
 * this entity if the json contains this field. 
 * <p>
 * Required fields are used for pattern matching incoming JSON against Entities. Because of this, 
 * at least one required field is necessary, and more than one is recommended for reliable good pattern 
 * matching against JsonEntity classes.
 * <p>
 * Alternatively, use the @Type property to set the entity class to match with.
 */

@Target(ElementType.FIELD)
annotation Require {
	
}