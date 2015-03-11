package nl.kii.entity;

import com.google.common.base.Objects;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.async.annotation.Atomic;
import nl.kii.entity.Change;
import nl.kii.entity.ChangeType;
import nl.kii.entity.EntityException;
import nl.kii.entity.EntityList;
import nl.kii.entity.EntityObject;
import nl.kii.entity.Reactive;
import nl.kii.observe.Observable;
import nl.kii.observe.Publisher;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.MapExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;

@SuppressWarnings("all")
public class EntityMap<V extends Object> extends HashMap<String, V> implements Reactive, EntityObject {
  private final Class<V> type;
  
  private final boolean isReactive;
  
  @Atomic
  private final transient AtomicReference<Publisher<Change>> _publisher = new AtomicReference<Publisher<Change>>();
  
  @Atomic
  private final transient AtomicReference<Map<String, Procedure0>> _subscriptionEnders = new AtomicReference<Map<String, Procedure0>>();
  
  public EntityMap(final Class<V> type) {
    super();
    this.type = type;
    this.isReactive = true;
    HashMap<String, Procedure0> _newHashMap = CollectionLiterals.<String, Procedure0>newHashMap();
    this.setSubscriptionEnders(_newHashMap);
  }
  
  public EntityMap(final Class<V> type, final int size) {
    super(size);
    this.type = type;
    this.isReactive = true;
  }
  
  public EntityMap(final Class<V> type, final Map<? extends String, ? extends V> m) {
    this.type = type;
    this.isReactive = true;
  }
  
  public Class<V> getType() {
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
  
  private Procedure0 observe(final V element, final String key) {
    Procedure0 _switchResult = null;
    boolean _matched = false;
    if (!_matched) {
      if (element instanceof Observable) {
        _matched=true;
        final Procedure1<Change> _function = new Procedure1<Change>() {
          @Override
          public void apply(final Change change) {
            Change _addPath = change.addPath(key);
            EntityMap.this.publish(_addPath);
          }
        };
        _switchResult = ((Observable<Change>)element).onChange(_function);
      }
    }
    return _switchResult;
  }
  
  @Override
  public V put(final String key, final V value) {
    V _xblockexpression = null;
    {
      Map<String, Procedure0> _subscriptionEnders = this.getSubscriptionEnders();
      Procedure0 _get = _subscriptionEnders.get(key);
      if (_get!=null) {
        _get.apply();
      }
      final V previous = super.put(key, value);
      final Procedure0 subscriptionEnder = this.observe(value, key);
      Map<String, Procedure0> _subscriptionEnders_1 = this.getSubscriptionEnders();
      _subscriptionEnders_1.put(key, subscriptionEnder);
      Change _change = new Change(ChangeType.UPDATE, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(key)), value);
      this.publish(_change);
      _xblockexpression = previous;
    }
    return _xblockexpression;
  }
  
  @Override
  public V remove(final Object key) {
    V _xblockexpression = null;
    {
      Map<String, Procedure0> _subscriptionEnders = this.getSubscriptionEnders();
      Procedure0 _get = _subscriptionEnders.get(key);
      if (_get!=null) {
        _get.apply();
      }
      final V previous = super.remove(key);
      boolean _notEquals = (!Objects.equal(previous, null));
      if (_notEquals) {
        String _string = key.toString();
        Change _change = new Change(ChangeType.REMOVE, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_string)), previous);
        this.publish(_change);
      }
      _xblockexpression = previous;
    }
    return _xblockexpression;
  }
  
  @Override
  public void putAll(final Map<? extends String, ? extends V> m) {
    Set<? extends String> _keySet = m.keySet();
    for (final String key : _keySet) {
      V _get = m.get(key);
      this.put(key, _get);
    }
  }
  
  @Override
  public void clear() {
    Map<String, Procedure0> _subscriptionEnders = this.getSubscriptionEnders();
    final Procedure2<String, Procedure0> _function = new Procedure2<String, Procedure0>() {
      @Override
      public void apply(final String k, final Procedure0 v) {
        if (v!=null) {
          v.apply();
        }
      }
    };
    MapExtensions.<String, Procedure0>forEach(_subscriptionEnders, _function);
    Map<String, Procedure0> _subscriptionEnders_1 = this.getSubscriptionEnders();
    _subscriptionEnders_1.clear();
    super.clear();
    Change _change = new Change(ChangeType.CLEAR, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()), null);
    this.publish(_change);
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
          boolean _equals = Objects.equal(path, null);
          if (_equals) {
            _matched=true;
          }
          if (!_matched) {
            int _size = path.size();
            boolean _equals_1 = (_size == 0);
            if (_equals_1) {
              _matched=true;
            }
          }
          if (_matched) {
            ChangeType _action = change.getAction();
            if (_action != null) {
              switch (_action) {
                case ADD:
                  throw new EntityException(("map does not support ADD, use UPDATE, for " + change));
                case UPDATE:
                  Object _value = change.getValue();
                  if ((!(_value instanceof Map<?, ?>))) {
                    throw new EntityException(("value is not a map, could not apply " + change));
                  }
                  this.clear();
                  Object _value_1 = change.getValue();
                  final Map<String, V> map = ((Map<String, V>) _value_1);
                  boolean _and_1 = false;
                  boolean _isEmpty = map.isEmpty();
                  boolean _not_1 = (!_isEmpty);
                  if (!_not_1) {
                    _and_1 = false;
                  } else {
                    Collection<V> _values = map.values();
                    V _head = IterableExtensions.<V>head(_values);
                    Class<?> _class = _head.getClass();
                    boolean _isAssignableFrom = _class.isAssignableFrom(this.type);
                    boolean _not_2 = (!_isAssignableFrom);
                    _and_1 = _not_2;
                  }
                  if (_and_1) {
                    String _name = this.type.getName();
                    String _plus = ("change value is a list of the wrong type, expecting a List<" + _name);
                    String _plus_1 = (_plus + "> but got a List<");
                    Collection<V> _values_1 = map.values();
                    V _head_1 = IterableExtensions.<V>head(_values_1);
                    Class<?> _class_1 = _head_1.getClass();
                    String _name_1 = _class_1.getName();
                    String _plus_2 = (_plus_1 + _name_1);
                    String _plus_3 = (_plus_2 + "> instead. For ");
                    String _plus_4 = (_plus_3 + change);
                    throw new EntityException(_plus_4);
                  }
                  this.putAll(map);
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
          boolean _equals_2 = (_size_1 == 1);
          if (_equals_2) {
            _matched=true;
            ChangeType _action_1 = change.getAction();
            if (_action_1 != null) {
              switch (_action_1) {
                case ADD:
                  throw new EntityException(("map does not support ADD, use UPDATE, for " + change));
                case UPDATE:
                  Object _value_2 = change.getValue();
                  Class<?> _class_2 = _value_2.getClass();
                  boolean _isAssignableFrom_1 = _class_2.isAssignableFrom(this.type);
                  boolean _not_3 = (!_isAssignableFrom_1);
                  if (_not_3) {
                    String _simpleName = this.type.getSimpleName();
                    String _plus_5 = ("value is not of correct type " + _simpleName);
                    String _plus_6 = (_plus_5 + ", could not apply ");
                    String _plus_7 = (_plus_6 + change);
                    throw new EntityException(_plus_7);
                  }
                  List<String> _path_1 = change.getPath();
                  String _head_2 = IterableExtensions.<String>head(_path_1);
                  Object _value_3 = change.getValue();
                  this.put(_head_2, ((V) _value_3));
                  break;
                case REMOVE:
                case CLEAR:
                  List<String> _path_2 = change.getPath();
                  final String key = IterableExtensions.<String>head(_path_2);
                  boolean _containsKey = this.containsKey(key);
                  if (_containsKey) {
                    List<String> _path_3 = change.getPath();
                    String _head_3 = IterableExtensions.<String>head(_path_3);
                    this.remove(_head_3);
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
            List<String> _path_4 = change.getPath();
            String _head_4 = IterableExtensions.<String>head(_path_4);
            final V value = this.get(_head_4);
            boolean _equals_3 = Objects.equal(value, null);
            if (_equals_3) {
              throw new EntityException(("path points to an empty value in the map, could not apply " + change));
            }
            if ((!(value instanceof Reactive))) {
              throw new EntityException(("path points inside an object that is not Reactive, could not apply " + change));
            }
            final Reactive reactive = ((Reactive) value);
            Change _forward = change.forward();
            reactive.apply(_forward);
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
  
  @Override
  public EntityMap<V> clone() {
    Object _clone = super.clone();
    return ((EntityMap<V>) _clone);
  }
  
  @Override
  public void validate() {
  }
  
  @Override
  public boolean equals(final Object o) {
    return super.equals(o);
  }
  
  @Override
  public int hashCode() {
    return super.hashCode();
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
          V _newInstance = this.type.newInstance();
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
  
  private void setSubscriptionEnders(final Map<String, Procedure0> value) {
    this._subscriptionEnders.set(value);
  }
  
  private Map<String, Procedure0> getSubscriptionEnders() {
    return this._subscriptionEnders.get();
  }
  
  private Map<String, Procedure0> getAndSetSubscriptionEnders(final Map<String, Procedure0> value) {
    return this._subscriptionEnders.getAndSet(value);
  }
}
