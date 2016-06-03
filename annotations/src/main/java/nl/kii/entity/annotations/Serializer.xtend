package nl.kii.entity.annotations

import java.lang.annotation.Target

@Target(FIELD)
annotation Serializer {
	Class<?> value
}