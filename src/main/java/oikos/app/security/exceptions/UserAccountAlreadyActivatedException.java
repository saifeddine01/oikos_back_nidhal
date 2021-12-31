package oikos.app.security.exceptions;

import oikos.app.common.exceptions.BaseException;

/** Created by Mohamed Haamdi on 26/03/2021. */
public class UserAccountAlreadyActivatedException extends BaseException {
  public UserAccountAlreadyActivatedException(String id) {
    super(String.format("User %s is already activated.", id));
  }
}
