package nl.kii.entity;

import com.google.common.base.Objects;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import nl.kii.async.annotation.Atomic;
import nl.kii.entity.Change;
import nl.kii.entity.ChangeType;
import nl.kii.entity.EntityException;
import nl.kii.entity.EntityList;
import nl.kii.entity.EntityObject;
import nl.kii.entity.Reactive;
import nl.kii.observe.Observable;
import nl.kii.observe.Publisher;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

@SuppressWarnings("all")
public class EntityMap<K extends Object, V extends Object> extends HashMap<K, V> implements Reactive, EntityObject {
  public enum SupportedTypes {
    String,
    
    Integer,
    
    Long,
    
    Float,
    
    Double,
    
    Boolean,
    
    Date;
  }
  
  /**
   * Using the standard Javascript date format
   */
  public final static DateFormat KEY_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z");
  
  private final Class<K> keyType;
  
  private final Class<V> type;
  
  private final boolean isReactive;
  
  @Atomic
  private final transient AtomicReference<Publisher<Change>> _publisher = new AtomicReference<Publisher<Change>>();
  
  @Atomic
  private final transient AtomicReference<Map<K, Procedure0>> _subscriptionEnders = new AtomicReference<Map<K, Procedure0>>();
  
  public EntityMap(final Class<K> keyType, final Class<V> type) {
    super();
    this.keyType = keyType;
    this.type = type;
    this.isReactive = true;
    HashMap<K, Procedure0> _newHashMap = CollectionLiterals.<K, Procedure0>newHashMap();
    this.setSubscriptionEnders(_newHashMap);
  }
  
  public EntityMap(final Class<K> keyType, final Class<V> type, final int size) {
    super(size);
    this.keyType = keyType;
    this.type = type;
    this.isReactive = true;
  }
  
  public EntityMap(final Class<K> keyType, final Class<V> type, final Map<? extends String, ? extends V> m) {
    this.type = type;
    this.keyType = keyType;
    this.isReactive = true;
  }
  
  private Object isSupportedType(final Class<K> type) {
    return null;
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
  
  private Procedure0 observe(final V element, final K key) {
    Procedure0 _switchResult = null;
    boolean _matched = false;
    if (!_matched) {
      if (element instanceof Observable) {
        _matched=true;
        final Procedure1<Change> _function = new Procedure1<Change>() {
          @Override
          public void apply(final Change change) {
            String _pathString = EntityMap.this.toPathString(key);
            Change _addPath = change.addPath(_pathString);
            EntityMap.this.publish(_addPath);
          }
        };
        _switchResult = ((Observable<Change>)element).onChange(_function);
      }
    }
    return _switchResult;
  }
  
  @Override
  public V put(final K key, final V value) {
    V _xblockexpression = null;
    {
      Map<K, Procedure0> _subscriptionEnders = this.getSubscriptionEnders();
      Procedure0 _get = _subscriptionEnders.get(key);
      if (_get!=null) {
        _get.apply();
      }
      final V previous = super.put(key, value);
      final Procedure0 subscriptionEnder = this.observe(value, key);
      Map<K, Procedure0> _subscriptionEnders_1 = this.getSubscriptionEnders();
      _subscriptionEnders_1.put(key, subscriptionEnder);
      String _pathString = this.toPathString(key);
      Change _change = new Change(ChangeType.UPDATE, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_pathString)), value);
      this.publish(_change);
      _xblockexpression = previous;
    }
    return _xblockexpression;
  }
  
  @Override
  public V remove(final Object key) {
    try {
      V _xblockexpression = null;
      {
        Class<?> _class = key.getClass();
        boolean _isAssignableFrom = this.keyType.isAssignableFrom(_class);
        boolean _not = (!_isAssignableFrom);
        if (_not) {
          String _string = this.toString();
          String _plus = (_string + " cannot remove key ");
          String _plus_1 = (_plus + key);
          String _plus_2 = (_plus_1 + " since it is not a ");
          Class<?> _class_1 = key.getClass();
          String _simpleName = _class_1.getSimpleName();
          String _plus_3 = (_plus_2 + _simpleName);
          throw new EntityException(_plus_3);
        }
        Map<K, Procedure0> _subscriptionEnders = this.getSubscriptionEnders();
        Procedure0 _get = _subscriptionEnders.get(key);
        if (_get!=null) {
          _get.apply();
        }
        final V previous = super.remove(key);
        boolean _notEquals = (!Objects.equal(previous, null));
        if (_notEquals) {
          String _pathString = this.toPathString(((K) key));
          Change _change = new Change(ChangeType.REMOVE, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_pathString)), previous);
          this.publish(_change);
        }
        _xblockexpression = previous;
      }
      return _xblockexpression;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Override
  public void putAll(final Map<? extends K, ? extends V> m) {
    Set<? extends K> _keySet = m.keySet();
    for (final K key : _keySet) {
      V _get = m.get(key);
      this.put(key, _get);
    }
  }
  
  @Override
  public void clear() {
    Map<K, Procedure0> _subscriptionEnders = this.getSubscriptionEnders();
    final BiConsumer<K, Procedure0> _function = new BiConsumer<K, Procedure0>() {
      @Override
      public void accept(final K k, final Procedure0 v) {
        if (v!=null) {
          v.apply();
        }
      }
    };
    _subscriptionEnders.forEach(_function);
    Map<K, Procedure0> _subscriptionEnders_1 = this.getSubscriptionEnders();
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
                  final Map<K, V> map = ((Map<K, V>) _value_1);
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
                  List<String> _path_1 = change.getPath();
                  final String key = IterableExtensions.<String>head(_path_1);
                  Class<? extends String> _class_2 = key.getClass();
                  boolean _isAssignableFrom_1 = _class_2.isAssignableFrom(this.keyType);
                  boolean _not_3 = (!_isAssignableFrom_1);
                  if (_not_3) {
                    String _simpleName = this.type.getSimpleName();
                    String _plus_5 = ("key is not of correct type " + _simpleName);
                    String _plus_6 = (_plus_5 + ", could not apply ");
                    String _plus_7 = (_plus_6 + change);
                    throw new EntityException(_plus_7);
                  }
                  Object _value_2 = change.getValue();
                  Class<?> _class_3 = _value_2.getClass();
                  boolean _isAssignableFrom_2 = _class_3.isAssignableFrom(this.type);
                  boolean _not_4 = (!_isAssignableFrom_2);
                  if (_not_4) {
                    String _simpleName_1 = this.type.getSimpleName();
                    String _plus_8 = ("value is not of correct type " + _simpleName_1);
                    String _plus_9 = (_plus_8 + ", could not apply ");
                    String _plus_10 = (_plus_9 + change);
                    throw new EntityException(_plus_10);
                  }
                  List<String> _path_2 = change.getPath();
                  String _head_2 = IterableExtensions.<String>head(_path_2);
                  Object _keyType = this.toKeyType(_head_2);
                  Object _value_3 = change.getValue();
                  this.put(((K) _keyType), ((V) _value_3));
                  break;
                case REMOVE:
                case CLEAR:
                  List<String> _path_3 = change.getPath();
                  final String key_1 = IterableExtensions.<String>head(_path_3);
                  Class<? extends String> _class_4 = key_1.getClass();
                  boolean _isAssignableFrom_3 = _class_4.isAssignableFrom(this.keyType);
                  boolean _not_5 = (!_isAssignableFrom_3);
                  if (_not_5) {
                    String _simpleName_2 = this.type.getSimpleName();
                    String _plus_11 = ("key is not of correct type " + _simpleName_2);
                    String _plus_12 = (_plus_11 + ", could not apply ");
                    String _plus_13 = (_plus_12 + change);
                    throw new EntityException(_plus_13);
                  }
                  final Object mapKey = this.toKeyType(key_1);
                  boolean _containsKey = this.containsKey(mapKey);
                  if (_containsKey) {
                    this.remove(mapKey);
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
            final String key_2 = IterableExtensions.<String>head(_path_4);
            Class<? extends String> _class_5 = key_2.getClass();
            boolean _isAssignableFrom_4 = _class_5.isAssignableFrom(this.keyType);
            boolean _not_6 = (!_isAssignableFrom_4);
            if (_not_6) {
              String _simpleName_3 = this.type.getSimpleName();
              String _plus_14 = ("key is not of correct type " + _simpleName_3);
              String _plus_15 = (_plus_14 + ", could not apply ");
              String _plus_16 = (_plus_15 + change);
              throw new EntityException(_plus_16);
            }
            Object _keyType_1 = this.toKeyType(key_2);
            final V value = this.get(_keyType_1);
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
  public EntityMap<K, V> clone() {
    Object _clone = super.clone();
    return ((EntityMap<K, V>) _clone);
  }
  
  @Override
  public void validate() {
  }
  
  @Override
  public boolean isValid() {
    return true;
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
  
  /**
   * Convert a type key to a String.
   * Uses normal type Type.toString methods, except for Date, which uses
   * the KEY_DATE_FORMAT as defined in this class.
   */
  public String toPathString(final K key) {
    String _switchResult = null;
    final Class<K> keyType = this.keyType;
    boolean _matched = false;
    if (!_matched) {
      if (Objects.equal(keyType, Date.class)) {
        _matched=true;
        _switchResult = EntityMap.KEY_DATE_FORMAT.format(((Date) key));
      }
    }
    if (!_matched) {
      _switchResult = key.toString();
    }
    return _switchResult;
  }
  
  /**
   * Try to convert a string to one of the supported key types.
   * Uses normal Type.parseType methods, except for Date, which uses
   * the KEY_DATE_FORMAT as defined in this class.
   */
  public Object toKeyType(final String s) {
    try {
      Object _xtrycatchfinallyexpression = null;
      try {
        Object _switchResult = null;
        final Class<K> keyType = this.keyType;
        boolean _matched = false;
        if (!_matched) {
          if (Objects.equal(keyType, String.class)) {
            _matched=true;
            _switchResult = s;
          }
        }
        if (!_matched) {
          if (Objects.equal(keyType, Integer.class)) {
            _matched=true;
            _switchResult = Integer.valueOf(Integer.parseInt(s));
          }
        }
        if (!_matched) {
          if (Objects.equal(keyType, Float.class)) {
            _matched=true;
            _switchResult = Float.valueOf(Float.parseFloat(s));
          }
        }
        if (!_matched) {
          if (Objects.equal(keyType, Double.class)) {
            _matched=true;
            _switchResult = Double.valueOf(Double.parseDouble(s));
          }
        }
        if (!_matched) {
          if (Objects.equal(keyType, Long.class)) {
            _matched=true;
            _switchResult = Long.valueOf(Long.parseLong(s));
          }
        }
        if (!_matched) {
          if (Objects.equal(keyType, Boolean.class)) {
            _matched=true;
            _switchResult = Boolean.valueOf(Boolean.parseBoolean(s));
          }
        }
        if (!_matched) {
          if (Objects.equal(keyType, Date.class)) {
            _matched=true;
            _switchResult = EntityMap.KEY_DATE_FORMAT.parse(s);
          }
        }
        if (!_matched) {
          String _string = this.toString();
          String _plus = (_string + " could not convert \"");
          String _plus_1 = (_plus + s);
          String _plus_2 = (_plus_1 + "\" to a valid key, since s is not a supported type. Supported types are: ");
          EntityMap.SupportedTypes[] _values = EntityMap.SupportedTypes.values();
          String _join = IterableExtensions.join(((Iterable<?>)Conversions.doWrapArray(_values)), ", ");
          String _plus_3 = (_plus_2 + _join);
          throw new EntityException(_plus_3);
        }
        _xtrycatchfinallyexpression = ((Object)_switchResult);
      } catch (final Throwable _t) {
        if (_t instanceof Exception) {
          final Exception e = (Exception)_t;
          String _string_1 = this.toString();
          String _plus_4 = (_string_1 + " could not convert \"");
          String _plus_5 = (_plus_4 + s);
          String _plus_6 = (_plus_5 + "\" to a valid key.");
          throw new EntityException(_plus_6, e);
        } else {
          throw Exceptions.sneakyThrow(_t);
        }
      }
      return ((Comparable<?>)_xtrycatchfinallyexpression);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Override
  public String toString() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("EntityMap<");
    String _simpleName = this.keyType.getSimpleName();
    _builder.append(_simpleName, "");
    _builder.append(",");
    String _simpleName_1 = this.type.getSimpleName();
    _builder.append(_simpleName_1, "");
    _builder.append(">(size: ");
    int _size = this.size();
    _builder.append(_size, "");
    _builder.append(")");
    return _builder.toString();
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
  
  private void setSubscriptionEnders(final Map<K, Procedure0> value) {
    this._subscriptionEnders.set(value);
  }
  
  private Map<K, Procedure0> getSubscriptionEnders() {
    return this._subscriptionEnders.get();
  }
  
  private Map<K, Procedure0> getAndSetSubscriptionEnders(final Map<K, Procedure0> value) {
    return this._subscriptionEnders.getAndSet(value);
  }
}
