package oikos.app.security.exceptions;

import oikos.app.common.exceptions.BaseException;

/** Created by Mohamed Haamdi on 27/03/2021. */
public class NoUserAssociatedException extends BaseException {
  public NoUserAssociatedException(String email) {
    super(String.format("No user account is associated with this email %s.", email));
  }
}
