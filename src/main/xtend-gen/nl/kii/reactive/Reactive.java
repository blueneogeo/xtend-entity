package nl.kii.reactive;

import nl.kii.observe.Observable;
import nl.kii.reactive.Change;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

/**
 * Reactive here means able to respond to external changes and be listenable to for internal changes
 */
@SuppressWarnings("all")
public interface Reactive extends Procedure1<Change>, Observable<Change> {
}
