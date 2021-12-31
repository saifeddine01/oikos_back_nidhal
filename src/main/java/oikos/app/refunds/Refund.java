package oikos.app.refunds;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import oikos.app.common.models.BaseEntity;
import oikos.app.orders.Order;
import oikos.app.serviceproviders.models.ServiceCompany;
import oikos.app.users.User;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@Entity
public class Refund extends BaseEntity {
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false, unique = true)
  private Order order;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "service_company_id", nullable = false)
  private ServiceCompany serviceCompany;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "client_id", nullable = false)
  private User client;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RefundStatus refundStatus;
}
