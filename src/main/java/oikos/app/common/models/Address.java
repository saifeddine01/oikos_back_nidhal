package oikos.app.common.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import oikos.app.common.utils.DepartmentUtils;

import javax.persistence.Embeddable;
import javax.persistence.Transient;
import java.io.Serializable;

/** Created by Mohamed Haamdi on 24/03/2021. */
@Data
@Embeddable
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address implements Serializable {
  private String street;
  private String zipCode;
  @JsonIgnore private int departmentIdentifier;

  @Transient
  public String getDepartmentName() {
    final var departmentUtils = DepartmentUtils.getInstance();
    return departmentUtils.get(departmentIdentifier).getName();
  }
}
