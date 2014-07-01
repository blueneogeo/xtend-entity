package nl.kii.reactive

import nl.kii.observe.Observable
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

/**
 * Reactive here means able to respond to external changes and be listenable to for internal changes
 */
interface Reactive extends Procedure1<Change>, Observable<Change> {
	
}
