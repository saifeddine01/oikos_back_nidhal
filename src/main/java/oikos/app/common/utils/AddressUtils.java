package oikos.app.common.utils;

import oikos.app.common.models.Address;
import oikos.app.common.request.AddressDTO;

/** Created by Mohamed Haamdi on 26/06/2021 */
public class AddressUtils {
  private AddressUtils() {}

  public static Address editAddressFromDTO(Address dest, AddressDTO source) {
    if (source.getDepartmentIdentifier() != null) {
      dest.setDepartmentIdentifier(source.getDepartmentIdentifier());
    }
    if (source.getStreet() != null) {
      dest.setStreet(source.getStreet());
    }
    if (source.getZipCode() != null) {
      dest.setZipCode(source.getZipCode());
    }
    return dest;
  }
}
