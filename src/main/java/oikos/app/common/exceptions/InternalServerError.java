package oikos.app.common.exceptions;

/**
 * Created by Mohamed Haamdi on 20/03/2021.
 */
public class InternalServerError extends RuntimeException {
  public InternalServerError(String message) {
    this(message, null);
  }

  public InternalServerError(String message, Throwable cause) {
    super(String.format(
      "Something very wrong happened at %s. Please contact the dev team.",
      message), cause);
  }
}
