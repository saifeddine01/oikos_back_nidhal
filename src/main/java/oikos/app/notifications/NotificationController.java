package oikos.app.notifications;

import lombok.AllArgsConstructor;
import oikos.app.common.responses.DoneResponse;
import oikos.app.common.utils.Monitor;
import oikos.app.security.CurrentUser;
import oikos.app.security.OikosUserDetails;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.text.MessageFormat;

import static oikos.app.common.utils.NanoIDGenerator.NANOID_SIZE;

/** Created by Mohamed Haamdi on 15/02/2021. */
@RestController
@AllArgsConstructor
@RequestMapping("/notifications")
@Validated
@Monitor
public class NotificationController {
  private final NotificationService service;
  private final ModelMapper mapper;

  @PreAuthorize("@notificationService.canDo('ADD_NOTIFICATION',null,null)")
  @PostMapping
  public NotificationResponse addNotification(
      @Valid @RequestBody CreateNotificationRequest request) {
    var notification = service.addNotification(request);
    return mapper.map(notification, NotificationResponse.class);
  }

  @PreAuthorize("@notificationService.canDo('GET_UNREAD_NOTIFICATIONS',#user.username,null)")
  @GetMapping("/unread")
  public Page<NotificationResponse> getUnreadNotifications(
      @CurrentUser OikosUserDetails user,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
    Pageable paging = PageRequest.of(page, size);
    final var unreadNotifications = service.getUnreadNotifications(user.getUsername(), paging);
    return unreadNotifications.map(
        notification -> mapper.map(notification, NotificationResponse.class));
  }

  @PreAuthorize("@notificationService.canDo('GET_ALL_NOTIFICATIONS',#user.username,null)")
  @GetMapping
  public Page<NotificationResponse> getAllNotifications(
      @CurrentUser OikosUserDetails user,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
    Pageable paging = PageRequest.of(page, size);
    final var allNotifications = service.getAllNotifications(user.getUsername(), paging);
    return allNotifications.map(
        notification -> mapper.map(notification, NotificationResponse.class));
  }

  @PreAuthorize("@notificationService.canDo('DELETE_NOTIFICATION',#user.username,#notificationId)")
  @DeleteMapping("/{notificationId}")
  public DoneResponse deleteNotification(
      @CurrentUser OikosUserDetails user,
      @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String notificationId) {
    service.deleteNotification(notificationId);
    return new DoneResponse(
        MessageFormat.format("Notification {0} has been deleted", notificationId));
  }

  @PreAuthorize("@notificationService.canDo('GET_NOTIFICATION',#user.username,#notificationId)")
  @GetMapping("/{notificationId}")
  public NotificationResponse getNotification(
      @CurrentUser OikosUserDetails user,
      @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String notificationId) {
    var notification = service.getNotification(notificationId);
    return mapper.map(notification, NotificationResponse.class);
  }

  @PreAuthorize(
      "@notificationService.canDo('MARK_NOTIFICATION_AS_READ',#user.username,#notificationId)")
  @GetMapping("/{notificationId}/read")
  public DoneResponse markNotificationAsRead(
      @CurrentUser OikosUserDetails user,
      @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String notificationId) {
    service.markNotificationAsRead(notificationId);
    return new DoneResponse(
        MessageFormat.format("Notification {0} has been marked as read", notificationId));
  }
}
