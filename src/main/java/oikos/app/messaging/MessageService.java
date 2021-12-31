package oikos.app.messaging;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import oikos.app.buyer.BuyerRepo;
import oikos.app.common.configurations.AppProperties;
import oikos.app.common.exceptions.BaseException;
import oikos.app.common.exceptions.EntityNotFoundException;
import oikos.app.common.exceptions.InternalServerError;
import oikos.app.common.pubsub.ChannelType;
import oikos.app.common.pubsub.PubSubService;
import oikos.app.common.services.FileService;
import oikos.app.users.Role;
import oikos.app.users.User;
import oikos.app.users.UserRepo;
import oikos.app.common.utils.Authorizable;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Created by Mohamed Haamdi on 12/02/2021
 */
@Service @Transactional @RequiredArgsConstructor @Slf4j
public class MessageService
  implements Authorizable<MessageService.MessageMethods> {
  private final MessageThreadRepo threadRepo;
  private final MessageRepo messageRepo;
  private final MessageAttachementRepo messageAttachementRepo;
  private final UserRepo userRepo;
  private final PubSubService pubSubService;
  private final BuyerRepo buyerRepo;
  private final FileService fileService;
  private final AppProperties appProperties;
  private Path messageDir;


  @ToString enum MessageMethods {
    GET_MESSAGE(Names.GET_MESSAGE), GET_THREADLIST(
      Names.GET_THREADLIST), SEND_MESSAGE(Names.SEND_MESSAGE), GET_THREAD(
      Names.GET_THREAD), DELETE_THREAD(Names.DELETE_THREAD), DELETE_MESSAGE(
      Names.DELETE_MESSAGE), MARK_AS_READ(
      Names.MARK_AS_READ), ADD_ATTACHEMENT_TO_MESSAAGE(
      Names.ADD_ATTACHEMENT_TO_MESSAAGE), LOAD_ATTACHMENT_FOR_MESSAGE(
      Names.LOAD_ATTACHMENT_FOR_MESSAGE), DELETE_ATTACHMENT_FOR_MESSAGE(
      Names.DELETE_ATTACHMENT_FOR_MESSAGE);
    private final String label;

    MessageMethods(String label) {
      this.label = label;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE) public static class Names {
      public static final String GET_THREADLIST = "GET_THREADLIST";
      public static final String SEND_MESSAGE = "SEND_MESSAGE";
      public static final String GET_THREAD = "GET_THREAD";
      public static final String DELETE_THREAD = "DELETE_THREAD";
      public static final String DELETE_MESSAGE = "DELETE_MESSAGE";
      public static final String MARK_AS_READ = "MARK_AS_READ";
      public static final String GET_MESSAGE = "GET_MESSAGE";
      public static final String DELETE_ATTACHMENT_FOR_MESSAGE =
        "DELETE_ATTACHMENT_FOR_MESSAGE";
      public static final String ADD_ATTACHEMENT_TO_MESSAAGE =
        "ADD_ATTACHEMENT_TO_MESSAAGE";
      public static final String LOAD_ATTACHMENT_FOR_MESSAGE =
        "LOAD_ATTACHMENT_FOR_MESSAGE";
    }
  }

  public Page<MessageThread> getThreadList(String userId, Pageable paging) {
    log.info("Getting thread list for user {} page {}", userId,
      paging.getPageNumber());
    return threadRepo.findMessageThreadByUserID(userId, paging);
  }

  public Message sendMessage(SendMessageRequest req, String senderID) {
    log.info("Sending message from {} to {}", senderID, req.getRecipientId());
    if (!userRepo.existsById(req.getRecipientId()))
      throw new EntityNotFoundException(User.class, req.getRecipientId());

    var sender = userRepo.getOne(senderID);
    if (sender.getRoles().equals(Set.of(Role.BUYER)) && !buyerRepo
      .getByUserID(senderID).isValidated())
      throw new BaseException(
        "Buyer is not validated by Oikos and can't send messages.");
    MessageThread thread;
    // We get the thread to add the message to
    final Optional<MessageThread> optThread = threadRepo
      .findMessageThreadBetweenUsersByID(req.getRecipientId(), senderID);
    // If the thread exists we simply append the message to the end
    // Else we create a new thread and add the message to it
    thread = optThread.orElseGet(
      () -> MessageThread.builder().user1(userRepo.getOne(req.getRecipientId()))
        .user2(sender).messages(new HashSet<>()).build());
    thread.setUser2Deleted(false);
    thread.setUser1Deleted(false);
    thread.setDateLastMessage(LocalDateTime.now());
    thread = threadRepo.save(thread);
    var msg = buildMessage(req, senderID, thread);
    thread.getMessages().add(msg);
    msg = messageRepo.save(msg);
    // After persisting to the database successfully, we also send the message to
    // the recipient in a pub sub message queue.
    pubSubService.publish(ChannelType.MESSAGES, req.getRecipientId(), msg);
    return msg;
  }

  public Page<Message> getThread(String threadID, String userID,
    Pageable pageable) {
    log.info("Getting messages in thread {} page {}", threadID,
      pageable.getPageNumber());
    if (!threadRepo.existsById(threadID))
      throw new EntityNotFoundException(MessageThread.class, threadID);
    return messageRepo.getMessagesInThread(threadID, userID, pageable);
  }

  public void deleteThread(String threadID, String userID) {
    log.info("Deleting thread {}.", threadID);
    var thread = threadRepo.findById(threadID).orElseThrow(
      () -> new EntityNotFoundException(MessageThread.class, threadID));
    if (userID.equals(thread.getUser1().getId())) {
      thread.setUser1Deleted(true);
    } else {
      thread.setUser2Deleted(true);
    }
    threadRepo.save(thread);
    // We loop through all messages in the thread and mark them as deleted
    thread.getMessages().forEach(msg -> deleteMessage(msg.getId(), userID));
    log.info("Deleted thread {}", threadID);
  }

  public void deleteMessage(String messageID, String userID) {
    log.info("Deleting message {}.", messageID);
    var message = messageRepo.findById(messageID)
      .orElseThrow(() -> new EntityNotFoundException(Message.class, messageID));
    if (userID.equals(message.getRecipient().getId())) {
      message.setRecipientDeleted(true);
    } else {
      message.setSenderDeleted(true);
    }
    messageRepo.save(message);
    log.info("Deleted message {}", messageID);
  }

  public void markAsRead(String messageID) {
    log.info("Marking message {} as read.", messageID);
    var message = messageRepo.findById(messageID)
      .orElseThrow(() -> new EntityNotFoundException(Message.class, messageID));
    message.setStatus(EtatMessage.READ);
    messageRepo.save(message);
  }

  @Override public boolean canDo(MessageMethods methodName, String userID,
    String objectID) {
    try {
      return switch (methodName) {
        case GET_THREAD, DELETE_THREAD ->
          threadRepo.getOne(objectID).getUser1().getId().equals(userID)
            || threadRepo.getOne(objectID).getUser2().getId().equals(userID);
        case SEND_MESSAGE, GET_THREADLIST -> true;
        case DELETE_MESSAGE, GET_MESSAGE, LOAD_ATTACHMENT_FOR_MESSAGE ->
          messageRepo.getOne(objectID).getSender().getId().equals(userID)
            || messageRepo.getOne(objectID).getRecipient().getId()
            .equals(userID);
        case MARK_AS_READ -> messageRepo.getOne(objectID).getRecipient().getId()
          .equals(userID);
        case ADD_ATTACHEMENT_TO_MESSAAGE, DELETE_ATTACHMENT_FOR_MESSAGE -> messageRepo
          .getOne(objectID).getSender().getId().equals(userID);
      };
    } catch (javax.persistence.EntityNotFoundException e) {
      throw new EntityNotFoundException(Message.class, objectID);
    }
  }

  public Message getMessage(String messageID, String userID) {
    log.info("Getting Message {} for User {}", messageID, userID);
    return messageRepo.getMessageForUser(messageID, userID)
      .orElseThrow(() -> new EntityNotFoundException(Message.class, messageID));
  }

  public Message addAttachementToMessaage(String messageID, MultipartFile file,
    String userID) {
    log.info("Adding attachement file to message{}", messageID);
    var message = messageRepo.getMessageForUser(messageID, userID)
      .orElseThrow(() -> new EntityNotFoundException(Message.class, messageID));
    if (message.getMessageAttachement() != null) {
      throw new AlreadyHasAttachementException(
        "This message already has an attachement! Send a new message and attach the file to the new one instead.");
    }

    var fileInfo = fileService.uploadFile(file, messageDir);
    var dbFile = MessageAttachement.builder().id(fileInfo.getId())
      .fileName(fileInfo.getNewName())
      .fileType(fileInfo.getFile().getContentType())
      .originalName(fileInfo.getFile().getOriginalFilename())
      .size(fileInfo.getFile().getSize()).message(message).build();
    dbFile = messageAttachementRepo.save(dbFile);
    message.setMessageAttachement(dbFile);
    message = messageRepo.save(message);
    return message;
  }

  public Resource loadAttachmentForMessage(String messageID, String userID) {
    log.info("Getting attachment for message{}", messageID);
    var dbMessage = messageRepo.getMessageForUser(messageID, userID)
      .orElseThrow(() -> new EntityNotFoundException(Message.class, messageID))
      .getMessageAttachement();
    if (dbMessage == null) {
      throw new EntityNotFoundException(MessageAttachement.class, messageID);
    }
    return fileService.loadResource(messageDir, dbMessage.getFileName());
  }

  public void deleteAttachmentForMessage(String messageID, String userID) {
    log.info("Deleting attachment for message{}", messageID);
    doDeleteAttachment(messageID);
    var message = messageRepo.getMessageForUser(messageID, userID)
      .orElseThrow(() -> new EntityNotFoundException(Message.class, messageID));
    message.setMessageAttachement(null);
    messageRepo.save(message);
  }


  private void doDeleteAttachment(String messageID) {
    var attachment = messageAttachementRepo.getAttachementForMessage(messageID);
    if (attachment.isPresent()) {
      messageAttachementRepo.delete(attachment.get());
      fileService.deleteFile(messageDir, attachment.get().getFileName());
    }
  }

  private Message buildMessage(SendMessageRequest req, String userID,
    MessageThread thread) {
    var recipient = userRepo.getOne(req.getRecipientId());
    var sender = userRepo.getOne(userID);
    return Message.builder().recipient(recipient).content(req.getContent())
      .sender(sender).status(EtatMessage.SENT).thread(thread).build();
  }

  @PostConstruct private void initMessageDir() {
    this.messageDir =
      Paths.get(appProperties.getFiles().getMessages()).toAbsolutePath()
        .normalize();
    try {
      if (!Files.exists(messageDir)) {
        Files.createDirectory(messageDir);
      }
    } catch (Exception exception) {
      log.error("messageservice.initmessagedir", exception);
      throw new InternalServerError(
        "Error creating message attachement directory", exception);
    }
  }
}
