package oikos.app.messaging;

import lombok.Data;

import java.time.LocalDateTime;

/** Created by Mohamed Haamdi on 12/02/2021 */
@Data
public class MessageThreadListResponse {
  String id;
  String recipientID;
  LocalDateTime dateLastMessage;
}
