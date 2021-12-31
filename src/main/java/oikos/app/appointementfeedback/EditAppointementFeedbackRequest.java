package oikos.app.appointementfeedback;

import lombok.Value;
import oikos.app.common.utils.validators.NullOrNotBlank;

/** Created by Mohamed Haamdi on 09/05/2021. */
@Value
public class EditAppointementFeedbackRequest {
  Intrest intrest;
  @NullOrNotBlank String opinion;
  @NullOrNotBlank String promisePoints;
  @NullOrNotBlank String priceOpinion;
}
