package oikos.app.oikosservices;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

import static oikos.app.common.utils.NanoIDGenerator.NANOID_SIZE;

/** Created by Mohamed Haamdi on 26/06/2021 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateServiceRequest {
  @Size(min = NANOID_SIZE, max = NANOID_SIZE)
  private String companyID;

  @NotNull private ServiceType serviceType;
  @NotBlank private String description;

  @Min(0)
  private BigDecimal price;

  @NotNull private boolean needsAppointment;
}
