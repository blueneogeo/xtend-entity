package nl.kii.entity.test

import nl.kii.entity.annotations.Entity

@Entity
class Location2 {
	String address
	Integer number
}

enum Membership2 {
	premium,
	trial,
	free
}