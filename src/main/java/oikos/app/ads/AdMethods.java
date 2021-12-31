package oikos.app.ads;

import lombok.ToString;

@ToString
public enum AdMethods {
  DELETE_AD(Names.DELETE_AD),
  GET_ADS_FOR_USER(Names.GET_ADS_FOR_USER),
  GET_AD_STATS(Names.GET_AD_STATS),
  CREATE_ADS(Names.CREATE_ADS);
  private final String label;

  AdMethods(String label) {
    this.label = label;
  }

  private static class Names {
    public static final String DELETE_AD = "DELETE_AD";
    public static final String GET_ADS_FOR_USER = "GET_ADS_FOR_USER";
    public static final String CREATE_ADS = "CREATE_ADS";
    public static final String GET_AD_STATS = "GET_AD_STATS";
  }
}
