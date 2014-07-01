package nl.kii.entity.annotations;

import nl.kii.entity.annotations.EntityProcessor;
import org.eclipse.xtend.lib.macro.Active;

/**
 * Reactive Objects can be observed for changes and can have change objects applied to them to change them.
 * Annotating a class with the @Reactive active annotation transforms it into a ReactiveObject.
 * 
 * <h3>Classes annotated with @Reactive attain the following features:</h3>
 * <p>
 * <li>extends ReactiveObjectBase
 * <li>an empty constructor
 * <li>a constructor of only the @Require fields
 * <li>a full constructor of all members in order of appearance
 * <li>getters and setters for all package fields
 * <li>an apply(Change change) method to change the contents of the fields.
 * Changes applied to this object also propagate into the members if the path points deeper into the object tree
 * <li>an onChange[handler] method to observe changes in the fields.
 * Changes inside fields of type Map, List and ReactiveObject are also reported as changes.
 * <li>an isValid() method that returns if the @Require fields are set
 * <p>
 * <h3>The must obey the following rules:</h3>
 * <p>
 * <li>Supported member types are Boolean, Integer, Long, Float, Double, Date, String and other ReactiveObjects,
 * as well as List<T> and Map<String, T>, where T is one of the above types
 * <li>Fields that start with _ in the name or that are protected/package/friendly level will not be converted into getters and setters
 * <p>
 * <h3>Internals</h3>
 * <p>
 * Classes annotated with @Reactive use the setters for the fields to observe changes. If the fields are set with ReactiveObjects,
 * they are observed for internal changes as well. In order to observe containers like Map and List, these types when set are
 * automatically converted to ReactiveList and ReactiveMap.
 */
@Active(EntityProcessor.class)
public @interface Entity {
  public boolean reactive() default true;
}
