package nl.kii.entity.annotations

import java.lang.annotation.Target

/**
 * Annotate an @Entity class field with @Type to indicate that when converting json to
 * this entity, the type value of that field should be used to determine what entity class to
 * convert to. The field has to be a string, and it will be automatically filled in when
 * calling the .getJson() method of the JsonEntity. You may also assign it a value, in which
 * case the field will always be filled with this value.
 */

@Target(FIELD)
annotation Type {
	
}