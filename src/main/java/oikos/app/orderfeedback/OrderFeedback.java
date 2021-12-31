package oikos.app.orderfeedback;

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
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Table(
    indexes = {
      @Index(name = "idx_orderfeedback", columnList = "servicecompany_id"),
      @Index(name = "idx_orderfeedback_order_id", columnList = "order_id")
    })
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Getter
@Setter
public class OrderFeedback extends BaseEntity {
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(nullable = false, unique = true)
  private Order order;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(nullable = false)
  private User customer;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(nullable = false, name = "servicecompany_id")
  private ServiceCompany serviceCompany;

  @Column(nullable = false)
  private String content;

  @Min(0)
  @Max(5)
  @Column(nullable = false)
  private int rating;

  private boolean isRefundRequest;
}
