package oikos.app.notifications;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import oikos.app.common.models.BaseEntity;
import oikos.app.users.User;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import java.time.Instant;

/** Created by Mohamed Haamdi on 27/04/2021. */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ScheduledNotification extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY) private User utilisateur;
  private String content;
  private String link;
  @NotNull private Instant instant;
}
