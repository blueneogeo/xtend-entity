package nl.kii.act

import java.util.Queue
import nl.kii.async.annotation.Atomic
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

import static com.google.common.collect.Queues.*

/** This code is here for backwards compatibility since xtend-async no longer has the actor. */
@Deprecated
abstract class Actor<T> implements Procedure1<T> {

	/** 
	 * Since the actor supports asynchronous callbacks, its main loop must be implemented via 
	 * recursion. However since it has no seperate process loop (as it steals from the threads
	 * that call it), this can mean that the recursion can go deeper than Java allows. The correct
	 * way of dealing with this would be via tail optimization, however this is unsupported by
	 * the JVM. Another good way of dealing with the growing stacktrace is by using continuations.
	 * That way, you can have a looping continuation instead of recursion. However, this is only
	 * possible using ASM bytecode enhancement, which I tried to avoid. Because of this, this
	 * actor uses a dirty trick to escape too long recursion stacktraces: it throws an exception
	 * when the stack is too deep, breaking out of the call-stack back to the loop. However since
	 * recursion is more performant than constantly throwing exceptions, it will only do so after
	 * an X amount of process depth. The MAX_PROCESS_DEPTH setting below sets how many time the
	 * processNextAsync call can be called recursively before it breaks out through an exception. 
	 */
	val static MAX_PROCESS_DEPTH = 10
	
	/**
	 * The queue of messages waiting to be processed by this actor.
	 * Must be a threadsafe queue.
	 */
	val Queue<T> inbox
	
	/**
	 * Indicates if this actor is busy processing the queue.
	 */
	@Atomic val boolean processing = false
	
	/** Create a new actor with a concurrentlinkedqueue as inbox */
	new() {	
		this(newConcurrentLinkedQueue)
	}

	/** 
	 * Create an actor with the given queue as inbox. 
	 * Queue implementation must be threadsafe and non-blocking.
	 */
	new(Queue<T> queue) { 
		inbox = queue
	}
	
	/** Give the actor something to process */
	override apply(T message) {
		inbox.add(message)
		process // let the applying thread do some work!
	}
	
	/** 
	 * Perform the actor on the next message in the inbox. 
	 * You must call done.apply when you have completed processing!
	 */
	protected abstract def void act(T message, =>void done)

	/**
	 * Start processing as many messages as possible before releasing the thread.
	 */
	protected def void process() {
		// this outer loop is for re-entering the process loop after a AtMaxProcessDepth exception is thrown
		// while(!processing && !inbox.empty) {
		while(!inbox.empty) {
			try {
				// processNextAsync will try to recurse through the entire inbox,
				processNextAsync(MAX_PROCESS_DEPTH)
				// in which case we are done processing and looping...
				// processing = false
				return;
			} catch (AtMaxProcessDepth e) {
				// ... or we end up at max recursion, in which case we are not finished and the while tries again
				// println('was at max depth, coming back up!')
				// processing = false
			}
		}
	}

	/**
	 * Process a single message recursively by calling itself until either the inbox is empty,
	 * or the maximum depth is achieved, in which case an AtMaxProcessDepth exception is thrown.
	 */
	protected def boolean processNextAsync(int depth) {
		if(depth == 0) throw new AtMaxProcessDepth
		// check if we were already processing, otherwise set as processing
		val allowed = _processing.compareAndSet(false, true)
		if(!allowed) return false
		// try to get the next message
		val message = inbox.poll
		if(message == null) return false
		// ok, we have a message, start processing
		act(message) [|
			// the act is done, stop processing
			processing = false
			// if there is more to do, call this method again
			if(!inbox.empty)
				processNextAsync(depth - 1)
		]
		true
	}
	
	/**
	 * Get the current inbox. The returned collection is unmodifiable. 
	 */
	def getInbox() {
		inbox.unmodifiableView
	}
	
	val static MAX_INBOX_TO_PRINT = 10
	
	override toString() '''
	Actor {
		processing: «processing»,
		inbox size: «inbox.size»,
		«IF inbox.size < Actor.MAX_INBOX_TO_PRINT» 
			inbox: {
				«FOR item : inbox SEPARATOR ','»
					«item»
				«ENDFOR»
			}
		«ELSE»
			inbox: { larger than «MAX_INBOX_TO_PRINT» items }
		«ENDIF»
	}
	'''
	
}

/** 
 * Thrown to indicate that the stack for the recursive calls is at its maximum and that it should
 * be rewound.
 */
class AtMaxProcessDepth extends Throwable { }