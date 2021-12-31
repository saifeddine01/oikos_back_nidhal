package oikos.app.search;

import lombok.Data;

@Data
public class BienVendreResponse {
  private String id;
  private int price;
  private String yearConstruction;
  private String keyPoints;
  private String description;
  private double allArea;
  private PropertyStanding propStanding;
  private String typeofprop;
  private PropertyAddress address;
}
