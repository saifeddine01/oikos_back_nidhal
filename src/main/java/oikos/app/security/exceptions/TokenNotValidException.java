package oikos.app.security.exceptions;

import oikos.app.common.exceptions.BaseException;

/** Created by Mohamed Haamdi on 26/03/2021. */
public class TokenNotValidException extends BaseException {
  public TokenNotValidException(String token) {
    super(String.format("Token %s is not valid.", token));
  }
}
