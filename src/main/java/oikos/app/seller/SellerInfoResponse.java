package oikos.app.seller;

import lombok.Data;
import oikos.app.common.models.Address;
import oikos.app.users.Civility;
import oikos.app.users.MaritalStatus;

import java.time.LocalDate;

/** Created by Mohamed Haamdi on 17/04/2021. */
@Data
public class SellerInfoResponse {
  MaritalStatus maritalStatus;
  Civility civility;
  Address address;
  LocalDate birthDate;
}
