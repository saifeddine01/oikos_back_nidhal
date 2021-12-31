package oikos.app.orders;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.javers.core.metamodel.annotation.DiffIgnore;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.math.BigDecimal;

import oikos.app.common.models.BaseEntity;
import oikos.app.oikosservices.OikosService;
import oikos.app.serviceproviders.models.ServiceCompany;
import oikos.app.users.User;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Getter
@Setter
@Table(
    name = "oikosorder",
    indexes = {
      @Index(name = "idx_order_client_id", columnList = "client_id"),
      @Index(name = "idx_order_service_id", columnList = "service_id"),
      @Index(name = "idx_order_servicecompany_id", columnList = "servicecompany_id")
    })
public class Order extends BaseEntity {
  @DiffIgnore
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(nullable = false)
  private User client;

  @DiffIgnore
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn
  private User collaborator;

  @DiffIgnore
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(nullable = true)
  private OikosService service;

  @DiffIgnore
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(nullable = false, name = "servicecompany_id")
  private ServiceCompany serviceCompany;

  private BigDecimal price;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentMethod paymentMethod;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OrderStatus orderStatus;
}
