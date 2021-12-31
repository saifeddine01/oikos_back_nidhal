package oikos.app.users;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

/** Created by Mohamed Haamdi on 08/02/2021. */
@Data @SuperBuilder @NoArgsConstructor @AllArgsConstructor
public class PasswordlessSignupRequest {
  @NotBlank private String firstName;
  @NotBlank private String lastName;
  @Email private String email;
  // TODO: Should use a real phone number validation here
  @NotBlank private String phoneNumber;
}
