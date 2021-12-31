package oikos.app.security.exceptions;

import oikos.app.common.exceptions.BaseException;

/** Created by Mohamed Haamdi on 27/03/2021. */
public class OldPasswordReuseException extends BaseException {
  public OldPasswordReuseException() {
    super("This password is already in use.");
  }
}
