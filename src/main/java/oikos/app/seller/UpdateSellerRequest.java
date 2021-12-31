package oikos.app.seller;

import lombok.Data;
import oikos.app.common.request.AddressDTO;
import oikos.app.users.Civility;
import oikos.app.users.MaritalStatus;

import javax.validation.Valid;
import java.time.LocalDate;

/** Created by Mohamed Haamdi on 17/04/2021. */
@Data
public class UpdateSellerRequest {
  private LocalDate birthDate;
  private MaritalStatus maritalStatus;
  private Civility civility;
  @Valid private AddressDTO address;
}
