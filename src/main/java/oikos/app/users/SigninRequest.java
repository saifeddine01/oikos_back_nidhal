package oikos.app.users;

import lombok.Value;

import javax.validation.constraints.NotBlank;

/** Created by Mohamed Haamdi on 23/03/2021. */
@Value
public class SigninRequest {
  @NotBlank String emailOrPhone;
  @NotBlank String password;
}
