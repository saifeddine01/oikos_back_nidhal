package oikos.app.ads;

import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Value
public class CreateAdRequest {
  @Size(min = 1)
  List<AdPlatform> platforms;
  @NotBlank String propID;
  @NotBlank String description;
  @NotBlank String title;
}
