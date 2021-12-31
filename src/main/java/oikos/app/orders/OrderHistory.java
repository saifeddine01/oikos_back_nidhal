package oikos.app.orders;

import lombok.Data;

import java.time.Instant;

@Data
public class OrderHistory {
  private String ID;
  private Instant updatedAt;
  private String updatedBy;
  private OrderStatus orderStatus;
}
