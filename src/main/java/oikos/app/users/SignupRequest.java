package oikos.app.users;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;

/**
 * Created by Mohamed Haamdi on 02/06/2021.
 */
@Data @SuperBuilder  @NoArgsConstructor @AllArgsConstructor public class SignupRequest extends PasswordlessSignupRequest {
  @NotBlank private String password;
}
