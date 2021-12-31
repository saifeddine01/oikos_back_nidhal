package oikos.app.messaging;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import oikos.app.common.models.BaseEntity;
import oikos.app.users.User;

import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.Table;

/** Created by Mohamed Haamdi on 12/02/2021 */
@Table(name = "Message", indexes = {
  @Index(name = "IDX_MESSAGE_sender_id", columnList = "sender_id"),
  @Index(name = "IDX_MESSAGE_recipient_id", columnList = "recipient_id"),
  @Index(name = "IDX_MESSAGE_thread_id", columnList = "thread_id"),
  @Index(name = "IDX_MESSAGE_status", columnList = "status")}) @Entity
@SuperBuilder @Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Message extends BaseEntity {

  private String content;

  @OneToOne(fetch = FetchType.LAZY) private User sender;

  @OneToOne(fetch = FetchType.LAZY) private User recipient;

  @Enumerated(EnumType.STRING)
  private EtatMessage status;

  @ManyToOne(fetch = FetchType.LAZY) private MessageThread thread;

  private boolean isSenderDeleted = false;

  private boolean isRecipientDeleted = false;

  @JoinColumn(name = "MESSAGE_ATTACHEMENT_ID")
  @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true)
  private MessageAttachement messageAttachement;
}
