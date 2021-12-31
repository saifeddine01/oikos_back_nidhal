package oikos.app.users;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import oikos.app.buyer.Buyer;
import oikos.app.common.models.BaseEntity;
import oikos.app.security.AuthProvider;
import oikos.app.seller.Seller;
import oikos.app.serviceproviders.models.Collaborator;
import oikos.app.serviceproviders.models.ServiceOwner;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import java.util.Set;

/** Created by Mohamed Haamdi on 08/02/2021 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "oikosuser")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Table(indexes = {@Index(columnList = "email"), @Index(columnList = "phoneNumber")})
@SuperBuilder
public class User extends BaseEntity {
  private String email;
  private String phoneNumber;

  @Enumerated(EnumType.STRING)
  private AuthProvider provider;

  private String providerId;
  private String firstName;
  private String lastName;
  private String password;
  private boolean isAccountExpired = false;
  private boolean isAccountLocked = false;
  private boolean isCredentialsExpired = false;
  private boolean isEnabled = true;
  /** Role information */
  @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
  @ElementCollection(fetch = FetchType.EAGER)
  @Enumerated(EnumType.STRING)
  private Set<Role> roles;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
  @PrimaryKeyJoinColumn
  private Seller sellerProfile;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
  @PrimaryKeyJoinColumn
  private Buyer buyerProfile;

  @JoinColumn(name = "USER_PROFILE_FILE_ID")
  @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true)
  private UserProfileFile userProfileFile;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
  @PrimaryKeyJoinColumn
  private Collaborator collaboratorProfile;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
  @PrimaryKeyJoinColumn
  private ServiceOwner serviceOwnerProfile;

  public User(String id) {
    super(id);
  }
}
