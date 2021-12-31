package oikos.app.orderfeedback;

import lombok.Value;
import oikos.app.common.utils.validators.NullOrBetween;
import oikos.app.common.utils.validators.NullOrNotBlank;

@Value
public class EditOrderFeedbackRequest {
  @NullOrNotBlank String content;

  @NullOrBetween(min = 0, max = 5)
  Integer rating;

  boolean refundRequest;
}
