package oikos.app.messaging;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oikos.app.common.responses.DoneResponse;
import oikos.app.common.exceptions.FileEmptyException;
import oikos.app.common.utils.Monitor;
import oikos.app.security.CurrentUser;
import oikos.app.security.OikosUserDetails;
import oikos.app.common.services.ResourceService;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.text.MessageFormat;

import static oikos.app.common.utils.NanoIDGenerator.NANOID_SIZE;

/**
 * Created by Mohamed Haamdi on 08/02/2021
 */
@Slf4j @RestController @AllArgsConstructor @Validated @Monitor
@RequestMapping("/messages") public class MessageController {
  private final MessageService service;
  private final ResourceService resourceService;
  private final ModelMapper mapper;

  @PreAuthorize("@messageService.canDo('GET_THREADLIST',#user.username,#user.username)")
  @GetMapping("/threads")
  public Page<MessageThreadListResponse> getMessageThreads(
    @CurrentUser OikosUserDetails user,
    @RequestParam(defaultValue = "0") @Min(0) int page,
    @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
    Pageable paging = PageRequest.of(page, size);
    Page<MessageThread> threadList =
      service.getThreadList(user.getUsername(), paging);
    return threadList.map(thread -> {
      MessageThreadListResponse response =
        mapper.map(thread, MessageThreadListResponse.class);
      response.setRecipientID(
        user.getUsername().equals(thread.getUser1().getId()) ?
          thread.getUser2().getId() :
          thread.getUser1().getId());
      return response;
    });
  }

  @PreAuthorize("@messageService.canDo('GET_THREAD',#user.username,#threadID)")
  @GetMapping("/threads/{threadID}")
  public Page<MessageResponse> getMessageThread(
    @CurrentUser OikosUserDetails user,
    @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String threadID,
    @RequestParam(defaultValue = "0") @Min(0) int page,
    @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
    Pageable paging = PageRequest.of(page, size);
    Page<Message> thread =
      service.getThread(threadID, user.getUsername(), paging);
    return thread.map(message -> mapper.map(message, MessageResponse.class));
  }

  @PreAuthorize("@messageService.canDo('GET_MESSAGE',#user.username,#messageID)")
  @GetMapping("/{messageID}")
  public MessageResponse getMessage(@CurrentUser OikosUserDetails user,
    @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable
      String messageID) {
    var res = service.getMessage(messageID, user.getUsername());
    return mapper.map(res, MessageResponse.class);
  }

  @PreAuthorize("@messageService.canDo('SEND_MESSAGE',#user.username,#user.username)")
  @PostMapping
  public MessageResponse sendMessage(@CurrentUser OikosUserDetails user,
    @Valid @RequestBody SendMessageRequest request) {
    var message = service.sendMessage(request, user.getUsername());
    return mapper.map(message, MessageResponse.class);
  }

  @PreAuthorize("@messageService.canDo('ADD_ATTACHEMENT_TO_MESSAAGE',#user.username,#messageID)")
  @PostMapping("/{messageID}/attachement")
  public MessageResponse addAttachementToMessage(
    @CurrentUser OikosUserDetails user,
    @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String messageID,
    @RequestParam("file") MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new FileEmptyException("Please select a valid file for upload");
    }
    var message =
      service.addAttachementToMessaage(messageID, file, user.getUsername());
    return mapper.map(message, MessageResponse.class);
  }

  @PreAuthorize("@messageService.canDo('LOAD_ATTACHMENT_FOR_MESSAGE',#user.username,#messageID)")
  @GetMapping("/{messageID}/attachement")
  public ResponseEntity<Resource> getAttachement(
    @CurrentUser OikosUserDetails user,
    @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String messageID,
    HttpServletRequest request) {
    final var resource =
      service.loadAttachmentForMessage(messageID, user.getUsername());
    return resourceService.getResourceResponse(request, resource);
  }

  @PreAuthorize("@messageService.canDo('DELETE_ATTACHMENT_FOR_MESSAGE',#user.username,#messageID)")
  @DeleteMapping("/{messageID}/attachement")
  public DoneResponse deleteAttachement(@CurrentUser OikosUserDetails user,
    @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable
      String messageID) {
    service.deleteAttachmentForMessage(messageID, user.getUsername());
    return new DoneResponse(MessageFormat
      .format("Attachement for message {0} has been deleted", messageID));
  }

  @PreAuthorize("@messageService.canDo('DELETE_MESSAGE',#user.username,#messageID)")
  @DeleteMapping("/{messageID}") public DoneResponse deleteMessage(
    @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String messageID,
    @CurrentUser OikosUserDetails user) {
    service.deleteMessage(messageID, user.getUsername());
    return new DoneResponse(
      MessageFormat.format("Message {0} has been deleted", messageID));
  }

  @PreAuthorize("@messageService.canDo('DELETE_THREAD',#user.username,#threadID)")
  @DeleteMapping("/threads/{threadID}") public DoneResponse deleteThread(
    @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String threadID,
    @CurrentUser OikosUserDetails user) {
    service.deleteThread(threadID, user.getUsername());
    return new DoneResponse(
      MessageFormat.format("Message Thread {0} has been deleted", threadID));
  }

  @PreAuthorize("@messageService.canDo('MARK_AS_READ',#user.username,#messageID)")
  @GetMapping("/{messageID}/read") public DoneResponse markMessageAsRead(
    @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String messageID,
    @CurrentUser OikosUserDetails user) {
    service.markAsRead(messageID);
    return new DoneResponse(
      MessageFormat.format("Message {0} has been marked as read", messageID));
  }
}
