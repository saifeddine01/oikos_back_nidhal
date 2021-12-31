package oikos.app.messaging;

import oikos.app.users.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/** Created by Mohamed Haamdi on 05/05/2021. */
@DataJpaTest
class MessageThreadRepoTest {

  private final Pageable paging = PageRequest.of(0, 10);
  @Autowired private MessageThreadRepo underTest;

  @AfterEach
  void tearDown() {
    underTest.deleteAll();
  }

  @Test
  void findMessageThreadByUserID() {
    // Given
    User sender = new User("userId");
    underTest.save(
        MessageThread.builder().messages(Set.of()).user1(sender).isUser1Deleted(false).build());

    // When
    Page<MessageThread> page = underTest.findMessageThreadByUserID(sender.getId(), paging);
    // Then
    assertThat(page).hasSize(1);
  }

  @Test
  void findMessageThreadByUserIDNoThreads() {
    // Given
    User sender = new User("userId");
    // When
    Page<MessageThread> page = underTest.findMessageThreadByUserID(sender.getId(), paging);
    // Then
    assertThat(page).isEmpty();
  }

  @Test
  void findMessageThreadByUserIDThreadDeleted() {
    // Given
    User sender = new User("userId");
    underTest.save(
        MessageThread.builder().messages(Set.of()).user1(sender).isUser1Deleted(true).build());

    // When
    Page<MessageThread> page = underTest.findMessageThreadByUserID(sender.getId(), paging);
    // Then
    assertThat(page).isEmpty();
  }

  @Test
  void findMessageThreadBetweenUsersByID() {
    // Given
    User sender = new User("userId");
    User receiver = new User("receiverID");
    underTest.save(
        MessageThread.builder().messages(Set.of()).user1(sender).user2(receiver).build());

    // When
    var res = underTest.findMessageThreadBetweenUsersByID(sender.getId(), receiver.getId());
    // Then
    assertThat(res).isPresent();
    // When
    // Then
  }
}
