package oikos.app.ads;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class AdStats {
  private int totalViews;
  private Map<AdPlatform, Integer> viewsByPlatform;
}
