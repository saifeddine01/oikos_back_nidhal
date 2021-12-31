package oikos.app.messaging;

import lombok.Data;

import java.time.Instant;

/**
 * Created by Mohamed Haamdi on 13/02/2021
 */
@Data public class MessageResponse {
  String id;
  String senderId;
  String recipientId;
  String threadId;
  EtatMessage status;
  Instant updatedAt;
  Instant createdAt;
  String content;
  MessageAttachementResponse messageAttachement;
}
