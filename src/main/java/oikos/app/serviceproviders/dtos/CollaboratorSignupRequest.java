package oikos.app.serviceproviders.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import oikos.app.users.PasswordlessSignupRequest;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CollaboratorSignupRequest extends PasswordlessSignupRequest {
  @NotBlank
  @Size(min = 8, max = 8)
  private String companyID;
}
