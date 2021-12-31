package oikos.app.serviceproviders.exceptions;

import oikos.app.common.exceptions.BaseException;

public class SIRETAlreadyUsedException extends BaseException {
  public SIRETAlreadyUsedException(String message) {
    super(message);
  }
}
