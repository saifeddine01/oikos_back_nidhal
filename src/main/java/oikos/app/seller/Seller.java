package oikos.app.seller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import oikos.app.common.models.Address;
import oikos.app.common.models.BaseEntity;
import oikos.app.common.models.BaseEntityNoId;
import oikos.app.users.Civility;
import oikos.app.users.MaritalStatus;
import oikos.app.users.User;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.LocalDate;

/** Created by Mohamed Haamdi on 23/03/2021. */
@Entity @Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Seller extends BaseEntityNoId {
  @Id
  @Column(name = "user_id")
  private String id;

  private LocalDate birthDate;



  @OneToOne
  @MapsId
  @JoinColumn(name = "user_id")
  private User user;

  @Enumerated(EnumType.STRING)
  private MaritalStatus maritalStatus;

  @Enumerated(EnumType.STRING)
  private Civility civility;

  @Embedded private Address address;
}
