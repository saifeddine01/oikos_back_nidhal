package oikos.app.common.request;

import java.time.LocalDateTime;

import lombok.Value;
import oikos.app.common.models.DisponibilityType;

@Value
public class DispoRequest {

  LocalDateTime dateStart;

  LocalDateTime dateEnd;

  Boolean isAllDay;

  String title;
  String description;
  DisponibilityType dispotype;

}
