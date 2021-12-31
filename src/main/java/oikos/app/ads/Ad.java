package oikos.app.ads;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

import oikos.app.common.models.BaseEntity;
import oikos.app.common.models.BienVendre;
import oikos.app.users.User;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@Entity
public class Ad extends BaseEntity {
  @OneToOne(fetch = FetchType.LAZY)
  private BienVendre prop;

  @OneToOne(fetch = FetchType.LAZY)
  private User propOwner;

  private int views;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AdPlatform adPlatform;

  private String url;
}
