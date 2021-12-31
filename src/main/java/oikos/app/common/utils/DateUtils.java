package oikos.app.common.utils;

import oikos.app.security.VerificationTokenType;

import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;

/** Created by Mohamed Haamdi on 27/04/2021. */
public class DateUtils {
  private DateUtils() {}

  public static Date convertToDateViaInstant(Instant dateToConvert) {
    return java.util.Date.from(dateToConvert.atZone(ZoneId.systemDefault()).toInstant());
  }

  public static Instant calculateTokenExpiryDate(
    VerificationTokenType type) {
    return switch (type) {
      case SMS -> Instant.now().plus(Duration.ofMinutes(5));
      case EMAIL, PASSWORD_RESET -> Instant.now().plus(Period.ofDays(1));
    };
  }
}
