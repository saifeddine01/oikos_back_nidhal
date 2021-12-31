package oikos.app.common.models;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;

/** Created by Mohamed Haamdi on 31/01/2021 */
@Entity
@Data
public class RendezVous implements Serializable {
  @Id
  @GeneratedValue(generator = "nano-generator")
  @GenericGenerator(name = "nano-generator", strategy = "oikos.app.common.utils.NanoIDGenerator")
  private String id;
}
