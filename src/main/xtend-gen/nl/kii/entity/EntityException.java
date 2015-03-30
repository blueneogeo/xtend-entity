package nl.kii.entity;

@SuppressWarnings("all")
public class EntityException extends Exception {
  public EntityException(final String s) {
    super(s);
  }
  
  public EntityException(final String s, final Throwable t) {
    super(s, t);
  }
}
