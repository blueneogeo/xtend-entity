package nl.kii.entity.test

import nl.kii.entity.annotations.Entity
import nl.kii.entity.annotations.Require
import java.util.Date

@Entity
class User {

	@Require String name
	User parent
	int age

	Date birthday
}
