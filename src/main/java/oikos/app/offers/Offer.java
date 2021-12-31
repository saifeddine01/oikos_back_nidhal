package oikos.app.offers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import oikos.app.common.models.BaseEntity;
import oikos.app.common.models.BienVendre;
import oikos.app.users.User;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Created by Mohamed Haamdi on 11/05/2021.
 */
@Cacheable @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Table(name = "Offer", indexes = {
  @Index(name = "IDX_OFFER_sender_id", columnList = "sender_id"),
  @Index(name = "IDX_OFFER_recipient_id", columnList = "recipient_id"),
  @Index(name = "IDX_OFFER_property_id", columnList = "property_id")}) @Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class Offer extends BaseEntity {
  @OneToOne(fetch = FetchType.LAZY) private User sender;
  @OneToOne(fetch = FetchType.LAZY) private User recipient;
  @OneToOne(fetch = FetchType.LAZY) private BienVendre property;
  private LocalDate endsAt;

  @Enumerated(EnumType.STRING) private OfferStatus status;

  private BigDecimal amount;

  @OneToOne @JoinColumn private Offer previousOffer;
}
