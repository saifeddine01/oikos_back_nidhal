package oikos.app.oikosservices;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

import oikos.app.common.utils.validators.NullOrMin;
import oikos.app.common.utils.validators.NullOrNotBlank;

/** Created by Mohamed Haamdi on 26/06/2021 */
@Value
@Builder
public class EditServiceRequest {
  @NullOrNotBlank String description;

  @NullOrMin(message = "Price must be positive", value = 0)
  BigDecimal price;

  boolean needsAppointment;
}
