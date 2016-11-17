package nl.kii.entity.annotations

import nl.kii.entity.Casing

/** Override serialization behaviour from type */
annotation Field {
	String name = ''
	Casing casing = Casing.ignore
}
