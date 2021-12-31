package oikos.app.common.utils.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
/** Created by Mohamed Haamdi on 16/04/2021. */
public class NullOrBetweenValidator implements ConstraintValidator<NullOrBetween, Integer> {

  private int min;
  private int max;

  @Override
  public void initialize(NullOrBetween parameters) {
    this.min = parameters.min();
    this.max = parameters.max();
  }

  @Override
  public boolean isValid(Integer value, ConstraintValidatorContext context) {
    return value == null || (value >= min && value <= max);
  }
}
