package oikos.app.common.utils.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/** Created by Mohamed Haamdi on 16/04/2021. */
public class NullOrNotBlankValidator implements ConstraintValidator<NullOrNotBlank, String> {

  @Override
  public void initialize(NullOrNotBlank parameters) {
    // Nothing to do here
  }

  public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
    return value == null || value.trim().length() > 0;
  }
}
