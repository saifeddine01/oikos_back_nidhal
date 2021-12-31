package oikos.app.appointementfeedback;

import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/** Created by Mohamed Haamdi on 07/05/2021. */
@Value
public class AddAppointementFeedbackRequest {
  @NotBlank String appointmentID;
  @NotBlank String propertyID;
  @NotNull Intrest intrest;
  @NotBlank String opinion;
  @NotBlank String promisePoints;
  @NotBlank String priceOpinion;
}
