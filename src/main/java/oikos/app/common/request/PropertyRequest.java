package oikos.app.common.request;

import javax.persistence.Embedded;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import lombok.Builder;
import lombok.Value;
import oikos.app.common.models.Location;
import oikos.app.common.models.PiecesOfProperty;
import oikos.app.common.models.PropExport;
import oikos.app.common.models.PropertyAddress;

@Builder
@Value
public class PropertyRequest {

  private int typepropInt;
  @NotBlank private String vueProp;
  @NotBlank private String propLocation;
  @NotBlank private String propStanding;
  private boolean isOwner = true;
  private double allArea;
  private double homeArea;
  @Min(value = 0, message = "livingArea should not be less than 0")
  @Max(value = 100000, message = "livingArea should not be greater than 100 000")
  private double livingArea;
  @Embedded private Location location;
  @Embedded @Valid private PropertyAddress address;
  private boolean hasDependancy;
  // mitoyen
  private boolean isAdjoining;
  @NotBlank private String description;
  //	@NotBlank
  //	private String keyPoints;
  @Min(value = 0, message = "nbBedrooms should not be less than 0")
  @Max(value = 150, message = "nbBedrooms should not be greater than 150")
  private int nbBedrooms;

  @Min(value = 0, message = "nbRooms should not be less than 0")
  @Max(value = 150, message = "nbRooms should not be greater than 150")
  private int nbRooms;
  // private int nbWC;
  @Min(value = 0, message = "nbStationnements should not be less than 0")
  @Max(value = 150, message = "nbStationnements should not be greater than 150")
  private int nbParkingPlaces;

  private boolean hasPlannedWork;

  @Min(value = 0, message = "nbFloors should not be less than 0")
  @Max(value = 150, message = "nbFloors should not be greater than 150")
  private int nbFloors;

  @NotBlank private String yearConstruction;

  @Min(value = 0, message = "price should not be less than 0")
  private int price;
  private int propInt;

  @Embedded @Valid private PiecesOfProperty piecesOfProperty;
  @Embedded @Valid private PropExport propExport;
}
