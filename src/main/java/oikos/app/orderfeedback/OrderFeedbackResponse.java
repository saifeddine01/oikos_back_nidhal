package oikos.app.orderfeedback;

import lombok.Data;

import java.time.Instant;

@Data
public class OrderFeedbackResponse {
  private String id;
  private String clientID;
  private String orderID;
  private String content;
  private int rating;
  private boolean isRefundRequest;
  private Instant createdAt;
}
