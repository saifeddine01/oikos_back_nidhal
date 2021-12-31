package oikos.app.serviceproviders.exceptions;

import oikos.app.common.exceptions.BaseException;

public class CompanyNotValidatedException extends BaseException {
  public CompanyNotValidatedException(String companyID) {
    super("Company "+companyID+" has not been approved by the Admin yet!");
  }
}
