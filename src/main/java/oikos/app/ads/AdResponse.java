package oikos.app.ads;

import lombok.Data;

@Data
public class AdResponse {
  private String id;
  private String propertyID;
  private AdPlatform platform;
  private int views;
  private String url;
}
