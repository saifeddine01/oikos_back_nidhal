package oikos.app.notifications;

import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import oikos.app.common.configurations.SchedulingConfig;
import oikos.app.common.exceptions.EntityNotFoundException;
import oikos.app.common.pubsub.ChannelType;
import oikos.app.common.pubsub.PubSubService;
import oikos.app.common.utils.Authorizable;
import oikos.app.common.utils.DateUtils;
import oikos.app.users.User;
import oikos.app.users.UserRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Created by Mohamed Haamdi on 15/02/2021. */
@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class NotificationService implements Authorizable<NotificationService.NotificationMethods> {
  private final NotificationRepo repo;
  private final ScheduledNotificationRepo scheduledRepo;
  private final UserRepo userRepo;
  private final PubSubService pubSubService;
  private final SchedulingConfig scheduleConfig;
  @ToString
  enum NotificationMethods {
    GET_NOTIFICATION(Names.GET_NOTIFICATION),
    ADD_NOTIFICATION(Names.ADD_NOTIFICATION),
    GET_UNREAD_NOTIFICATIONS(Names.GET_UNREAD_NOTIFICATIONS),
    GET_ALL_NOTIFICATIONS(Names.GET_ALL_NOTIFICATIONS),
    DELETE_NOTIFICATION(Names.DELETE_NOTIFICATION),
    MARK_NOTIFICATION_AS_READ(Names.MARK_NOTIFICATION_AS_READ);
    private final String label;

    NotificationMethods(String label) {
      this.label = label;
    }

    public static class Names {

      public static final String ADD_NOTIFICATION = "ADD_NOTIFICATION";
      public static final String GET_UNREAD_NOTIFICATIONS = "GET_UNREAD_NOTIFICATIONS";
      public static final String GET_ALL_NOTIFICATIONS = "GET_ALL_NOTIFICATIONS";
      public static final String DELETE_NOTIFICATION = "DELETE_NOTIFICATION";
      public static final String MARK_NOTIFICATION_AS_READ = "MARK_NOTIFICATION_AS_READ";
      public static final String GET_NOTIFICATION = "GET_NOTIFICATION";

      private Names() {}
    }
  }

  @Transactional
  public Notification addNotification(CreateNotificationRequest req) {

    log.info("Adding notification from request {}", req);
    if (!userRepo.existsById(req.getUserId())) {
      throw new EntityNotFoundException(User.class,req.getUserId());
    }
    var user = userRepo.getOne(req.getUserId());
    Notification n = Notification.builder().content(req.getContent()).
        etat(EtatNotification.NON_VU).lien(req.getLien()).utilisateur(user).build();
    n = repo.save(n);
    //After we persisted the notification, we send it to the pub sub message broker
    //for real time notifications.
    pubSubService.publish(ChannelType.NOTIFICATIONS,req.getUserId(),n);
    return n;
  }

  @Transactional
  public Notification addNotification(String recipientID,String content,String link){
    return addNotification(
      new CreateNotificationRequest(recipientID
        ,content
        ,link));
  }

  @Transactional
  public Page<Notification> getUnreadNotifications(String userID, Pageable paging) {
    log.info(
        "Getting unread notifications for {} page {}",
        userID,
        paging.getPageNumber());
    return repo.getUnreadNotifications(userID, paging);
  }

  @Transactional
  public Page<Notification> getAllNotifications(String userID, Pageable paging) {
    log.info(
        "Getting all notifications for {} page {}", userID, paging.getPageNumber());
    return repo.getAllNotifications(userID, paging);
  }

  @Transactional
  public void deleteNotification(String notificationId) {
    log.info("Deleting notification {}", notificationId);
    Notification notification =
        repo.findById(notificationId)
            .orElseThrow(() -> new EntityNotFoundException(Notification.class, notificationId));
    if (notification.getEtat() == EtatNotification.ARCHIVE)
      throw new EntityNotFoundException(Notification.class, notificationId);
    notification.setEtat(EtatNotification.ARCHIVE);
    repo.save(notification);
    log.info("Responding with a Done Response.");
  }

  @Transactional
  public void markNotificationAsRead(String notificationId) {
    log.info("Marking notification {} as read", notificationId);
    Notification notification =
        repo.findById(notificationId)
            .orElseThrow(() -> new EntityNotFoundException(Notification.class, notificationId));
    notification.setEtat(EtatNotification.VU);
    repo.save(notification);
    log.info("Responding with a Done Response.");
  }

  @Override public boolean canDo(NotificationMethods methodName, String userID,
      String objectID) {
    return switch (methodName) {
      case DELETE_NOTIFICATION,MARK_NOTIFICATION_AS_READ, GET_NOTIFICATION -> {
        try {
          yield repo.getOne(objectID).getUtilisateur().getId().equals(userID);
        } catch (Exception e) {
          throw new EntityNotFoundException(Notification.class,objectID);
        }
      }
      case ADD_NOTIFICATION ,GET_ALL_NOTIFICATIONS,GET_UNREAD_NOTIFICATIONS-> true;
    };
  }
  @Transactional
  public ScheduledNotification addNotificationToSchedule(ScheduledNotification request){
    return scheduledRepo.save(request);
  }
  @Transactional
  public Notification sendPastNotification(ScheduledNotification request){
    scheduledRepo.delete(request);
    return addNotification(new CreateNotificationRequest(request.getUtilisateur().
      getId(),request.getContent(), request.getLink()));

  }

  public void scheduleNotification(ScheduledNotification request){
    Runnable task = () -> {
      sendPastNotification(request);
    };
    scheduleConfig.executeTask(task, DateUtils.convertToDateViaInstant(request.getInstant()));
  }
  @Transactional
  public List<ScheduledNotification> getScheduledNotifications(){
    return scheduledRepo.findAll();
  }
  @Transactional
  public Notification getNotification(String notificationId) {
    return repo.findById(notificationId).orElseThrow(()-> new EntityNotFoundException(Notification.class,notificationId));
  }
}
