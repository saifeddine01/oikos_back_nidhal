package oikos.app.common.utils.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

/** Created by Mohamed Haamdi on 26/06/2021 */
public class NullOrMinValidator implements ConstraintValidator<NullOrMin, BigDecimal> {
  private BigDecimal min;

  @Override
  public void initialize(NullOrMin parameters) {
    this.min = BigDecimal.valueOf(parameters.value());
  }

  @Override
  public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
    return value == null || value.compareTo(min) >= 0;
  }
}
