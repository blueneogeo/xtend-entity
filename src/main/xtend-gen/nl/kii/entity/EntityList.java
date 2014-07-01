package nl.kii.entity;

import com.google.common.base.Objects;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.entity.Change;
import nl.kii.entity.ChangeType;
import nl.kii.entity.EntityException;
import nl.kii.entity.Reactive;
import nl.kii.observe.Observable;
import nl.kii.observe.Publisher;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.MapExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;

@SuppressWarnings("all")
public class EntityList<E extends Object> extends ArrayList<E> implements Reactive {
  private final Class<E> type;
  
  private final boolean isReactive;
  
  private final transient AtomicReference<Publisher<Change>> _publisher = new AtomicReference<Publisher<Change>>();
  
  private transient Map<Integer, Procedure0> subscriptionEnders = CollectionLiterals.<Integer, Procedure0>newHashMap();
  
  public EntityList(final Class<E> type) {
    super();
    this.type = type;
    this.isReactive = true;
  }
  
  public EntityList(final Class<E> type, final int size) {
    super(size);
    this.type = type;
    this.isReactive = true;
  }
  
  public EntityList(final Class<E> type, final Collection<? extends E> coll) {
    super(coll);
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
  
  private Procedure0 observe(final E element) {
    Procedure0 _switchResult = null;
    boolean _matched = false;
    if (!_matched) {
      if (element instanceof Observable) {
        _matched=true;
        final Procedure1<Change> _function = new Procedure1<Change>() {
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
  
  public E set(final int index, final E value) {
    E _xblockexpression = null;
    {
      Procedure0 _get = this.subscriptionEnders.get(Integer.valueOf(index));
      if (_get!=null) {
        _get.apply();
      }
      final E previous = super.set(index, value);
      final Procedure0 subscriptionEnder = this.observe(value);
      this.subscriptionEnders.put(Integer.valueOf(index), subscriptionEnder);
      Change _change = new Change(ChangeType.UPDATE, index, value);
      this.publish(_change);
      _xblockexpression = previous;
    }
    return _xblockexpression;
  }
  
  public boolean add(final E element) {
    boolean _xblockexpression = false;
    {
      final boolean success = super.add(element);
      if ((!success)) {
        return false;
      }
      final Procedure0 subscriptionEnder = this.observe(element);
      final int index = this.indexOf(element);
      this.subscriptionEnders.put(Integer.valueOf(index), subscriptionEnder);
      Change _change = new Change(ChangeType.ADD, element);
      this.publish(_change);
      _xblockexpression = true;
    }
    return _xblockexpression;
  }
  
  public void add(final int index, final E value) {
    Procedure0 _get = this.subscriptionEnders.get(Integer.valueOf(index));
    if (_get!=null) {
      _get.apply();
    }
    super.add(index, value);
    final Procedure0 subscriptionEnder = this.observe(value);
    this.subscriptionEnders.put(Integer.valueOf(index), subscriptionEnder);
    Change _change = new Change(ChangeType.UPDATE, index, value);
    this.publish(_change);
  }
  
  public E remove(final int index) {
    E _xblockexpression = null;
    {
      Procedure0 _get = this.subscriptionEnders.get(Integer.valueOf(index));
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
  
  public boolean remove(final Object o) {
    boolean _xblockexpression = false;
    {
      final int index = this.indexOf(o);
      if ((index < 0)) {
        return false;
      }
      Procedure0 _get = this.subscriptionEnders.get(Integer.valueOf(index));
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
  
  public void clear() {
    final Procedure2<Integer, Procedure0> _function = new Procedure2<Integer, Procedure0>() {
      public void apply(final Integer k, final Procedure0 v) {
        if (v!=null) {
          v.apply();
        }
      }
    };
    MapExtensions.<Integer, Procedure0>forEach(this.subscriptionEnders, _function);
    this.subscriptionEnders.clear();
    super.clear();
    Change _change = new Change(ChangeType.CLEAR);
    this.publish(_change);
  }
  
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
  public boolean addAll(final int index, final Collection<? extends E> c) {
    throw new UnsupportedOperationException();
  }
  
  @Deprecated
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
          _publisher_3.setPublishing(wasPublishing);
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
  
  public EntityList<E> clone() {
    Object _clone = super.clone();
    return ((EntityList<E>) _clone);
  }
}
