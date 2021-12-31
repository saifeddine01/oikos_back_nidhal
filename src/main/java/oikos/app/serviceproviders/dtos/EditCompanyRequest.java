package oikos.app.serviceproviders.dtos;

import lombok.Data;
import oikos.app.common.request.AddressDTO;
import oikos.app.common.utils.validators.NullOrNotBlank;

import javax.validation.Valid;

/** Created by Mohamed Haamdi on 25/06/2021 */
@Data
public class EditCompanyRequest {
  @NullOrNotBlank String RIB;
  @NullOrNotBlank String name;
  @Valid private AddressDTO address;
}
