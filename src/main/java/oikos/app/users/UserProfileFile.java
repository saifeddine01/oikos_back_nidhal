package oikos.app.users;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import oikos.app.common.models.File;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

/**
 * Created by Mohamed Haamdi on 06/01/2021.
 */
@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class UserProfileFile extends File {
  @OneToOne(mappedBy = "userProfileFile", fetch = FetchType.LAZY) private User
    user;
}
