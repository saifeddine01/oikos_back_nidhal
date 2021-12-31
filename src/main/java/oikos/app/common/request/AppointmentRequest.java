package oikos.app.common.request;

import lombok.Value;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Value
public class AppointmentRequest {
  @NotBlank private String title;
  @NotBlank private String description;
  LocalDateTime dateStartApp;

  LocalDateTime dateEndApp;
  String idDispo;
}
