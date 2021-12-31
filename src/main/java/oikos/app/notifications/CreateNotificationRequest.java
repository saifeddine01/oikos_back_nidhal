package oikos.app.notifications;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import static oikos.app.common.utils.NanoIDGenerator.NANOID_SIZE;

/** Created by Mohamed Haamdi on 15/02/2021. */
@Value
@Builder
@AllArgsConstructor
public class CreateNotificationRequest {
  @Size(min = NANOID_SIZE, max = NANOID_SIZE)
  @NotBlank
  String userId;

  @NotBlank String content;
  String lien;
}
