package oikos.app.common.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import oikos.app.common.utils.validators.NullOrBetween;
import oikos.app.common.utils.validators.NullOrNotBlank;

/** Created by Mohamed Haamdi on 26/06/2021 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {
  @NullOrNotBlank private String street;
  @NullOrNotBlank private String zipCode;

  @NullOrBetween(min = 0, max = 95, message = "Departement must be between 0 and 95")
  private Integer departmentIdentifier;
}
