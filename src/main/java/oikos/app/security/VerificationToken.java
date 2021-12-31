package oikos.app.security;

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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.Instant;

/** Created by Mohamed Haamdi on 26/03/2021. */
@Table(name = "VerificationToken", indexes = {
  @Index(name = "IDX_VERIFICATIONTOKEN_user_id", columnList = "user_id")}) @Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class VerificationToken extends BaseEntity {

  private String token;
  private Instant expiresAt;
  @OneToOne(fetch = FetchType.LAZY) private User user;

  @Enumerated(EnumType.STRING)
  private VerificationTokenType type;
}
