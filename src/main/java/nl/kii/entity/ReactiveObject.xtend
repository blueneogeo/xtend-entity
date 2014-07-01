package nl.kii.entity

import java.util.concurrent.atomic.AtomicReference
import nl.kii.observe.Publisher
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

/**
 * Base implementation of ReactiveObject used by the @Reactive Annotation.
 * It will create an internal publisher when someone starts listening for changes.
 */
abstract class ReactiveObject implements Reactive, EntityObject {
	
	/** we only have a publisher if someone wants to listen */
	transient protected val _publisher = new AtomicReference<Publisher<Change>>

	/**
	 * apply a change to this reactive object. 
	 */
	override apply(Change p)
	
	/** 
	 * subscribe a listener for changes in the object
	 * @return a procedure that can be called to unsubscribe the listener
	 */
	override =>void onChange(Procedure1<? super Change> listener) {
		publisher.onChange(listener)
	}

	/** set if we want the object to publish changes */
	def setPublishing(boolean publish) {
		if(hasPublisher)
			publisher.publishing = publish
	}
	
	/** check if the object will publish internal changes to listeners */
	def isPublishing() {
		if(hasPublisher)
			publisher.isPublishing
		else false
	}

	/** create a publisher on demand */
	protected def getPublisher() {
		if(_publisher.get == null) 
			_publisher.set(new Publisher<Change>)
		_publisher.get
	}
	
	/** check if a publisher has been created. the publisher is lazily created on demand. */
	protected def hasPublisher() {
		_publisher.get != null
	}

}
