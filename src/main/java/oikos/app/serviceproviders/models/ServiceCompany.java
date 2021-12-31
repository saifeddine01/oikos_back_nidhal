package oikos.app.serviceproviders.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import oikos.app.common.models.Address;
import oikos.app.common.models.BaseEntity;
import oikos.app.oikosservices.OikosService;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Cacheable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Getter
@Setter
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
public class ServiceCompany extends BaseEntity {

  private String SIRET;

  private String RIB;

  private String name;

  private boolean isValidated;

  @Embedded private Address address;

  @OneToMany(fetch = FetchType.LAZY)
  private List<Collaborator> collaborators;

  @OneToMany(fetch = FetchType.LAZY)
  private List<OikosService> services;

  @OneToOne(mappedBy = "serviceCompany")
  private ServiceOwner serviceOwner;
}
