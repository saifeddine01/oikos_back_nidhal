package oikos.app.orders;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class OrderResponse {
  private String id;
  private String clientID;
  private String companyID;
  private String serviceID;
  private String collaboratorID;
  private BigDecimal price;
  private PaymentMethod paymentMethod;
  private OrderStatus orderStatus;
  private Instant createdAt;
}
