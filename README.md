xtend-reactive
====================

# Entities

By annotating a class with the @Entity class, you create a vertx entity. This entity is much like a class annotated with the @Data annotation, but mutable: 

- you get constructors
- for your fields you get getters and setters

Entities have some specific benefits though:

- a setJson() method
- a getJson() method
- pattern matching against json

The getJson and setJson methods are part of the Jsonable interface, which all entities implement.

This means that entities can be converted into Json and from Json to an entity. It uses pattern matching to find out if it can make the conversion. For this it has a validate() method.

An example of a simple entity:

	@Entity
	class User {
		@Match int userId
		@Match String name
		Address address
		int age = 30
	}

## @Match

The fields annotated with @Match are required and used for matching/validating when trying to convert a json string into the entity. In this case, that means that the json must be a json object with a field userId that contains an int and a field name that contains a String. Address and age are optional. However age has a default value of 30 if it is not passed.

## @Type

Instead or aside pattern matching, you can also have the type passed in the object. This guarantees that the correct type will be pattern matched.

	@Entity
	class AddUser {
		@Type String command = 'add-user'
		@Match User user
	}

For example, the above class has a command property that must be set to the value 'add-user'. You can also leave out the default value 'add-user', in which case the full name of the class is used in the json.

## Date formatting

Dates are automatically converted into date strings and back. However, you may want a specific date format to be used, or be dependant on an incoming date format from a third party.

In order to deal with this, you can pass a date format parameter to the @Entity annotation:

	@Entity(dateFormat='yyyy-MM-dd')
	class UserUpdated {
		@Match User updatedUser
		@Match Date updatedAt
	}

See the DateFormat class in the standard JavaSDK for more information on how to format the dateFormat text.

## Casing

By default, the entities use dash-casing when converting its fields to json. For example, the field updatedUser becomes 'updated-user'. However you can also preserve the camelcasing of Java by setting the casing property to camel:

	@Entity(casing='camel') // default casing='dash'


# Reactive Entities

Reactive entities have two major features:

- they can publish that they have changed
- they can accept a change command and update themselves with it

This allows code to listen to changes in an entity and publish these changes so that other instances of that data automatically are updated. Since the changes themselves are Jsonable, they can be sent across any network. This opens up the possibility where data is in a 'live' state. Imagine a user name being updated, and it also updating in a database automatically, as well as on any browser that has that user open, without any extra code being necessary.

This requires that these entities are 'managed'. For reasons of performance, each entity can have one change listener. For example, this becomes possible:

	@Entity(reactive)
	class User {
  	@Match int userId
		@Match string name
	}

	val user = new User(12,'Mary')
	user.onChange [ println('new ' + path.head + ' is ' + value) ]
	user.name = 'John' // will print 'new name is John'

As you can see, setting a property of the user fires the onChange handler with a change. A Change is a Jsonable with an action (ADD, UPDATE, DELETE or CLEAR), a path (a list of strings) and an optional new value. In the above case, the action is UPDATE, the path is #['name'] and the value is 'John'.

For setters, only UPDATE makes sense. ADD, DELETE and CLEAR are meant for lists. Reactive entities automatically create and wrap  ListenableLists and ListenableMaps in order to let you be able to monitor what changes inside of them. For example:

	@Entity
	class Visitors {
		@Match List<User> users
	}

Remember that User is also a reactive entity. Now, we can actually listen to changes to users inside this list inside this entity. For example:

	// create some data and listen for changes
	val visitors = new Visitors
	visitors.users = #[	new User(3, 'John'), new User(12, 'Mary') ]
	visitors.onChange [ println(it) ]

	// now change something
	visitors.users.findFirst[name=='Mary'].name = 'Helen'

This will print the following change:

	action: UPDATE
	path: #['users', 'name']
	value: 'Helen'

The interesting thing is that instead of this:

	visitors.users.findFirst[name=='Mary'].name = 'Helen'

We could also have done this:

	visitors.apply(new Change(UPDATE, #['users', 'name'], 'Helen'))

Reactive entities can have changes applied to them just like they can generate them. This allows entities to be managed, *even while already in memory*. That is, you can have a reactive entity loaded, and it can change automatically through another process, even while you are not touching it.

This can be pretty unwanted behavior. This is why entities can be marked static and even immutable.

You can say an entity is static like this: 	@Entity(type=static). A static entity is not reactive and will not even have an apply and onChange method.

You can mark an entity as immutable like this: @Entity(type=immutable). An immutable entity is automatically also static, and on top of that, it has no setters, so no data can be set after the fact.

However, where reactive entities come into their own is when it is wanted behaviour to have the same state accessible in many locations at once.

For example, a datamodel and having that reflected in a browser. Or in a database, and in the app without reloading the data.

Normally, a system like this is rife with problems due to multithreading issues. Deadlock and other timing problems are bound to happen. However, because we are using Vertx, we are always using a single thread, which alleviates a lot of these collision problems. The biggest problem left is latency over the line, so there can be race conditions when updating the same entity from multiple locations. For these reasons, it is best not to use this model when you need transaction-like data stability (don't use for financial or account transactions for example).

# Managed Reactive Entities

When entities are managed, you can have several representations of that entity and the manager keeps their state in sync. This means that when you update one instance of that entity, all other instances with the same key change as well, in memory.

A manager has the following methods:

- put(key, value)
- get(key)
- delete(key)

Well, and that is a basic manager. A manager manages a given type. When you get an entity by key, the manager listenes to changes you make to that entity. When you perform a change, the manager performs that change to all other outstanding versions of that entity.

For example:

	val manager = new Manager<User>
	val user = new User('Christian', 20)
	manager.add('user', user)

	val chris = manager.get('user')
	println(chris.age) // 20

	user.age = 40
	println(chris.age) // 40!

In the above example, you could argue this al is a bit convoluted and does not make much sense. After all, instead of saying:

	manager.add('user', user)
	val chris = manager.get('user')

You could also say:

	val chris = user

And indeed that would have the same effect. A manager acts like a memory manager. However the benefit of this approach is that a manager can be anything. It could be a proxy to a remote database. It could be a shared memory vertx module. The power of this mechanism is transparant, realtime distributed memory. Realtime in that changes come over the wire directly (unless you don't want to, which is adressed later). And transparant in that once you've made the connection, like performing the get('user) above, you no longer need to think about the network anymore.

An added bonus is that you may still monitor the entity for changes. This is the reactive part for your interface. Say that the user above is changed, you would want to have a way of being notified of this, so you can adapt a view the user has. You could do this:

	val manager = new Manager<User>
	val user = new User('Christian', 20)
	manager.add('user', user)

	val chris = manager.get('user')
	println(chris.age) // 20
	chris.onChange [ updateInterface(chris) ]

	user.age = 40
	println(chris.age) // 40!

# ListenableMap as a Manager

If you think about it, a normal Map already behaves like this. However if you want to send data over a network, you need to be able to stream the changes. That is where a ListenableMap comes in. It manages entities, and you can listen for the changes.

	val map = new ListenableMap<User>
	val user = new User('Christian', 20)
	map.put('user', user)
	
	val chris = map.get('user')
	println(chris.age) // 20
	map.onChange [ println('we got change ' + it) ]
	
	user.age = 40 // prints "UPDATE user.age 40"
	println(chris.age) // 40

In order to stream this data over a network, you have to connect it to another module that manages the same state. This is where vertx comes into play.

	// connect to the address 'users'
	val map1 = vertx.collection('users').map
	// connect to the same address
	val map2 = vertx.collection('users').map
	
Here we created two maps that both are ListenableMaps and both live on the addressbus on address 'users'. They try to keep the same state! If you make a change on one, it gets reflected in the other. For example:

	val chris1 = new User('Christian')
	map1.put('chris', chris1)

	//... latency

	val chris2 = map2.get('chris')
	assertEquals(chris1, chris2) // yes!

As you see, putting the user in map1 also made it available in map2! But it goes further.

	chris1.age = 20

	//.. latency

	assertEquals(20, chris2.age)

By setting the age of chris1, the change is hotwired across the bus, and chris2 gets updated!

In a way, the map acts as a cache. If it cannot find the data it is looking for, it will request it on the bus. If any other map on the same bus has this data, it will get it. Similary, any changes you make to items gets propagated over the bus and updates that same entity on other verticles.

This is made possible because changes can now be listened to and streamed across any medium, such as the Vert.x eventbus.

# From scratch

	val list = new LinkedList<User>
	
	vertx.collection('user', list) // make an existing list reactive

