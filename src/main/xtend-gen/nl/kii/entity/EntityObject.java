package nl.kii.entity;

import nl.kii.entity.EntityException;

/**
 * An EntityObject has getters and setters, default constructors, mandatory fields and a validation method for those fields.
 */
@SuppressWarnings("all")
public interface EntityObject extends Cloneable {
  /**
   * Throws an EntityException with a reason if the object is not valid
   */
  public abstract void validate() throws EntityException;
}
