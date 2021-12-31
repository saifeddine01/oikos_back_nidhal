package oikos.app.departements;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import java.io.Serializable;

/** Created by Mohamed Haamdi on 21/04/2021. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class Department implements Serializable {
  private int id;
  private String code;
  private String name;
}
