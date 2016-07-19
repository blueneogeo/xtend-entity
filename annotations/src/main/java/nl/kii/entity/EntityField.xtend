package nl.kii.entity

import org.eclipse.xtend.lib.annotations.Data

@Data 
class EntityField {
	String name
	String formattedName
	Class<?> type
}
