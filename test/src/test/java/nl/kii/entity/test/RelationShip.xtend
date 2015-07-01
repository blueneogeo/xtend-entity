package nl.kii.entity.test

import java.util.List
import java.util.Map
import nl.kii.entity.annotations.Entity
import nl.kii.entity.annotations.Require

@Entity
class RelationShip {

	@Require User user
	Map<String, User> relations
	List<User> friends

}
