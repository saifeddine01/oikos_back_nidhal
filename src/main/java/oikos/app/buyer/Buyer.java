package oikos.app.buyer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import oikos.app.common.models.BaseEntity;
import oikos.app.common.models.BaseEntityNoId;
import oikos.app.users.User;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/** Created by Mohamed Haamdi on 29/03/2021. */
 @Entity @Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Buyer extends BaseEntityNoId {
  @Id
  @Column(name = "user_id")
  private String id;

  @OneToOne
  @MapsId
  @JoinColumn(name = "user_id")
  private User user;

  private boolean isValidated;
}
