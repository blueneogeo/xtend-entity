package nl.kii.entity

import nl.kii.observe.Observable
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

/**
 * Reactive here means able to respond to external changes and be listenable to for internal changes
 */
interface Reactive extends Procedure1<Change>, Observable<Change> {
	
	/** set if we want the object to publish changes */
	def void setPublishing(boolean publish)
	
	/** check if the object will publish internal changes to listeners */
	def boolean isPublishing()
	
}
