package nl.kii.reactive;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.observe.Observable;
import nl.kii.observe.Publisher;
import nl.kii.reactive.Change;
import nl.kii.reactive.ChangeType;
import nl.kii.reactive.EntityException;
import nl.kii.reactive.Reactive;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.MapExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;

@SuppressWarnings("all")
public class EntityMap<V extends Object> extends HashMap<String, V> implements Reactive {
  private final Class<V> type;
  
  private final boolean isReactive;
  
  private final transient AtomicReference<Publisher<Change>> _publisher = new AtomicReference<Publisher<Change>>();
  
  private transient Map<String, Procedure0> subscriptionEnders = CollectionLiterals.<String, Procedure0>newHashMap();
  
  public EntityMap(final Class<V> type) {
    super();
    this.type = type;
    this.isReactive = true;
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
  
  private Publisher<Change> getPublisher() {
    return this._publisher.get();
  }
  
  private void initPublisher() {
    Publisher<Change> _get = this._publisher.get();
    boolean _equals = Objects.equal(_get, null);
    if (_equals) {
      Publisher<Change> _publisher = new Publisher<Change>();
      this._publisher.set(_publisher);
    }
  }
  
  public Procedure0 onChange(final Procedure1<? super Change> listener) {
    Procedure0 _xblockexpression = null;
    {
      this.initPublisher();
      Publisher<Change> _publisher = this.getPublisher();
      _xblockexpression = _publisher.onChange(listener);
    }
    return _xblockexpression;
  }
  
  private void publish(final Change change) {
    Publisher<Change> _publisher = this.getPublisher();
    if (_publisher!=null) {
      _publisher.apply(change);
    }
  }
  
  private Procedure0 observe(final V element, final String key) {
    Procedure0 _switchResult = null;
    boolean _matched = false;
    if (!_matched) {
      if (element instanceof Observable) {
        _matched=true;
        final Procedure1<Change> _function = new Procedure1<Change>() {
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
  
  public V put(final String key, final V value) {
    V _xblockexpression = null;
    {
      Procedure0 _get = this.subscriptionEnders.get(key);
      if (_get!=null) {
        _get.apply();
      }
      final V previous = super.put(key, value);
      final Procedure0 subscriptionEnder = this.observe(value, key);
      this.subscriptionEnders.put(key, subscriptionEnder);
      Change _change = new Change(ChangeType.UPDATE, Collections.<String>unmodifiableList(Lists.<String>newArrayList(key)), value);
      this.publish(_change);
      _xblockexpression = previous;
    }
    return _xblockexpression;
  }
  
  public V remove(final Object key) {
    V _xblockexpression = null;
    {
      Procedure0 _get = this.subscriptionEnders.get(key);
      if (_get!=null) {
        _get.apply();
      }
      final V previous = super.remove(key);
      boolean _notEquals = (!Objects.equal(previous, null));
      if (_notEquals) {
        String _string = key.toString();
        Change _change = new Change(ChangeType.REMOVE, Collections.<String>unmodifiableList(Lists.<String>newArrayList(_string)), previous);
        this.publish(_change);
      }
      _xblockexpression = previous;
    }
    return _xblockexpression;
  }
  
  public void putAll(final Map<? extends String, ? extends V> m) {
    Set<? extends String> _keySet = m.keySet();
    for (final String key : _keySet) {
      V _get = m.get(key);
      this.put(key, _get);
    }
  }
  
  public void clear() {
    final Procedure2<String, Procedure0> _function = new Procedure2<String, Procedure0>() {
      public void apply(final String k, final Procedure0 v) {
        if (v!=null) {
          v.apply();
        }
      }
    };
    MapExtensions.<String, Procedure0>forEach(this.subscriptionEnders, _function);
    this.subscriptionEnders.clear();
    super.clear();
    Change _change = new Change(ChangeType.CLEAR, Collections.<String>unmodifiableList(Lists.<String>newArrayList()), null);
    this.publish(_change);
  }
  
  public void apply(final Change change) {
    try {
      boolean _and = false;
      Publisher<Change> _publisher = this.getPublisher();
      boolean _notEquals = (!Objects.equal(_publisher, null));
      if (!_notEquals) {
        _and = false;
      } else {
        Publisher<Change> _publisher_1 = this.getPublisher();
        boolean _isPublishing = _publisher_1.isPublishing();
        boolean _not = (!_isPublishing);
        _and = _not;
      }
      final boolean wasPublishing = _and;
      try {
        Publisher<Change> _publisher_2 = this.getPublisher();
        if (_publisher_2!=null) {
          _publisher_2.setPublishing(false);
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
          _publisher_3.setPublishing(wasPublishing);
        }
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public EntityMap<V> clone() {
    Object _clone = super.clone();
    return ((EntityMap<V>) _clone);
  }
}
