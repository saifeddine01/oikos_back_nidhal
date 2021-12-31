package oikos.app.ads;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

import oikos.app.common.models.Location;

@EqualsAndHashCode(callSuper = true)
@Data
public class AdLeBonCoin extends AdDTO {
  private String propertyType;
  private Double livingArea;
  private Double allArea;
  private Integer nbRooms;
  private String title;
  private String description;
  private Integer price;
  private Set<String> photos;
  private Location propertyCoordinates;
}
