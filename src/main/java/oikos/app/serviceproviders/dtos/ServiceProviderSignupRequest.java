package oikos.app.serviceproviders.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import oikos.app.users.SignupRequest;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data @SuperBuilder @NoArgsConstructor @AllArgsConstructor public class ServiceProviderSignupRequest extends SignupRequest {

  @NotNull private String SIRET;
  @NotNull private String RIB;
  @NotNull private String name;
  @NotNull private String street;
  @NotNull private String zipCode;
  @Min(0) @Max(95) private int departmentIdentifier;
}
