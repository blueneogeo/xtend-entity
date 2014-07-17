package nl.kii.entity

import nl.kii.async.annotation.Atomic
import nl.kii.observe.Publisher
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

/**
 * Base implementation of ReactiveObject used by the @Reactive Annotation.
 * It will create an internal publisher when someone starts listening for changes.
 */
abstract class ReactiveObject implements Reactive, EntityObject {
	
	/** we only have a publisher if someone wants to listen */
	@Atomic transient protected Publisher<Change> publisher

	/**
	 * apply a change to this reactive object. 
	 */
	override apply(Change p)
	
	/** 
	 * subscribe a listener for changes in the object
	 * @return a procedure that can be called to unsubscribe the listener
	 */
	override =>void onChange(Procedure1<? super Change> listener) {
		if(publisher == null) publisher = new Publisher<Change>
		publisher.onChange(listener)
	}

	/** set if we want the object to publish changes */
	override setPublishing(boolean publish) {
		if(hasPublisher) publisher.publishing = publish
	}
	
	/** check if the object will publish internal changes to listeners */
	override isPublishing() {
		hasPublisher &&  publisher.publishing
	}

	/** check if a publisher has been created. the publisher is lazily created on demand. */
	protected def hasPublisher() {
		publisher != null
	}

}
