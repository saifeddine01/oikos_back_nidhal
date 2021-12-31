package oikos.app.offers;

import lombok.Value;

import javax.validation.constraints.Future;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Value public class AddCounterOfferRequest {
  @NotNull @Future LocalDate endsAt;
  @NotNull @Min(0) BigDecimal amount;
}
