package oikos.app.common.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
/**
 * Created by Mohamed Haamdi on 06/01/2021.
 */
@MappedSuperclass @Getter @Setter @NoArgsConstructor @AllArgsConstructor
@SuperBuilder public class File extends BaseEntityNoId {
  long size;
  @Column(name = "ID", nullable = false)
  @Id private String id;
  private String fileName;
  private String originalName;
  private String fileType;
}
