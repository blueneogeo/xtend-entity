package nl.kii.reactive;

/**
 * An EntityObject has getters and setters, default constructors, mandatory fields and a validation method for those fields.
 */
@SuppressWarnings("all")
public interface EntityObject extends Cloneable {
  /**
   * @return true if all fields annotated with @Require have a value
   */
  public abstract boolean isValid();
}
