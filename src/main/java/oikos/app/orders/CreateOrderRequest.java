package oikos.app.orders;

import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static oikos.app.common.utils.NanoIDGenerator.NANOID_SIZE;

@Value
public class CreateOrderRequest {
  @NotBlank
  @Size(min = NANOID_SIZE, max = NANOID_SIZE)
  String serviceID;

  @NotNull PaymentMethod paymentMethod;
}
