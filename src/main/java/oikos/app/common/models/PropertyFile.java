package oikos.app.common.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class PropertyFile extends File {
  @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST,
    CascadeType.MERGE, CascadeType.REFRESH})
  @JoinColumn(name = "biens_id", referencedColumnName = "id") private BienVendre
    bien;
  @JsonBackReference
  public BienVendre getBien() {
    return bien;
  }
}
