package nl.kii.entity;

import com.google.common.base.Objects;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import nl.kii.async.annotation.Atomic;
import nl.kii.entity.Change;
import nl.kii.entity.ChangeType;
import nl.kii.entity.EntityException;
import nl.kii.entity.EntityObject;
import nl.kii.entity.Reactive;
import nl.kii.observe.Observable;
import nl.kii.observe.Publisher;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

@SuppressWarnings("all")
public class EntityList<E extends Object> extends ArrayList<E> implements Reactive, EntityObject {
  private final Class<E> type;
  
  private final boolean isReactive;
  
  @Atomic
  private final transient AtomicReference<Publisher<Change>> _publisher = new AtomicReference<Publisher<Change>>();
  
  @Atomic
  private final transient AtomicReference<Map<Integer, Procedure0>> _subscriptionEnders = new AtomicReference<Map<Integer, Procedure0>>();
  
  public EntityList(final Class<E> type) {
    super();
    this.type = type;
    this.isReactive = true;
    HashMap<Integer, Procedure0> _newHashMap = CollectionLiterals.<Integer, Procedure0>newHashMap();
    this.setSubscriptionEnders(_newHashMap);
  }
  
  public EntityList(final Class<E> type, final int size) {
    super(size);
    this.type = type;
    this.isReactive = true;
    HashMap<Integer, Procedure0> _newHashMap = CollectionLiterals.<Integer, Procedure0>newHashMap();
    this.setSubscriptionEnders(_newHashMap);
  }
  
  public EntityList(final Class<E> type, final Collection<? extends E> coll) {
    super(coll);
    this.type = type;
    this.isReactive = true;
    HashMap<Integer, Procedure0> _newHashMap = CollectionLiterals.<Integer, Procedure0>newHashMap();
    this.setSubscriptionEnders(_newHashMap);
  }
  
  public Class<E> getType() {
    return this.type;
  }
  
  private void publish(final Change change) {
    Publisher<Change> _publisher = this.getPublisher();
    if (_publisher!=null) {
      _publisher.apply(change);
    }
  }
  
  @Override
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
  
  @Override
  public void setPublishing(final boolean publish) {
    boolean _and = false;
    Publisher<Change> _publisher = this.getPublisher();
    boolean _equals = Objects.equal(_publisher, null);
    if (!_equals) {
      _and = false;
    } else {
      _and = (!publish);
    }
    if (_and) {
      Publisher<Change> _publisher_1 = new Publisher<Change>();
      this.setPublisher(_publisher_1);
    }
    Publisher<Change> _publisher_2 = this.getPublisher();
    boolean _notEquals = (!Objects.equal(_publisher_2, null));
    if (_notEquals) {
      Publisher<Change> _publisher_3 = this.getPublisher();
      _publisher_3.setPublishing(Boolean.valueOf(publish));
    }
  }
  
  @Override
  public boolean isPublishing() {
    boolean _and = false;
    Publisher<Change> _publisher = this.getPublisher();
    boolean _notEquals = (!Objects.equal(_publisher, null));
    if (!_notEquals) {
      _and = false;
    } else {
      Publisher<Change> _publisher_1 = this.getPublisher();
      Boolean _publishing = _publisher_1.getPublishing();
      _and = (_publishing).booleanValue();
    }
    return _and;
  }
  
  private Procedure0 observe(final E element) {
    Procedure0 _switchResult = null;
    boolean _matched = false;
    if (!_matched) {
      if (element instanceof Observable) {
        _matched=true;
        final Procedure1<Change> _function = new Procedure1<Change>() {
          @Override
          public void apply(final Change change) {
            int _indexOf = EntityList.this.indexOf(element);
            final String path = Integer.valueOf(_indexOf).toString();
            Change _addPath = change.addPath(path);
            EntityList.this.publish(_addPath);
          }
        };
        _switchResult = ((Observable<Change>)element).onChange(_function);
      }
    }
    return _switchResult;
  }
  
  @Override
  public E set(final int index, final E value) {
    E _xblockexpression = null;
    {
      Map<Integer, Procedure0> _subscriptionEnders = this.getSubscriptionEnders();
      Procedure0 _get = _subscriptionEnders.get(Integer.valueOf(index));
      if (_get!=null) {
        _get.apply();
      }
      final E previous = super.set(index, value);
      final Procedure0 subscriptionEnder = this.observe(value);
      Map<Integer, Procedure0> _subscriptionEnders_1 = this.getSubscriptionEnders();
      _subscriptionEnders_1.put(Integer.valueOf(index), subscriptionEnder);
      Change _change = new Change(ChangeType.UPDATE, index, value);
      this.publish(_change);
      _xblockexpression = previous;
    }
    return _xblockexpression;
  }
  
  @Override
  public boolean add(final E element) {
    boolean _xblockexpression = false;
    {
      final boolean success = super.add(element);
      if ((!success)) {
        return false;
      }
      final Procedure0 subscriptionEnder = this.observe(element);
      final int index = this.indexOf(element);
      Map<Integer, Procedure0> _subscriptionEnders = this.getSubscriptionEnders();
      _subscriptionEnders.put(Integer.valueOf(index), subscriptionEnder);
      Change _change = new Change(ChangeType.ADD, element);
      this.publish(_change);
      _xblockexpression = true;
    }
    return _xblockexpression;
  }
  
  @Override
  public void add(final int index, final E value) {
    Map<Integer, Procedure0> _subscriptionEnders = this.getSubscriptionEnders();
    Procedure0 _get = _subscriptionEnders.get(Integer.valueOf(index));
    if (_get!=null) {
      _get.apply();
    }
    super.add(index, value);
    final Procedure0 subscriptionEnder = this.observe(value);
    Map<Integer, Procedure0> _subscriptionEnders_1 = this.getSubscriptionEnders();
    _subscriptionEnders_1.put(Integer.valueOf(index), subscriptionEnder);
    Change _change = new Change(ChangeType.UPDATE, index, value);
    this.publish(_change);
  }
  
  @Override
  public E remove(final int index) {
    E _xblockexpression = null;
    {
      Map<Integer, Procedure0> _subscriptionEnders = this.getSubscriptionEnders();
      Procedure0 _get = _subscriptionEnders.get(Integer.valueOf(index));
      if (_get!=null) {
        _get.apply();
      }
      final E previous = super.remove(index);
      boolean _notEquals = (!Objects.equal(previous, null));
      if (_notEquals) {
        Change _change = new Change(ChangeType.REMOVE, index, previous);
        this.publish(_change);
      }
      _xblockexpression = previous;
    }
    return _xblockexpression;
  }
  
  @Override
  public boolean remove(final Object o) {
    boolean _xblockexpression = false;
    {
      final int index = this.indexOf(o);
      if ((index < 0)) {
        return false;
      }
      Map<Integer, Procedure0> _subscriptionEnders = this.getSubscriptionEnders();
      Procedure0 _get = _subscriptionEnders.get(Integer.valueOf(index));
      if (_get!=null) {
        _get.apply();
      }
      final boolean success = super.remove(o);
      if ((!success)) {
        return false;
      }
      Change _change = new Change(ChangeType.REMOVE, index, o);
      this.publish(_change);
      _xblockexpression = true;
    }
    return _xblockexpression;
  }
  
  @Override
  public void clear() {
    Map<Integer, Procedure0> _subscriptionEnders = this.getSubscriptionEnders();
    final BiConsumer<Integer, Procedure0> _function = new BiConsumer<Integer, Procedure0>() {
      @Override
      public void accept(final Integer k, final Procedure0 v) {
        if (v!=null) {
          v.apply();
        }
      }
    };
    _subscriptionEnders.forEach(_function);
    Map<Integer, Procedure0> _subscriptionEnders_1 = this.getSubscriptionEnders();
    _subscriptionEnders_1.clear();
    super.clear();
    Change _change = new Change(ChangeType.CLEAR);
    this.publish(_change);
  }
  
  @Override
  public boolean addAll(final Collection<? extends E> c) {
    boolean _xblockexpression = false;
    {
      for (final E it : c) {
        this.add(it);
      }
      _xblockexpression = true;
    }
    return _xblockexpression;
  }
  
  @Override
  public boolean removeAll(final Collection<?> c) {
    boolean _xblockexpression = false;
    {
      for (final Object it : c) {
        this.remove(it);
      }
      _xblockexpression = true;
    }
    return _xblockexpression;
  }
  
  @Deprecated
  @Override
  public boolean addAll(final int index, final Collection<? extends E> c) {
    throw new UnsupportedOperationException();
  }
  
  @Deprecated
  @Override
  public boolean retainAll(final Collection<?> c) {
    throw new UnsupportedOperationException();
  }
  
  private int getIndex(final Change change) {
    try {
      int _xtrycatchfinallyexpression = (int) 0;
      try {
        List<String> _path = change.getPath();
        String _head = IterableExtensions.<String>head(_path);
        _xtrycatchfinallyexpression = Integer.parseInt(_head);
      } catch (final Throwable _t) {
        if (_t instanceof Exception) {
          final Exception e = (Exception)_t;
          throw new EntityException(("could not parse list index from change path, for " + change));
        } else {
          throw Exceptions.sneakyThrow(_t);
        }
      }
      return _xtrycatchfinallyexpression;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Override
  public void apply(final Change change) {
    try {
      boolean _and = false;
      Publisher<Change> _publisher = this.getPublisher();
      boolean _notEquals = (!Objects.equal(_publisher, null));
      if (!_notEquals) {
        _and = false;
      } else {
        Publisher<Change> _publisher_1 = this.getPublisher();
        Boolean _publishing = _publisher_1.getPublishing();
        boolean _not = (!(_publishing).booleanValue());
        _and = _not;
      }
      final boolean wasPublishing = _and;
      try {
        Publisher<Change> _publisher_2 = this.getPublisher();
        if (_publisher_2!=null) {
          _publisher_2.setPublishing(Boolean.valueOf(false));
        }
        List<String> _path = change.getPath();
        final List<String> path = _path;
        boolean _matched = false;
        if (!_matched) {
          if (Objects.equal(path, null)) {
            _matched=true;
          }
          if (!_matched) {
            int _size = path.size();
            boolean _equals = (_size == 0);
            if (_equals) {
              _matched=true;
            }
          }
          if (_matched) {
            ChangeType _action = change.getAction();
            if (_action != null) {
              switch (_action) {
                case ADD:
                  Object _value = change.getValue();
                  Class<?> _class = _value.getClass();
                  boolean _isAssignableFrom = _class.isAssignableFrom(this.type);
                  boolean _not_1 = (!_isAssignableFrom);
                  if (_not_1) {
                    String _simpleName = this.type.getSimpleName();
                    String _plus = ("value is not of correct type " + _simpleName);
                    String _plus_1 = (_plus + ", could not apply ");
                    String _plus_2 = (_plus_1 + change);
                    throw new EntityException(_plus_2);
                  }
                  Object _value_1 = change.getValue();
                  this.add(((E) _value_1));
                  break;
                case UPDATE:
                  Object _value_2 = change.getValue();
                  if ((!(_value_2 instanceof List<?>))) {
                    throw new EntityException(("value is not a list, could not apply " + change));
                  }
                  Object _value_3 = change.getValue();
                  final List<E> list = ((List<E>) _value_3);
                  boolean _and_1 = false;
                  boolean _isEmpty = list.isEmpty();
                  boolean _not_2 = (!_isEmpty);
                  if (!_not_2) {
                    _and_1 = false;
                  } else {
                    E _head = IterableExtensions.<E>head(list);
                    Class<?> _class_1 = _head.getClass();
                    boolean _isAssignableFrom_1 = _class_1.isAssignableFrom(this.type);
                    boolean _not_3 = (!_isAssignableFrom_1);
                    _and_1 = _not_3;
                  }
                  if (_and_1) {
                    String _name = this.type.getName();
                    String _plus_3 = ("change value is a list of the wrong type, expecting a List<" + _name);
                    String _plus_4 = (_plus_3 + "> but got a List<");
                    E _head_1 = IterableExtensions.<E>head(list);
                    Class<?> _class_2 = _head_1.getClass();
                    String _name_1 = _class_2.getName();
                    String _plus_5 = (_plus_4 + _name_1);
                    String _plus_6 = (_plus_5 + "> instead. For ");
                    String _plus_7 = (_plus_6 + change);
                    throw new EntityException(_plus_7);
                  }
                  this.clear();
                  this.addAll(list);
                  break;
                case REMOVE:
                  throw new EntityException(("cannot remove, change contains no index: " + change));
                case CLEAR:
                  this.clear();
                  break;
                default:
                  break;
              }
            }
          }
        }
        if (!_matched) {
          int _size_1 = path.size();
          boolean _equals_1 = (_size_1 == 1);
          if (_equals_1) {
            _matched=true;
            ChangeType _action_1 = change.getAction();
            if (_action_1 != null) {
              switch (_action_1) {
                case ADD:
                  int _index = this.getIndex(change);
                  final E value = this.get(_index);
                  boolean _equals_2 = Objects.equal(value, null);
                  if (_equals_2) {
                    throw new EntityException(("path points to an empty value in the map, could not apply " + change));
                  }
                  this.applyToValue(change, value);
                  break;
                case UPDATE:
                  Object _value_4 = change.getValue();
                  Class<?> _class_3 = _value_4.getClass();
                  boolean _isAssignableFrom_2 = _class_3.isAssignableFrom(this.type);
                  boolean _not_4 = (!_isAssignableFrom_2);
                  if (_not_4) {
                    String _simpleName_1 = this.type.getSimpleName();
                    String _plus_8 = ("value is not of correct type " + _simpleName_1);
                    String _plus_9 = (_plus_8 + ", could not apply ");
                    String _plus_10 = (_plus_9 + change);
                    throw new EntityException(_plus_10);
                  }
                  int _index_1 = this.getIndex(change);
                  Object _value_5 = change.getValue();
                  this.set(_index_1, ((E) _value_5));
                  break;
                case REMOVE:
                case CLEAR:
                  final int index = this.getIndex(change);
                  E _get = this.get(index);
                  boolean _notEquals_1 = (!Objects.equal(_get, null));
                  if (_notEquals_1) {
                    this.remove(index);
                  }
                  break;
                default:
                  break;
              }
            }
          }
        }
        if (!_matched) {
          int _size_2 = path.size();
          boolean _greaterThan = (_size_2 > 1);
          if (_greaterThan) {
            _matched=true;
            int _index_2 = this.getIndex(change);
            final E value_1 = this.get(_index_2);
            boolean _equals_3 = Objects.equal(value_1, null);
            if (_equals_3) {
              throw new EntityException(("path points to an empty value in the map, could not apply " + change));
            }
            this.applyToValue(change, value_1);
          }
        }
      } finally {
        Publisher<Change> _publisher_3 = this.getPublisher();
        if (_publisher_3!=null) {
          _publisher_3.setPublishing(Boolean.valueOf(wasPublishing));
        }
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  private void applyToValue(final Change change, final E value) {
    try {
      if ((!(value instanceof Reactive))) {
        throw new EntityException(("path points inside an object that is not Reactive, could not apply " + change));
      }
      final Reactive reactive = ((Reactive) value);
      Change _forward = change.forward();
      reactive.apply(_forward);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Override
  public EntityList<E> clone() {
    Object _clone = super.clone();
    return ((EntityList<E>) _clone);
  }
  
  @Override
  public void validate() {
  }
  
  @Override
  public boolean isValid() {
    return true;
  }
  
  @Override
  public Class<?> getInstanceType(final List<String> path) throws EntityException {
    try {
      Class<?> _switchResult = null;
      final List<String> it = path;
      boolean _matched = false;
      if (!_matched) {
        if (Objects.equal(it, null)) {
          _matched=true;
        }
        if (!_matched) {
          int _length = ((Object[])Conversions.unwrapArray(it, Object.class)).length;
          boolean _equals = (_length == 0);
          if (_equals) {
            _matched=true;
          }
        }
        if (_matched) {
          _switchResult = EntityList.class;
        }
      }
      if (!_matched) {
        int _length_1 = ((Object[])Conversions.unwrapArray(it, Object.class)).length;
        boolean _equals_1 = (_length_1 == 1);
        if (_equals_1) {
          _matched=true;
          _switchResult = this.type;
        }
      }
      if (!_matched) {
        Class<?> _xifexpression = null;
        boolean _isAssignableFrom = EntityObject.class.isAssignableFrom(this.type);
        if (_isAssignableFrom) {
          E _newInstance = this.type.newInstance();
          Iterable<String> _tail = IterableExtensions.<String>tail(path);
          List<String> _list = IterableExtensions.<String>toList(_tail);
          _xifexpression = ((EntityObject) _newInstance).getInstanceType(_list);
        } else {
          Iterable<String> _tail_1 = IterableExtensions.<String>tail(path);
          String _plus = ("EntityList cannot apply path " + _tail_1);
          String _plus_1 = (_plus + " to type ");
          String _plus_2 = (_plus_1 + this.type);
          throw new EntityException(_plus_2);
        }
        _switchResult = _xifexpression;
      }
      return _switchResult;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  private void setPublisher(final Publisher<Change> value) {
    this._publisher.set(value);
  }
  
  private Publisher<Change> getPublisher() {
    return this._publisher.get();
  }
  
  private Publisher<Change> getAndSetPublisher(final Publisher<Change> value) {
    return this._publisher.getAndSet(value);
  }
  
  private void setSubscriptionEnders(final Map<Integer, Procedure0> value) {
    this._subscriptionEnders.set(value);
  }
  
  private Map<Integer, Procedure0> getSubscriptionEnders() {
    return this._subscriptionEnders.get();
  }
  
  private Map<Integer, Procedure0> getAndSetSubscriptionEnders(final Map<Integer, Procedure0> value) {
    return this._subscriptionEnders.getAndSet(value);
  }
}
