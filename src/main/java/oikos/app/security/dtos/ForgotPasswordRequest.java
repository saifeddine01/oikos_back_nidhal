package oikos.app.security.dtos;

import lombok.Data;

import javax.validation.constraints.Email;

/** Created by Mohamed Haamdi on 27/03/2021. */
@Data
public class ForgotPasswordRequest {
  @Email String email;
}
