package oikos.app.serviceproviders.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import oikos.app.common.models.Address;

/** Created by Mohamed Haamdi on 25/06/2021 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyResponse {
  private String id;

  private String SIRET;

  private String RIB;

  private String name;

  private boolean isValidated;

  private Address address;
}
