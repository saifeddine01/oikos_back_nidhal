package oikos.app.messaging;

import oikos.app.buyer.Buyer;
import oikos.app.buyer.BuyerRepo;
import oikos.app.common.configurations.AppProperties;
import oikos.app.common.exceptions.BaseException;
import oikos.app.common.exceptions.EntityNotFoundException;
import oikos.app.messaging.MessageService.MessageMethods;
import oikos.app.common.models.FileInfo;
import oikos.app.common.pubsub.ChannelType;
import oikos.app.common.pubsub.PubSubService;
import oikos.app.common.services.FileService;
import oikos.app.users.Role;
import oikos.app.users.User;
import oikos.app.users.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created by Mohamed Haamdi on 26/04/2021.
 */
@ExtendWith(MockitoExtension.class) class MessageServiceTest {
  Pageable paging = PageRequest.of(0, 10);
  @Mock private MessageThreadRepo threadRepo;
  @Mock private MessageRepo messageRepo;
  @Mock private MessageAttachementRepo messageAttachementRepo;
  @Mock private UserRepo userRepo;
  @Mock private PubSubService pubSubService;
  @Mock private BuyerRepo buyerRepo;
  @Mock private FileService fileService;
  @Mock private AppProperties appProperties;
  private MessageService service;
  @InjectMocks private MessageService underTest;

  @BeforeEach void setUp() {
    service =
      new MessageService(threadRepo, messageRepo, messageAttachementRepo,
        userRepo, pubSubService, buyerRepo, fileService, appProperties);
  }

  @Test void getThreadList() {
    // Given
    var user = new User("userID");
    var notUser = new User("other");
    var thread1 = MessageThread.builder().id("0").user1(user).build();
    var thread2 = MessageThread.builder().id("1").user1(user).build();
    var thread3 = MessageThread.builder().id("2").user1(notUser).build();
    when(threadRepo.findMessageThreadByUserID(user.getId(), paging))
      .thenReturn(new PageImpl<>(List.of(thread1, thread2)));
    // When
    var res = service.getThreadList(user.getId(), paging);
    // Then
    assertThat(res).contains(thread1, thread2).doesNotContain(thread3);
  }

  @Test void sendMessageUserDoesNotExist() {
    // Given
    var senderID = "VALID";
    var recipientID = "INVALID";
    var dummyMessage = new SendMessageRequest(recipientID, "dummy content");
    when(userRepo.existsById(recipientID)).thenReturn(false);
    // Then
    assertThatThrownBy(() -> {
      service.sendMessage(dummyMessage, senderID);
    }).isInstanceOf(EntityNotFoundException.class);
  }

  @Test void sendMessageSellerNotVerified() {
    // Given
    var senderID = "SENDERNotVerified";
    var recipientID = "RECIPIENT";
    var user = new User(senderID);
    user.setRoles(Set.of(Role.BUYER));
    var buyer = new Buyer();
    buyer.setValidated(false);
    user.setBuyerProfile(buyer);
    var dummyMessage = new SendMessageRequest(recipientID, "dummy content");
    when(userRepo.existsById(any())).thenReturn(true);
    when(userRepo.getOne(senderID)).thenReturn(user);
    when(buyerRepo.getByUserID(senderID)).thenReturn(buyer);
    // Then
    assertThatThrownBy(() -> service.sendMessage(dummyMessage, senderID))
      .isInstanceOf(BaseException.class);
  }

  @Test void sendMessageThreadDoesntExist() {
    // Given
    var senderID = "SENDER";
    var recipientID = "RECIPIENT";
    var dummyMessage = new SendMessageRequest(recipientID, "dummy content");
    var sender = new User(senderID);
    sender.setRoles(Set.of(Role.ADMIN));
    when(userRepo.existsById(any())).thenReturn(true);
    when(threadRepo.findMessageThreadBetweenUsersByID(any(), any()))
      .thenReturn(Optional.empty());
    when(threadRepo.save(any())).thenAnswer(returnsFirstArg());
    when(messageRepo.save(any())).thenAnswer(returnsFirstArg());
    when(userRepo.getOne(recipientID)).thenReturn(new User(recipientID));
    when(userRepo.getOne(senderID)).thenReturn(sender);
    // When
    var res = service.sendMessage(dummyMessage, senderID);
    // Then
    assertThat(res).isNotNull();
    assertThat(res.getContent()).isEqualTo(dummyMessage.getContent());
    assertThat(res.getThread().getUser1().getId()).isIn(senderID, recipientID);
    assertThat(res.getThread().getUser2().getId()).isIn(senderID, recipientID);
    verify(pubSubService, atLeastOnce())
      .publish(eq(ChannelType.MESSAGES), any(), any());
  }

  @Test void sendMessageThreadExists() {
    // Given
    var senderID = "SENDER";
    var recipientID = "RECIPIENT";
    var dummyMessage = new SendMessageRequest(recipientID, "dummy content");
    var dummyMessage2 = new SendMessageRequest(recipientID, "dummy content");
    var thread = MessageThread.builder().user1(new User(senderID))
      .user2(new User(recipientID)).messages(new HashSet<>()).id("dummy")
      .build();
    var sender = new User(senderID);
    sender.setRoles(Set.of(Role.ADMIN));
    when(userRepo.existsById(recipientID)).thenReturn(true);
    when(threadRepo.findMessageThreadBetweenUsersByID(any(), any()))
      .thenReturn(Optional.ofNullable(thread));
    when(threadRepo.save(any())).thenAnswer(returnsFirstArg());
    when(messageRepo.save(any())).thenAnswer(returnsFirstArg());
    when(userRepo.getOne(recipientID)).thenReturn(new User(recipientID));
    when(userRepo.getOne(senderID)).thenReturn(sender);
    // When
    var res = service.sendMessage(dummyMessage, senderID);
    var res2 = service.sendMessage(dummyMessage, senderID);
    // Then
    assertThat(res).isNotNull();
    assertThat(res.getThread()).isEqualTo(thread);
    assertThat(res.getThread().getMessages()).contains(res, res2);
    verify(pubSubService, atLeastOnce())
      .publish(eq(ChannelType.MESSAGES), any(), any());
  }

  @Test void getThreadInvalidID() {
    // Given
    var invalidID = "INVALID";
    var userID = "userID";
    when(threadRepo.existsById(any())).thenReturn(false);
    // Then
    assertThatThrownBy(() -> {
      service.getThread(invalidID, userID, paging);
    }).isInstanceOf(EntityNotFoundException.class);
  }

  @Test void getThreadValidID() {
    // Given
    var threadID = "thread";
    var thread = MessageThread.builder().id(threadID).build();
    var messages = List.of(Message.builder().thread(thread).build());
    var userID = "userID";
    when(threadRepo.existsById(threadID)).thenReturn(true);
    when(messageRepo.getMessagesInThread(threadID, userID, paging))
      .thenReturn((Page<Message>) new PageImpl<>(messages));
    // When
    var res = service.getThread(threadID, userID, paging);
    // Then
    assertThat(res).hasSize(1);
  }

  @Test void getMessage() {
    // given
    var msg = Message.builder().id("id").sender(new User("senderID")).build();
    when(messageRepo.getMessageForUser("id", "senderID"))
      .thenReturn(Optional.of(msg));
    // when
    var res = service.getMessage("id", "senderID");
    // then
    assertThat(res).isNotNull();
  }

  @Test void getMessageNotFound() {
    // given
    when(messageRepo.getMessageForUser("id", "senderID"))
      .thenReturn(Optional.empty());
    // when
    assertThatThrownBy(() -> service.getMessage("id", "senderID"))
      .isInstanceOf(EntityNotFoundException.class);
  }

  @Test void deleteAttachment() {
    final String MESSAGE_I_D = "msgid";
    final String USER_ID = "usrid";
    User user = User.builder().id(USER_ID).build();
    var message = Message.builder().id(MESSAGE_I_D).sender(new User(USER_ID))
      .messageAttachement(MessageAttachement.builder().build()).build();
    when(messageAttachementRepo.getAttachementForMessage(MESSAGE_I_D))
      .thenReturn(Optional.empty());
    when(messageRepo.getMessageForUser(MESSAGE_I_D, USER_ID))
      .thenReturn(Optional.of(message));
    when(messageRepo.save(any())).thenAnswer(returnsFirstArg());
    //when
    underTest.deleteAttachmentForMessage(MESSAGE_I_D,USER_ID);
    //then
    ArgumentCaptor<Message> argument = ArgumentCaptor.forClass(Message.class);
    verify(messageRepo).save(argument.capture());
    assertThat(argument.getValue().getMessageAttachement()).isNull();
  }


  @Nested class WhenDeletingThread {
    private final String THREAD_I_D = "THREAD_I_D";
    private final String USER_I_D = "USER_I_D";

    @BeforeEach void setup() {
    }

    @Test void deleteInvalidThread() {
      when(threadRepo.existsById(any())).thenReturn(false);
      // Then
      assertThatThrownBy(() -> {
        underTest.getThread(THREAD_I_D, USER_I_D, paging);
      }).isInstanceOf(EntityNotFoundException.class);
    }

    @Test void deleteThreadExists() {
      // Given
      var user = new User(USER_I_D);
      var user2 = new User(USER_I_D + "1");
      var message1 = Message.builder().id("msg1").content("dummy").sender(user)
        .recipient(user2).build();
      var message2 = Message.builder().id("msg2").content("dummy").sender(user2)
        .recipient(user).build();
      var messages = Set.of(message1, message2);
      var thread =
        MessageThread.builder().id(THREAD_I_D).user2(user2).user1(user)
          .messages(messages).build();
      when(threadRepo.findById(THREAD_I_D)).thenReturn(Optional.of(thread));
      when(messageRepo.findById("msg1")).thenReturn(Optional.of(message1));
      when(messageRepo.findById("msg2")).thenReturn(Optional.of(message2));
      // When
      underTest.deleteThread(THREAD_I_D, USER_I_D);
      // Then
      assertThat(thread.isUser1Deleted()).isTrue();
      assertThat(thread.isUser2Deleted()).isFalse();
      assertThat(message1.isSenderDeleted()).isTrue();
      assertThat(message2.isRecipientDeleted()).isTrue();
    }
  }


  @Nested class WhenDeletingMessage {
    private final String MESSAGE_I_D = "MESSAGE_I_D";
    private final String USER_I_D = "USER_I_D";

    @BeforeEach void setup() {
    }

    @Test void deleteMessageInvalidID() {
      // Given
      when(messageRepo.findById(any())).thenReturn(Optional.empty());
      // Then
      assertThatThrownBy(() -> {
        underTest.deleteMessage(MESSAGE_I_D, USER_I_D);
      }).isInstanceOf(EntityNotFoundException.class);
    }

    @Test void deleteMessageValidID() {
      // Given
      var user = new User(USER_I_D);
      var user2 = new User(USER_I_D + "1");
      var message1 =
        Message.builder().content("dummy").sender(user).recipient(user2)
          .build();
      when(messageRepo.findById(MESSAGE_I_D))
        .thenReturn(Optional.ofNullable(message1));
      // When
      underTest.deleteMessage(MESSAGE_I_D, USER_I_D);
      // Then
      assertThat(message1).isNotNull();
      assertThat(message1.isSenderDeleted()).isTrue();
    }
  }


  @Nested class WhenMarkingAsRead {
    private final String MESSAGE_I_D = "MESSAGE_I_D";

    @BeforeEach void setup() {
    }

    @Test void markAsReadInvalidID() {
      // Given
      when(messageRepo.findById(any())).thenReturn(Optional.empty());
      // Then
      assertThatThrownBy(() -> {
        underTest.markAsRead(MESSAGE_I_D);
      }).isInstanceOf(EntityNotFoundException.class);
    }

    @Test void markAsReadValidID() {
      // Given
      var message1 = Message.builder().content("dummy").build();
      when(messageRepo.findById(MESSAGE_I_D))
        .thenReturn(Optional.ofNullable(message1));
      when(messageRepo.save(any())).thenAnswer(returnsFirstArg());
      // When
      underTest.markAsRead(MESSAGE_I_D);
      // Then
      assertThat(message1).isNotNull();
      assertThat(message1.getStatus()).isEqualTo(EtatMessage.READ);
    }
  }


  @Nested class WhenCheckingIfCanDo {
    private final String USERID = "USER_I_D";
    private final String USERID2 = "USER_I_D_2";
    private final String OBJECT_ID = "OBJECT_I_D";
    private final String OBJECT_ID2 = "OBJECT_I_D_2";

    @ParameterizedTest @EnumSource(value = MessageMethods.class, names = {
      MessageMethods.Names.GET_THREAD, MessageMethods.Names.DELETE_THREAD})
    void canDoThreadIDNotFound(MessageMethods method) {
      // Given
      when(threadRepo.getOne(any()))
        .thenThrow(javax.persistence.EntityNotFoundException.class);
      // Then
      assertThatThrownBy(() -> {
        boolean b = underTest.canDo(method, USERID, OBJECT_ID);
      }).isInstanceOf(EntityNotFoundException.class);
    }

    @ParameterizedTest @EnumSource(value = MessageMethods.class, names = {
      MessageMethods.Names.DELETE_MESSAGE, MessageMethods.Names.GET_MESSAGE,
      MessageMethods.Names.MARK_AS_READ}) void canDoMessageIDNotFound(
      MessageMethods method) {
      // Given
      when(messageRepo.getOne(any()))
        .thenThrow(javax.persistence.EntityNotFoundException.class);
      // Then
      assertThatThrownBy(() -> {
        boolean b = underTest.canDo(method, USERID, OBJECT_ID);
      }).isInstanceOf(EntityNotFoundException.class);
    }

    @ParameterizedTest @EnumSource(value = MessageMethods.class, names = {
      MessageMethods.Names.SEND_MESSAGE, MessageMethods.Names.GET_THREADLIST})
    void canSendMessageAndGetThreadList(MessageMethods method) {
      // When
      var res = underTest.canDo(method, USERID, OBJECT_ID);
      // Then
      assertThat(res).isTrue();
    }

    @ParameterizedTest @EnumSource(value = MessageMethods.class, names = {
      MessageMethods.Names.GET_THREAD, MessageMethods.Names.DELETE_THREAD})
    void canGetThreadOrDeleteThread(MessageMethods method) {
      // Given
      when(threadRepo.getOne(OBJECT_ID)).thenReturn(
        MessageThread.builder().id(OBJECT_ID).user1(new User(USERID))
          .user2(new User(USERID2)).build());
      when(threadRepo.getOne(OBJECT_ID2)).thenReturn(
        MessageThread.builder().id(OBJECT_ID2).user1(new User("NOTONE"))
          .user2(new User(USERID2)).build());
      // When
      var res = underTest.canDo(method, USERID, OBJECT_ID);
      var res2 = underTest.canDo(method, USERID, OBJECT_ID2);
      // Then
      assertThat(res).isTrue();
      assertThat(res2).isFalse();
    }

    @ParameterizedTest @EnumSource(value = MessageMethods.class, names = {
      MessageMethods.Names.ADD_ATTACHEMENT_TO_MESSAAGE,
      MessageMethods.Names.DELETE_ATTACHMENT_FOR_MESSAGE})
    void canAddOrDeleteAttachment(MessageMethods method) {
      // Given
      when(messageRepo.getOne(OBJECT_ID)).thenReturn(
        Message.builder().id(OBJECT_ID).sender(new User(USERID))
          .recipient(new User(USERID2)).build());
      when(messageRepo.getOne(OBJECT_ID2)).thenReturn(
        Message.builder().id(OBJECT_ID2).recipient(new User(USERID))
          .sender(new User(USERID2)).build());
      // When
      var res = underTest.canDo(method, USERID, OBJECT_ID);
      var res2 = underTest.canDo(method, USERID, OBJECT_ID2);
      // Then
      assertThat(res).isTrue();
      assertThat(res2).isFalse();
    }

    @ParameterizedTest @EnumSource(value = MessageMethods.class, names = {
      MessageMethods.Names.DELETE_MESSAGE, MessageMethods.Names.GET_MESSAGE,
      MessageMethods.Names.LOAD_ATTACHMENT_FOR_MESSAGE})
    void canDeleteMessageOrGetMessage(MessageMethods method) {
      // Given
      when(messageRepo.getOne(OBJECT_ID)).thenReturn(
        Message.builder().id(OBJECT_ID).sender(new User(USERID))
          .recipient(new User(USERID2)).build());
      when(messageRepo.getOne(OBJECT_ID2)).thenReturn(
        Message.builder().id(OBJECT_ID2).sender(new User("NOTONE"))
          .recipient(new User(USERID2)).build());
      // When
      var res = underTest.canDo(method, USERID, OBJECT_ID);
      var res2 = underTest.canDo(method, USERID, OBJECT_ID2);
      // Then
      assertThat(res).isTrue();
      assertThat(res2).isFalse();
    }

    @Test void canMarkAsRead() {
      // Given
      when(messageRepo.getOne(OBJECT_ID)).thenReturn(
        Message.builder().id(OBJECT_ID).sender(new User(USERID))
          .recipient(new User(USERID2)).build());
      when(messageRepo.getOne(OBJECT_ID2)).thenReturn(
        Message.builder().id(OBJECT_ID2).sender(new User(USERID2))
          .recipient(new User(USERID)).build());
      // When
      var res = underTest.canDo(MessageMethods.MARK_AS_READ, USERID, OBJECT_ID);
      var res2 =
        underTest.canDo(MessageMethods.MARK_AS_READ, USERID, OBJECT_ID2);
      // Then
      assertThat(res).isFalse();
      assertThat(res2).isTrue();
    }
  }


  @Nested class WhenAddingAttachmentToMessage {
    private final String MESSAGE_I_D = "id";
    private final String USER_ID = "userID";
    private final MockMultipartFile file =
      new MockMultipartFile("fileName", "ogFileName", "type",
        InputStream.nullInputStream());

    WhenAddingAttachmentToMessage() throws IOException {
    }

    @Test void addAttachementToMessaageInvalidID() {
      // Given
      var msg =
        Message.builder().id(MESSAGE_I_D).sender(new User(USER_ID)).build();
      when(messageRepo.getMessageForUser(MESSAGE_I_D, USER_ID))
        .thenReturn(Optional.empty());
      // Then
      assertThatThrownBy(() -> {
        underTest.addAttachementToMessaage(MESSAGE_I_D, file, USER_ID);
      }).isInstanceOf(EntityNotFoundException.class);
    }

    @Test void addAttachementToMessaageAlreadyHasAttachement() {
      // Given
      var message = Message.builder().id(MESSAGE_I_D).sender(new User(USER_ID))
        .messageAttachement(MessageAttachement.builder().build()).build();
      when(messageRepo.getMessageForUser(MESSAGE_I_D, USER_ID))
        .thenReturn(Optional.of(message));
      // Then
      assertThatThrownBy(() -> {
        underTest.addAttachementToMessaage(MESSAGE_I_D, file, USER_ID);
      }).isInstanceOf(AlreadyHasAttachementException.class);
    }

    @Test void addAttachementToMessaage() {
      // Given
      var message = Message.builder().id(MESSAGE_I_D).sender(new User(USER_ID))
        .content("dummy").build();
      when(messageRepo.getMessageForUser(MESSAGE_I_D, USER_ID))
        .thenReturn(Optional.of(message));
      when(fileService.uploadFile(eq(file), any()))
        .thenReturn(new FileInfo("fileID", "fileID.ext", file));
      when(messageRepo.save(any())).thenAnswer(returnsFirstArg());
      when(messageAttachementRepo.save(any())).thenAnswer(returnsFirstArg());
      // When
      final var res =
        underTest.addAttachementToMessaage(MESSAGE_I_D, file, USER_ID);
      // Then
      assertThat(res.getMessageAttachement()).isNotNull();
      assertThat(res.getMessageAttachement().getMessage().getId())
        .isEqualTo(MESSAGE_I_D);
    }
  }


  @Nested class WhenLoadingAttachement {
    final byte[] bytes = "SampleData".getBytes(StandardCharsets.UTF_8);
    final MockMultipartFile file = new MockMultipartFile("imageID", bytes);
    final FileInfo fileInfo = new FileInfo("imageID", "imageID.jpg", file);
    private final String MESSAGE_ID = "id";
    private final String USER_ID = "userID";

    @Test void loadAttachement_MessageNotFound() {
      when(messageRepo.getMessageForUser(MESSAGE_ID, USER_ID))
        .thenReturn(Optional.empty());
      assertThatThrownBy(
        () -> underTest.loadAttachmentForMessage(MESSAGE_ID, USER_ID))
        .isInstanceOf(EntityNotFoundException.class);
    }

    @Test void loadAttachment_AttachmentNotFound() {
      var message =
        Message.builder().id(MESSAGE_ID).sender(new User(USER_ID)).build();
      when(messageRepo.getMessageForUser(MESSAGE_ID, USER_ID))
        .thenReturn(Optional.of(message));
      assertThatThrownBy(
        () -> underTest.loadAttachmentForMessage(MESSAGE_ID, USER_ID))
        .isInstanceOf(EntityNotFoundException.class);
    }

    @Test void loadAttachment_Success() {
      var message = Message.builder().id(MESSAGE_ID).sender(new User(USER_ID))
        .messageAttachement(
          MessageAttachement.builder().fileName("filename").build()).build();
      when(messageRepo.getMessageForUser(MESSAGE_ID, USER_ID))
        .thenReturn(Optional.of(message));
      when(fileService.loadResource(any(), any()))
        .thenReturn(file.getResource());
      var res = underTest.loadAttachmentForMessage(MESSAGE_ID, USER_ID);
      assertThat(res).isEqualTo(file.getResource());
    }
  }
}
