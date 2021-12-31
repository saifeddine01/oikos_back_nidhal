package oikos.app.security.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/** Created by Mohamed Haamdi on 27/03/2021. */
@Data
public class ResetPasswordRequest {
  @NotBlank String password;
}
