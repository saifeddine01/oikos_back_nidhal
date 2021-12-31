package oikos.app.messaging;

import oikos.app.common.exceptions.BaseException;

/**
 * Created by Mohamed Haamdi on 04/06/2021.
 */
public class AlreadyHasAttachementException extends BaseException {
  public AlreadyHasAttachementException(String message) {
    super(message);
  }
}
