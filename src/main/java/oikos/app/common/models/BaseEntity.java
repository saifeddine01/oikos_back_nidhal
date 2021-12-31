package oikos.app.common.models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/** Created by Mohamed Haamdi on 28/03/2021. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@SuperBuilder
public class BaseEntity extends BaseEntityNoId {
  @Id
  @GeneratedValue(generator = "nano-generator")
  @GenericGenerator(name = "nano-generator", strategy = "oikos.app.common.utils.NanoIDGenerator")
  private String id;
}
