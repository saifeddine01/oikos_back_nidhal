package oikos.app.common.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import oikos.app.users.User;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Proxy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Setter(value = AccessLevel.PUBLIC)
@Proxy(lazy = false)
@ToString
@EntityListeners(AuditingEntityListener.class)
// @JsonIgnoreProperties(ignoreUnknown = true)
// @JsonIdentityInfo(generator=ObjectIdGenerators.StringIdGenerator.class, property="bien_id")
@Entity(name = "bienavendre")
@SuperBuilder
public class BienVendre implements Serializable {

  @CreatedDate Instant createdAt;
  @LastModifiedDate Instant updatedAt;
  @Id
  @GeneratedValue(generator = "nano-generator")
  @GenericGenerator(name = "nano-generator", strategy = "oikos.app.common.utils.NanoIDGenerator")
  private String id;
  @Enumerated(EnumType.STRING)
  private Status status = Status.Pending;

  //	@Enumerated(EnumType.STRING)
  //	private TypeImmobilier typeofprop;

  private String typeofprop;

  // private int typepropInt;

  @Enumerated(EnumType.STRING)
  private PropertyVue vueProp;

  @Enumerated(EnumType.STRING)
  private PropertyLocation propLocation;

  @Enumerated(EnumType.STRING)
  private PropertyStanding propStanding;

  private boolean isOwner = true;
  private double allArea;
  private double homeArea;
  private double livingArea;
  @Embedded private Location location;
  @Embedded private PropertyAddress address;

  @Embedded private PiecesOfProperty piecesOfProperty;
  @Embedded private PropExport propExport;

  private boolean hasDependancy;
  // mitoyen
  private boolean isAdjoining;

  private String description;
  private String keyPoints;
  private int nbBedrooms;
  private int nbRooms;
  // private int nbWC;
  private int nbParkingPlaces;
  private boolean hasPlannedWork;
  private int nbFloors;
  private String yearConstruction;
  private String mainPic;
  private int price;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  private User userId;

  @JsonManagedReference
  @OneToMany(mappedBy = "bien", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Set<PropertyFile> fileBien = new HashSet<>();

  @JsonManagedReference
  @OneToMany(mappedBy = "prop", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Set<LikedProperty> proplikes = new HashSet<>();

  public String getMainPic() {
    return mainPic;
  }

  public int getPrice() {
    return price;
  }

  @JsonManagedReference
  public Set<PropertyFile> getFileBien() {
    return fileBien;
  }

  public String getId() {
    return id;
  }

  public Status getStatus() {
    return status;
  }

  public boolean isOwner() {
    return isOwner;
  }

  public double getHomeArea() {
    return homeArea;
  }

  public double getLivingArea() {
    return livingArea;
  }

  public Location getLocation() {
    return location;
  }

  public PiecesOfProperty getPiecesOfProperty() {
    return piecesOfProperty;
  }

  public PropExport getPropExport() {
    return propExport;
  }

  public boolean isHasDependancy() {
    return hasDependancy;
  }

  public boolean isAdjoining() {
    return isAdjoining;
  }

  public String getDescription() {
    return description;
  }

  public String getKeyPoints() {
    return keyPoints;
  }

  public int getNbRooms() {
    return nbRooms;
  }

  public int getNbParkingPlaces() {
    return nbParkingPlaces;
  }

  public boolean isHasPlannedWork() {
    return hasPlannedWork;
  }

  public int getNbFloors() {
    return nbFloors;
  }

  public String getYearConstruction() {
    return yearConstruction;
  }

  public int getNbBedrooms() {
    return nbBedrooms;
  }

  public PropertyVue getVueProp() {
    return vueProp;
  }

  public PropertyLocation getPropLocation() {
    return propLocation;
  }

  public PropertyStanding getPropStanding() {
    return propStanding;
  }

  public PropertyAddress getAddress() {
    return address;
  }

  public double getAllArea() {
    return allArea;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  @JsonManagedReference
  public Set<LikedProperty> getProplikes() {
    return proplikes;
  }

  public User getUserId() {
    return userId;
  }

  public String getTypeofprop() {
    return typeofprop;
  }
}
