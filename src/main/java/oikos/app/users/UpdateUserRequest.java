package oikos.app.users;

import lombok.Value;
import oikos.app.common.utils.validators.NullOrNotBlank;

import javax.validation.constraints.Email;

/** Created by Mohamed Haamdi on 15/04/2021. */
@Value
public class UpdateUserRequest {
  @NullOrNotBlank String firstName;
  @NullOrNotBlank String lastName;
  @NullOrNotBlank String password;
  @Email String email;
  // TODO: Should use a real phone number validation here
  @NullOrNotBlank String phoneNumber;
}
