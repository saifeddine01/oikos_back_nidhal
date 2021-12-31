package oikos.app.oikosservices;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Created by Mohamed Haamdi on 26/06/2021 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OikosServiceResponse {
  private String id;
  private ServiceType serviceType;
  private String description;
  private BigDecimal price;
  private boolean needsAppointment;
}
