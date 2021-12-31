package oikos.app.security.exceptions;

import oikos.app.common.exceptions.BaseException;

public class TokenRevokedException extends BaseException {

  public TokenRevokedException(String msg) {
    super(msg);
  }
}
