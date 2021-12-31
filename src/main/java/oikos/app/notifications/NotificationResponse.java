package oikos.app.notifications;

import lombok.Data;

import java.time.Instant;

/** Created by Mohamed Haamdi on 15/02/2021. */
@Data
public class NotificationResponse {
  String id;
  String content;
  Instant dateCreation;
  EtatNotification etat;
  String lien;
}
