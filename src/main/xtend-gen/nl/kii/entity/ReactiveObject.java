package nl.kii.entity;

import com.google.common.base.Objects;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.async.annotation.Atomic;
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
  @Atomic
  private final transient AtomicReference<Publisher<Change>> _publisher = new AtomicReference<Publisher<Change>>();
  
  /**
   * apply a change to this reactive object.
   */
  public abstract void apply(final Change p);
  
  /**
   * subscribe a listener for changes in the object
   * @return a procedure that can be called to unsubscribe the listener
   */
  public Procedure0 onChange(final Procedure1<? super Change> listener) {
    Procedure0 _xblockexpression = null;
    {
      Publisher<Change> _publisher = this.getPublisher();
      boolean _equals = Objects.equal(_publisher, null);
      if (_equals) {
        Publisher<Change> _publisher_1 = new Publisher<Change>();
        this.setPublisher(_publisher_1);
      }
      Publisher<Change> _publisher_2 = this.getPublisher();
      _xblockexpression = _publisher_2.onChange(listener);
    }
    return _xblockexpression;
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
    boolean _and = false;
    boolean _hasPublisher = this.hasPublisher();
    if (!_hasPublisher) {
      _and = false;
    } else {
      Publisher<Change> _publisher = this.getPublisher();
      boolean _isPublishing = _publisher.isPublishing();
      _and = _isPublishing;
    }
    return _and;
  }
  
  /**
   * check if a publisher has been created. the publisher is lazily created on demand.
   */
  protected boolean hasPublisher() {
    Publisher<Change> _publisher = this.getPublisher();
    return (!Objects.equal(_publisher, null));
  }
  
  protected Publisher<Change> setPublisher(final Publisher<Change> value) {
    return this._publisher.getAndSet(value);
  }
  
  protected Publisher<Change> getPublisher() {
    return this._publisher.get();
  }
}
