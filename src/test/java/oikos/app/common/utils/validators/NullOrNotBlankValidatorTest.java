package oikos.app.common.utils.validators;

import org.junit.jupiter.api.Test;

import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/** Created by Mohamed Haamdi on 26/04/2021. */
class NullOrNotBlankValidatorTest {
  @Test
  void testInit() {
    var validator = new NullOrNotBlankValidator();
    validator.initialize(null);
    assertThat(validator).isNotNull();
  }

  @Test
  void isValidWhenNull() {
    // given
    var validator = new NullOrNotBlankValidator();
    // when
    boolean valid = validator.isValid(null, mock(ConstraintValidatorContext.class));
    // then
    assertThat(valid).isTrue();
  }

  @Test
  void isValidWhenNotBlank() {
    // given
    var validator = new NullOrNotBlankValidator();
    // when
    boolean valid = validator.isValid("01234567", mock(ConstraintValidatorContext.class));
    // then
    assertThat(valid).isTrue();
  }

  @Test
  void isNotValidWhenNotNullNorBlank() {
    // given
    var validator = new NullOrNotBlankValidator();
    // when
    boolean valid = validator.isValid("", mock(ConstraintValidatorContext.class));
    // then
    assertThat(valid).isFalse();
  }
}
