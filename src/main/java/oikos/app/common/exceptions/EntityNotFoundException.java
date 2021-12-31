package oikos.app.common.exceptions;

public class EntityNotFoundException extends RuntimeException {

  public EntityNotFoundException(Class<?> c, String id) {
    super(String.format("Could not find %s with id %s", c.getSimpleName(), id));
  }
}
