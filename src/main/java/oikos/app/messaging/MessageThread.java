package oikos.app.messaging;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import oikos.app.common.models.BaseEntity;
import oikos.app.users.User;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.Set;

/** Created by Mohamed Haamdi on 12/02/2021 */
@Table(name = "MessageThread", indexes = {
  @Index(name = "IDX_MESSAGETHREAD_user_1", columnList = "user_1"),
  @Index(name = "IDX_MESSAGETHREAD_user_2", columnList = "user_2")}) @Entity
@SuperBuilder @Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageThread extends BaseEntity {

  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "user_1", referencedColumnName = "id")
  private User user1;

  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "user_2", referencedColumnName = "id")
  private  User user2;

  @OneToMany(fetch = FetchType.LAZY) private Set<Message> messages;

  @LastModifiedDate private LocalDateTime dateLastMessage;

  @CreatedDate private LocalDateTime dateCreation;

  private boolean isUser1Deleted = false;

  private boolean isUser2Deleted = false;
}
