package nl.kii.entity

import java.time.Instant
import java.util.Date
import java.util.List
import java.util.Map
import nl.kii.entity.annotations.Field
import nl.kii.entity.annotations.Require
import nl.kii.util.Period
import static extension nl.kii.util.IterableExtensions.*

@nl.kii.entity.annotations.Entity(casing=underscore)
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
	
	@Field(casing=camel) 
	def Integer getFriendsCount() { if (friends != null) friends.size as Integer }
	
	Location location
	Membership membership = Membership.free
	
	Map<String, String> attributes
	Map<Membership, Period> membershipDurations
	Map<Location, List<Double>> coordinates
	
	Long profileId
	int voucherCount = 10
	
	@nl.kii.entity.annotations.Serializer(Period) 
	val static s2 = Serializers.period
	
	@nl.kii.entity.annotations.Serializer(Date) 
	val static s1 = Serializers.date('yyyy-MM-dd', 'yyyy_MM_dd')
	
	@nl.kii.entity.annotations.Serializer(Instant) 
	val static s3 = Serializers.instant
	
	def void setCoordinates(Pair<Location, List<Double>>... coordinates) {
		this.coordinates = coordinates.toMap
	}
		
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

@nl.kii.entity.annotations.Entity(optionals=true)
class Location {
	String address
	Integer number
	
	/** Yahoo location id */
	@Field(casing=dot) Long WOEID
	
	@Field(name='long') Double longitude
	@Field(name='lat') Double latitude
}
