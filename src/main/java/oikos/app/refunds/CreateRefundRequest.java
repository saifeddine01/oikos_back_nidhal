package oikos.app.refunds;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import static oikos.app.common.utils.NanoIDGenerator.NANOID_SIZE;

@Data
public class CreateRefundRequest {
  @NotBlank
  @Size(min = NANOID_SIZE, max = NANOID_SIZE)
  String orderFeedbackID;
}
