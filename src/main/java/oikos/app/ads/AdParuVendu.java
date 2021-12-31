package oikos.app.ads;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

import oikos.app.common.models.PropertyAddress;

@EqualsAndHashCode(callSuper = true)
@Data
public class AdParuVendu extends AdDTO {
  private String propertyType;
  private String description;
  private String title;
  private Double allArea;
  private String yearConstruction;
  private Integer nbRooms;
  private Integer price;
  private PropertyAddress address;
  private Set<String> photos = new HashSet<>();
}
