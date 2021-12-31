package oikos.app.seller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import oikos.app.users.PasswordlessSignupRequest;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Created by Mohamed Haamdi on 02/06/2021.
 */
@Data @SuperBuilder @NoArgsConstructor @AllArgsConstructor public class SellerBySecretarySignupRequest
  extends PasswordlessSignupRequest {
  @NotNull private LocalDate birthDate;
  @NotNull private String street;
  @NotNull private String zipCode;
  @Min(0) @Max(95) private int departmentIdentifier;
}
