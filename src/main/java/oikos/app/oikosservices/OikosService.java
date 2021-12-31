package oikos.app.oikosservices;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import oikos.app.common.models.BaseEntity;
import oikos.app.serviceproviders.models.ServiceCompany;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;

@Table(indexes = {@Index(name = "idx_oikosservice", columnList = "servicecompany_id")})
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Getter
@Setter
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
public class OikosService extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "servicecompany_id")
  private ServiceCompany serviceCompany;

  @Enumerated(EnumType.STRING)
  @Column(name = "service_type", nullable = false)
  private ServiceType serviceType;

  private String description;

  private BigDecimal price;

  private boolean needsAppointment;

  private boolean isActive = true;
}
