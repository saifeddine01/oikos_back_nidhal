package oikos.app.orderfeedback;

import lombok.Value;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static oikos.app.common.utils.NanoIDGenerator.NANOID_SIZE;

@Value
public class CreateOrderFeedbackRequest {
  @NotBlank
  @Size(min = NANOID_SIZE, max = NANOID_SIZE)
  String orderID;

  @NotBlank String content;

  @Min(0)
  @Max(5)
  int rating;

  @NotNull boolean refundRequest;
}
