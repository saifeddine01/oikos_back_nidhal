package oikos.app.notifications;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import oikos.app.common.models.BaseEntity;
import oikos.app.users.User;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/** Created by Mohamed Haamdi on 15/02/2021 */
@Table(name = "Notification", indexes = {
  @Index(name = "IDX_NOTIFICATION", columnList = "utilisateur_id")}) @Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Notification extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY) private User utilisateur;
  private String content;

  @Enumerated(EnumType.STRING)
  private EtatNotification etat = EtatNotification.NON_VU;

  // Not all notifications have an internal link.
  private String lien;
}
