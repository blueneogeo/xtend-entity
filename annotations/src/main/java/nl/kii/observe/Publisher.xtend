package nl.kii.observe

import java.util.List
import java.util.concurrent.CopyOnWriteArrayList
import nl.kii.act.Actor
import nl.kii.async.annotation.Atomic
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

/** This code is here for backwards compatibility since xtend-async has a new publisher. */
class Publisher<T> extends Actor<T> implements Procedure1<T>, Observable<T> {
	
	@Atomic public val boolean publishing = true
	@Atomic transient val List<Procedure1<T>> observers 

	new() { }
	
	new(boolean isPublishing) {
		publishing = isPublishing
	}

	/** Listen for publications from the publisher */
	override =>void onChange((T)=>void observeFn) {
		if(observers == null) observers = new CopyOnWriteArrayList 
		observers.add(observeFn)
		return [| observers.remove(observeFn) ]

		// notice that we constantly create new lists. combined with the observers
		// list being atomic, this guarantees that the list is fully thread-safe.
		// CopyOnWriteArrayList is not atomic so it cannot be used. It is also locking
		// while this solution does not lock. It is not that expensive to keep copying
		// the list since it is usually only contains 2 to 3 listeners.
//		if(observers == null) observers = newLinkedList(observeFn) 
//		else observers = Lists.newLinkedList(observers) => [ add(observeFn) ]
//		return [| observers = Lists.newLinkedList(observers) => [ remove(observeFn) ] ]
	}
	
	override act(T message, =>void done) {
		if(observers != null && publishing) {
			for(observer : observers) {
				observer.apply(message)
			}
		} 
		done.apply
	}
	
	def getSubscriptionCount() { if(observers != null) observers.size else 0 }
	
	override toString() '''Publisher { publishing: «publishing», observers: «observers.size», inbox: «inbox.size» } '''
	
}