package oikos.app.messaging;

import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import static oikos.app.common.utils.NanoIDGenerator.NANOID_SIZE;

/** Created by Mohamed Haamdi on 12/02/2021 */
@Value
public class SendMessageRequest {
  @Size(min = NANOID_SIZE, max = NANOID_SIZE)
  @NotBlank
  String recipientId;

  @NotBlank String content;
}
