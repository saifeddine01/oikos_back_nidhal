package oikos.app.ads;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

import oikos.app.common.models.PropertyAddress;

@EqualsAndHashCode(callSuper = true)
@Data
public class AdFacebookMarketplace extends AdDTO {
  private String propertyType;
  private Integer nbBedrooms;
  private Integer nbWC;
  private PropertyAddress address;
  private String description;
  private Integer price;
  private Set<String> photos;
}
