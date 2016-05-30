package nl.kii.observe

/** This code is here for backwards compatibility since xtend-async has a new observable. */
@Deprecated
interface Observable<T> {
	
	/** 
	 * Observe changes on the observable.
	 * @return a function that can be called to stop observing
	 */
	def =>void onChange((T)=>void observeFn)
	
}
