package oikos.app.messaging;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import oikos.app.common.models.File;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

@SuperBuilder @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Entity
public class MessageAttachement extends File {
  @OneToOne(fetch = FetchType.LAZY, mappedBy = "messageAttachement", optional = false)
  private Message message;
}
