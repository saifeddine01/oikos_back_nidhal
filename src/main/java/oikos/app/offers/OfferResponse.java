package oikos.app.offers;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data public class OfferResponse {
  private String id;
  private String previousOfferID;
  private Instant createdAt;
  private String senderID;
  private String recipientID;
  private String propertyID;
  private LocalDate endsAt;
  private OfferStatus status;
  private BigDecimal amount;
}
