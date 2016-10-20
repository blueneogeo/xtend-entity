package nl.kii.entity

import java.time.Instant
import java.util.Date
import java.util.List
import java.util.Map
import nl.kii.entity.Serializers
import nl.kii.entity.annotations.Entity
import nl.kii.entity.annotations.Require
import nl.kii.entity.annotations.Serializer
import nl.kii.util.Period

@Entity(casing=underscore)
class User {
	@Require String name
	User referral
	Integer age
	
	Date dateOfBirth
	Date registered
	
	Period bestTime
	
	List<String> interests
	List<Date> purchases
	List<User> friends
	
	Location location
	Membership membership = Membership.free
	
	Map<String, String> attributes 
	Map<Membership, Period> membershipDurations
	Map<Location, List<Double>> coordinates
	
	Long profileId
	int voucherCount = 10
	
	@Serializer(Period) 
	val static s2 = Serializers.period
	
	@Serializer(Date) 
	val static s1 = Serializers.date('yyyy-MM-dd', 'yyyy_MM_dd')
	
	@Serializer(Instant) 
	val static s3 = Serializers.instant
	
//	val static dateFormat = #[ 'yyyy-MM-dd' ]	
//	val static serializers = #[
//		Period -> Serializers.period,
//		Instant -> Serializers.instant('')
//	]	
}

enum Membership {
	premium, 
	trial,
	free
}

@Entity(optionals=true)
class Location {
	String address
	Integer number
}
