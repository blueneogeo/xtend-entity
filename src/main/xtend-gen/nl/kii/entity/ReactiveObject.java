package nl.kii.entity;

import com.google.common.base.Objects;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.entity.Change;
import nl.kii.entity.EntityObject;
import nl.kii.entity.Reactive;
import nl.kii.observe.Publisher;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

/**
 * Base implementation of ReactiveObject used by the @Reactive Annotation.
 * It will create an internal publisher when someone starts listening for changes.
 */
@SuppressWarnings("all")
public abstract class ReactiveObject implements Reactive, EntityObject {
  /**
   * we only have a publisher if someone wants to listen
   */
  protected final transient AtomicReference<Publisher<Change>> _publisher = new AtomicReference<Publisher<Change>>();
  
  /**
   * apply a change to this reactive object.
   */
  public abstract void apply(final Change p);
  
  /**
   * subscribe a listener for changes in the object
   * @return a procedure that can be called to unsubscribe the listener
   */
  public Procedure0 onChange(final Procedure1<? super Change> listener) {
    Publisher<Change> _publisher = this.getPublisher();
    return _publisher.onChange(listener);
  }
  
  /**
   * set if we want the object to publish changes
   */
  public void setPublishing(final boolean publish) {
    boolean _hasPublisher = this.hasPublisher();
    if (_hasPublisher) {
      Publisher<Change> _publisher = this.getPublisher();
      _publisher.setPublishing(publish);
    }
  }
  
  /**
   * check if the object will publish internal changes to listeners
   */
  public boolean isPublishing() {
    boolean _xifexpression = false;
    boolean _hasPublisher = this.hasPublisher();
    if (_hasPublisher) {
      Publisher<Change> _publisher = this.getPublisher();
      _xifexpression = _publisher.isPublishing();
    } else {
      _xifexpression = false;
    }
    return _xifexpression;
  }
  
  /**
   * create a publisher on demand
   */
  protected Publisher<Change> getPublisher() {
    Publisher<Change> _xblockexpression = null;
    {
      Publisher<Change> _get = this._publisher.get();
      boolean _equals = Objects.equal(_get, null);
      if (_equals) {
        Publisher<Change> _publisher = new Publisher<Change>();
        this._publisher.set(_publisher);
      }
      _xblockexpression = this._publisher.get();
    }
    return _xblockexpression;
  }
  
  /**
   * check if a publisher has been created. the publisher is lazily created on demand.
   */
  protected boolean hasPublisher() {
    Publisher<Change> _get = this._publisher.get();
    return (!Objects.equal(_get, null));
  }
}
