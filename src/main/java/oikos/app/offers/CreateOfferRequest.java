package oikos.app.offers;

import lombok.Value;

import javax.validation.constraints.Future;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Created by Mohamed Haamdi on 11/05/2021.
 */
@Value public class CreateOfferRequest {
  @NotBlank String propertyID;
  @NotNull @Future LocalDate endsAt;
  @NotNull @Min(0) BigDecimal amount;
}
