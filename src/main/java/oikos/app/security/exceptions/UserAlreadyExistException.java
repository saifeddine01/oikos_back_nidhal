package oikos.app.security.exceptions;

import oikos.app.common.exceptions.BaseException;

/** Created by Mohamed Haamdi on 16/03/2021. */
public class UserAlreadyExistException extends BaseException {
  public UserAlreadyExistException(String msg) {
    super(msg);
  }
}
