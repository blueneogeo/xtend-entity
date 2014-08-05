package nl.kii.entity;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import nl.kii.entity.ChangeType;
import nl.kii.entity.EntityException;
import nl.kii.entity.EntityObject;
import nl.kii.util.IterableExtensions;
import nl.kii.util.OptExtensions;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;

/**
 * Represents a change to a reactive entity
 * <p>
 * @Property action - the ChangeType what was made. it is stored as changeId so it can be sent as json.
 * @Property path - contains a / seperated path, for example '1/name' from #[ 23, #{ name: 'mary' }] refers to 'mary'
 * @Property value - the new value of the item referenced by the path, if any
 */
@SuppressWarnings("all")
public class Change implements EntityObject {
  public final static String PATH_SEPARATOR = ".";
  
  private final long id;
  
  private final ChangeType action;
  
  private final List<String> path;
  
  private final Object value;
  
  public Change(final ChangeType action) {
    this((-1), action, null, null);
  }
  
  public Change(final ChangeType action, final Object value) {
    this((-1), action, null, value);
  }
  
  public Change(final ChangeType action, final int index, final Object value) {
    this((-1), action, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(("" + Integer.valueOf(index)))), value);
  }
  
  public Change(final ChangeType action, final String key, final Object value) {
    this((-1), action, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(key)), value);
  }
  
  public Change(final ChangeType action, final List<String> path, final Object value) {
    this((-1), action, path, value);
  }
  
  public Change(final long id, final ChangeType action, final List<String> path, final Object value) {
    this.id = id;
    this.action = action;
    this.path = path;
    this.value = value;
  }
  
  public long getId() {
    return this.id;
  }
  
  public ChangeType getAction() {
    return this.action;
  }
  
  public Object getValue() {
    return this.value;
  }
  
  public List<String> getPath() {
    List<String> _elvis = null;
    if (this.path != null) {
      _elvis = this.path;
    } else {
      _elvis = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList());
    }
    return _elvis;
  }
  
  /**
   * add a part to the path of a change, and return a new change from that
   */
  public Change addPath(final String addedPath) {
    List<String> _path = this.getPath();
    ImmutableList<String> _concat = IterableExtensions.<String>concat(addedPath, _path);
    return new Change(this.id, this.action, _concat, this.value);
  }
  
  /**
   * remove the first part of the path of a change and create a new change from that
   */
  public Change forward() {
    try {
      Change _xblockexpression = null;
      {
        boolean _or = false;
        boolean _equals = Objects.equal(this.path, null);
        if (_equals) {
          _or = true;
        } else {
          int _length = ((Object[])Conversions.unwrapArray(this.path, Object.class)).length;
          boolean _equals_1 = (_length == 0);
          _or = _equals_1;
        }
        if (_or) {
          throw new EntityException(("cannot forward a change with an empty path. for change " + this));
        }
        Iterable<String> _tail = org.eclipse.xtext.xbase.lib.IterableExtensions.<String>tail(this.path);
        List<String> _list = IterableExtensions.<String>list(((String[])Conversions.unwrapArray(_tail, String.class)));
        _xblockexpression = new Change(this.id, this.action, _list, this.value);
      }
      return _xblockexpression;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public void validate() {
  }
  
  public String toString() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append(this.action, "");
    _builder.append(" ");
    String _join = null;
    if (this.path!=null) {
      _join=org.eclipse.xtext.xbase.lib.IterableExtensions.join(this.path, ".");
    }
    _builder.append(_join, "");
    {
      boolean _and = false;
      boolean _defined = OptExtensions.<Object>defined(this.value);
      if (!_defined) {
        _and = false;
      } else {
        int _length = 0;
        if (((Object[])Conversions.unwrapArray(this.path, Object.class))!=null) {
          _length=((Object[])Conversions.unwrapArray(this.path, Object.class)).length;
        }
        boolean _greaterThan = (_length > 0);
        _and = _greaterThan;
      }
      if (_and) {
        _builder.append(" = ");
      }
    }
    {
      boolean _defined_1 = OptExtensions.<Object>defined(this.value);
      if (_defined_1) {
        _builder.append("\"");
        _builder.append(this.value, "");
        _builder.append("\"");
      }
    }
    return _builder.toString();
  }
  
  public boolean equals(final Object o) {
    boolean _switchResult = false;
    boolean _matched = false;
    if (!_matched) {
      if (o instanceof Change) {
        _matched=true;
        boolean _and = false;
        boolean _and_1 = false;
        boolean _and_2 = false;
        if (!(((Change)o).id == this.id)) {
          _and_2 = false;
        } else {
          boolean _equals = Objects.equal(((Change)o).action, this.action);
          _and_2 = _equals;
        }
        if (!_and_2) {
          _and_1 = false;
        } else {
          boolean _equals_1 = Objects.equal(((Change)o).path, this.path);
          _and_1 = _equals_1;
        }
        if (!_and_1) {
          _and = false;
        } else {
          boolean _equals_2 = Objects.equal(((Change)o).value, this.value);
          _and = _equals_2;
        }
        _switchResult = _and;
      }
    }
    if (!_matched) {
      _switchResult = true;
    }
    return _switchResult;
  }
  
  public int hashCode() {
    int _hashCode = Long.valueOf(this.id).hashCode();
    int _hashCode_1 = this.action.hashCode();
    int _multiply = (_hashCode * _hashCode_1);
    int _hashCode_2 = 0;
    if (this.path!=null) {
      _hashCode_2=this.path.hashCode();
    }
    int _multiply_1 = (_multiply * _hashCode_2);
    int _hashCode_3 = 0;
    if (this.value!=null) {
      _hashCode_3=this.value.hashCode();
    }
    int _multiply_2 = (_multiply_1 * _hashCode_3);
    return (_multiply_2 * 37);
  }
  
  public Change clone() {
    String[] _clone = null;
    if (((String[])Conversions.unwrapArray(this.path, String.class))!=null) {
      _clone=((String[])Conversions.unwrapArray(this.path, String.class)).clone();
    }
    return new Change(this.id, this.action, (List<String>)Conversions.doWrapArray(_clone), this.value);
  }
}
