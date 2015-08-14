package nl.kii.entity.test

import java.util.Date
import java.util.List
import nl.kii.entity.annotations.Entity
import nl.kii.entity.annotations.Require

@Entity
class User {

	@Require String name
	User parent
	int age

	Date birthday
	Date registered
	List<String> sports
}
