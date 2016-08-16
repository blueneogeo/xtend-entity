package nl.kii.entity.annotations

import java.lang.annotation.Target

@Target(FIELD, METHOD)
annotation Serializer {
	Class<?> value
}