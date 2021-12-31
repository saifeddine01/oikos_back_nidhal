package oikos.app.appointementfeedback;

import lombok.Data;

import java.time.Instant;

/** Created by Mohamed Haamdi on 07/05/2021. */
@Data
public class AppointementFeedbackResponse {
  private String id;
  private String reviewerID;
  private String appointmentID;
  private String propertyID;
  private Intrest intrest;
  private String opinion;
  private String promisePoints;
  private String priceOpinion;
  private Instant createdAt;
}
