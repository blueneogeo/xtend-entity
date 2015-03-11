package nl.kii.entity;

import java.util.List;
import nl.kii.entity.EntityException;

/**
 * An EntityObject has getters and setters, default constructors, mandatory fields and a validation method for those fields.
 */
@SuppressWarnings("all")
public interface EntityObject extends Cloneable {
  /**
   * Get the type of the property given the passed path, allowing you to bypass Java erasure.
   * Throws an EntityException with a reason if the path is not valid.
   */
  public abstract Class<?> getInstanceType(final List<String> path) throws EntityException;
  
  /**
   * Throws an EntityException with a reason if the object data is not valid
   */
  public abstract void validate() throws EntityException;
  
  /**
   * only returns true if the object data is valid
   */
  public abstract boolean isValid();
}
