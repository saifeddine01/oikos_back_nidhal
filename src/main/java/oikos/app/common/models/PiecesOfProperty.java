package oikos.app.common.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@Embeddable
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PiecesOfProperty implements Serializable {
  private Boolean hasSallon;
  private Boolean hasCuisine;
  private Boolean hasTerrasse;
  private Boolean hasGarage;
  private Boolean hasBalcon;
  private Boolean hasJardin;
  private Boolean hasCave;
  private Boolean hasPiscine;
}
