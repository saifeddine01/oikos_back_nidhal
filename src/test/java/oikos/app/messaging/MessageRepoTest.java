package oikos.app.messaging;

import oikos.app.users.User;
import oikos.app.users.UserRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/** Created by Mohamed Haamdi on 05/05/2021. */
@DataJpaTest
class MessageRepoTest {
  private final Pageable paging = PageRequest.of(0, 10);
  @Autowired private MessageRepo underTest;
  @Autowired private MessageThreadRepo threadRepo;
  @Autowired private UserRepo userRepo;

  @AfterEach
  void tearDown() {
    underTest.deleteAll();
    threadRepo.deleteAll();
    userRepo.deleteAll();
  }

  @Test
  void getMessagesInThreadNoThread() {
    Page<Message> messagesInThread =
        underTest.getMessagesInThread("DoesntExist", "DoesntExist", paging);
    assertThat(messagesInThread).isEmpty();
  }

  @Test
  void getMessagesInThread() {
    // given
    String userId = "senderID";
    User sender = new User(userId);
    var receiver = new User("receiver");
    var m1 =
        Message.builder()
            .sender(sender)
            .recipient(receiver)
            .isSenderDeleted(false)
            .isRecipientDeleted(false)
            .build();
    var m2 =
        Message.builder()
            .sender(sender)
            .recipient(receiver)
            .isSenderDeleted(false)
            .isRecipientDeleted(false)
            .build();

    MessageThread o =
        threadRepo.save(
            MessageThread.builder().messages(Set.of(m1, m2)).user1(sender).user2(receiver).build());
    m1.setThread(o);
    m2.setThread(o);

    m1 = underTest.save(m1);
    m2 = underTest.save(m2);
    // when
    Page<Message> messagesInThread = underTest.getMessagesInThread(o.getId(), userId, paging);

    // then
    assertThat(m1.getThread().getId()).isEqualTo(o.getId());
    assertThat(m2.getThread().getId()).isEqualTo(o.getId());
    assertThat(messagesInThread).hasSize(2);
  }

  @Test
  void getMessagesInThreadOneIsDeleted() {
    // given
    String userId = "senderID";
    User sender = new User(userId);
    var receiver = new User("receiver");
    var m1 =
        Message.builder()
            .sender(sender)
            .recipient(receiver)
            .isSenderDeleted(true)
            .isRecipientDeleted(false)
            .build();
    var m2 =
        Message.builder()
            .sender(sender)
            .recipient(receiver)
            .isSenderDeleted(false)
            .isRecipientDeleted(false)
            .build();

    MessageThread o =
        threadRepo.save(
            MessageThread.builder().messages(Set.of(m1, m2)).user1(sender).user2(receiver).build());
    m1.setThread(o);
    m2.setThread(o);

    m1 = underTest.save(m1);
    m2 = underTest.save(m2);
    // when
    Page<Message> messagesInThread = underTest.getMessagesInThread(o.getId(), userId, paging);

    // then
    assertThat(m1.getThread().getId()).isEqualTo(o.getId());
    assertThat(m2.getThread().getId()).isEqualTo(o.getId());
    assertThat(messagesInThread).hasSize(1);
  }

  @Test
  void getMessageForUser() {
    // given
    final var u1 = new User("u1");
    final var u2 = new User("u2");
    userRepo.saveAll(List.of(u1, u2));
    var message1 =
        underTest.save(
            Message.builder()
                .sender(u1)
                .isSenderDeleted(false)
                .recipient(u2)
                .isRecipientDeleted(false)
                .build());
    // when
    var res = underTest.getMessageForUser(message1.getId(), "u1");
    // then
    assertThat(res).isPresent();
  }

  @Test
  void getMessageForUserIsDeleted() {
    // given
    final var u1 = new User("u1");
    final var u2 = new User("u2");
    userRepo.saveAll(List.of(u1, u2));
    var message1 =
        underTest.save(
            Message.builder()
                .sender(u1)
                .isSenderDeleted(true)
                .recipient(u2)
                .isRecipientDeleted(false)
                .build());
    // when
    var res = underTest.getMessageForUser(message1.getId(), "u1");
    // then
    assertThat(res).isEmpty();
  }

  @Test
  void getMessageForUserIsNotIncluded() {
    // given
    final var u1 = new User("anotherUser");
    final var u2 = new User("u2");
    userRepo.saveAll(List.of(u1, u2));
    var message1 =
        underTest.save(
            Message.builder()
                .sender(u1)
                .isSenderDeleted(false)
                .recipient(u2)
                .isRecipientDeleted(false)
                .build());
    // when
    var res = underTest.getMessageForUser(message1.getId(), "u1");
    // then
    assertThat(res).isEmpty();
  }
}
