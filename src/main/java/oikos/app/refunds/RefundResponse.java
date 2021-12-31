package oikos.app.refunds;

import lombok.Data;

@Data
public class RefundResponse {
  private String id;
  private String orderID;
  private String serviceCompanyID;
  private RefundStatus refundStatus;
}
