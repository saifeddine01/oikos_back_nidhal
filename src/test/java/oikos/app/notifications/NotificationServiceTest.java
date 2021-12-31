package oikos.app.notifications;

import lombok.extern.slf4j.Slf4j;
import oikos.app.common.configurations.SchedulingConfig;
import oikos.app.common.exceptions.EntityNotFoundException;
import oikos.app.notifications.NotificationService.NotificationMethods;
import oikos.app.common.pubsub.ChannelType;
import oikos.app.common.pubsub.PubSubService;
import oikos.app.users.User;
import oikos.app.users.UserRepo;
import oikos.app.common.utils.NanoIDGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/** Created by Mohamed Haamdi on 27/04/2021. */
@ExtendWith(MockitoExtension.class)
@Slf4j
class NotificationServiceTest {

  @Mock private NotificationRepo repo;
  @Mock private UserRepo userRepo;
  @Mock private SchedulingConfig config;
  @Mock private ScheduledNotificationRepo notificationRepo;
  @Mock private PubSubService pubSubService;
  @InjectMocks private NotificationService underTest;

  @Test
  void testScheduleNotification() {
    // Setup
    final ScheduledNotification request =
        new ScheduledNotification(
            new User("id"),
            "content",
            "link",
            LocalDateTime.of(2022, 1, 1, 0, 0, 0, 0).toInstant(ZoneOffset.UTC));

    // Run the test
    underTest.scheduleNotification(request);

    // Verify the results
    verify(config).executeTask(any(), any());
  }

  @Test
  void testSendPastNotification() {
    // Setup
    final ScheduledNotification request =
        new ScheduledNotification(
            new User("id"),
            "content",
            "link",
            LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0).toInstant(ZoneOffset.UTC));
    when(userRepo.existsById("id")).thenReturn(true);
    when(userRepo.getOne("id")).thenReturn(new User("id"));
    // Configure NotificationRepo.save(...).

    when(repo.save(any(Notification.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());

    // Run the test
    final Notification result = underTest.sendPastNotification(request);
    // Verify the results
    assertThat(result).isNotNull();
    verify(notificationRepo).delete(request);
  }

  @Nested
  class WhenGettingNotification {
    @Test
    void getNotification() {
      // given
      var notification = Notification.builder().id("id").build();
      when(repo.findById("id")).thenReturn(Optional.of(notification));
      // when
      var res = underTest.getNotification("id");
      // then
      assertThat(res).isEqualTo(notification);
    }

    @Test
    void getNotificationNotFound() {
      // given
      when(repo.findById("id")).thenReturn(Optional.empty());
      // then
      assertThatThrownBy(() -> underTest.getNotification("id"))
          .isInstanceOf(EntityNotFoundException.class);
    }
  }

  @Nested
  class WhenAddingNotification {
    private CreateNotificationRequest req;

    @BeforeEach
    void setup() {
      req = new CreateNotificationRequest("USERID", "Content", null);
    }

    @Test
    void addNotificationUserDoesntExist() {
      // Given
      when(userRepo.existsById(any())).thenReturn(false);
      // Then
      assertThatThrownBy(
              () -> {
                underTest.addNotification(req);
              })
          .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void addNotification() {
      // Given
      when(userRepo.existsById(any())).thenReturn(true);
      when(repo.save(any()))
          .thenAnswer(
              a -> {
                Notification argument = a.getArgument(0);
                argument.setId(NanoIDGenerator.generateSMSVerificationCode());
                argument.setUtilisateur(new User(req.getUserId()));
                return argument;
              });
      // When
      var n = underTest.addNotification(req);
      // Then
      assertThat(n.getId()).isNotNull();
      assertThat(n.getUtilisateur().getId()).isEqualTo(req.getUserId());
      assertThat(n.getEtat()).isEqualTo(EtatNotification.NON_VU);
      verify(pubSubService).publish(ChannelType.NOTIFICATIONS, req.getUserId(), n);
    }
  }

  @Nested
  class WhenGettingUnreadNotifications {

    private final String USER_I_D = "USER_I_D";
    @Mock private Pageable paging;

    @Test
    void getUnreadNotification() {
      // Given
      var n = Notification.builder().build();
      var n2 = Notification.builder().build();
      var n3 = Notification.builder().build();
      String USER_I_D = "USER_I_D";
      when(repo.getUnreadNotifications(USER_I_D, paging))
          .thenReturn(new PageImpl<>(List.of(n, n2, n3)));
      // When
      var v = underTest.getUnreadNotifications(USER_I_D, paging);
      // Then
      assertThat(v).contains(n, n2, n3);
    }
  }

  @Nested
  class WhenGettingAllNotifications {

    private final String USER_I_D = "USER_I_D";
    @Mock private Pageable paging;

    @Test
    void getAllNotification() {
      // Given
      var n = Notification.builder().build();
      var n2 = Notification.builder().build();
      var n3 = Notification.builder().build();
      String USER_I_D = "USER_I_D";
      when(repo.getAllNotifications(USER_I_D, paging))
          .thenReturn(new PageImpl<>(List.of(n, n2, n3)));
      // When
      var v = underTest.getAllNotifications(USER_I_D, paging);
      // Then
      assertThat(v).contains(n, n2, n3);
    }
  }

  @Nested
  class WhenDeletingNotification {
    private final String NOTIFICATION_ID = "NOTIFICATION_ID";
    @Spy NotificationRepo repoSpy;

    @Test
    void deleteNotificationInvalidID() {
      // Given
      when(repo.findById(NOTIFICATION_ID)).thenReturn(Optional.empty());
      // Then
      assertThatThrownBy(() -> underTest.deleteNotification(NOTIFICATION_ID))
          .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deleteNotificationAlreadyArchived() {
      // Given
      var notification = Notification.builder().etat(EtatNotification.ARCHIVE).build();
      when(repo.findById(NOTIFICATION_ID)).thenReturn(Optional.ofNullable(notification));
      // Then
      assertThatThrownBy(() -> underTest.deleteNotification(NOTIFICATION_ID))
          .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deleteNotification() {
      var service =
          new NotificationService(repoSpy, notificationRepo, userRepo, pubSubService, config);
      // Given
      var notification = Notification.builder().build();
      doReturn(Optional.ofNullable(notification)).when(repoSpy).findById(NOTIFICATION_ID);
      // When
      service.deleteNotification(NOTIFICATION_ID);
      assertThat(notification).isNotNull();
      verify(repoSpy).save(notification);
      assertThat(notification.getEtat()).isEqualTo(EtatNotification.ARCHIVE);
    }
  }

  @Nested
  class WhenMarkingNotificationAsRead {
    private final String NOTIFICATION_ID = "NOTIFICATION_ID";
    @Spy NotificationRepo repoSpy;

    @Test
    void markAsReadNotificationInvalidID() {
      // Given
      when(repo.findById(NOTIFICATION_ID)).thenReturn(Optional.empty());
      // Then
      assertThatThrownBy(() -> underTest.markNotificationAsRead(NOTIFICATION_ID))
          .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void markAsReadNotification() {
      var service =
          new NotificationService(repoSpy, notificationRepo, userRepo, pubSubService, config);
      // Given
      var notification = Notification.builder().build();
      doReturn(Optional.ofNullable(notification)).when(repoSpy).findById(NOTIFICATION_ID);
      // When
      service.markNotificationAsRead(NOTIFICATION_ID);
      assertThat(notification).isNotNull();
      verify(repoSpy).save(notification);
      assertThat(notification.getEtat()).isEqualTo(EtatNotification.VU);
    }
  }

  @Nested
  class WhenCheckingIfCanDo {
    private final String USERID = "USER_I_D";
    private final String USERID2 = "USERID2";

    private final String OBJECTID = "OBJECT_I_D";
    private final String OBJECTID2 = "OBJECT_I_D2";
    private final NotificationMethods METHOD_NAME = NotificationMethods.ADD_NOTIFICATION;
    private final String USER_I_D = "USER_I_D";
    private final String OBJECT_I_D = "OBJECT_I_D";

    @ParameterizedTest
    @EnumSource(
        value = NotificationMethods.class,
        names = {
          NotificationMethods.Names.ADD_NOTIFICATION,
          NotificationMethods.Names.GET_ALL_NOTIFICATIONS,
          NotificationMethods.Names.GET_UNREAD_NOTIFICATIONS
        })
    void canAddNotificationsAndGetAllorUnreadNotifications(NotificationMethods method) {
      var res = underTest.canDo(method, USERID, OBJECTID);
      assertThat(res).isTrue();
    }

    @ParameterizedTest
    @EnumSource(
        value = NotificationMethods.class,
        names = {
          NotificationMethods.Names.GET_NOTIFICATION,
          NotificationMethods.Names.DELETE_NOTIFICATION,
          NotificationMethods.Names.MARK_NOTIFICATION_AS_READ
        })
    void canDoNotificationIDNotFound(NotificationMethods method) {
      // Given
      when(repo.getOne(any())).thenThrow(javax.persistence.EntityNotFoundException.class);
      // Then
      assertThatThrownBy(
              () -> {
                boolean b = underTest.canDo(method, USERID, OBJECTID);
              })
          .isInstanceOf(EntityNotFoundException.class);
    }

    @ParameterizedTest
    @EnumSource(
        value = NotificationMethods.class,
        names = {
          NotificationMethods.Names.GET_NOTIFICATION,
          NotificationMethods.Names.DELETE_NOTIFICATION,
          NotificationMethods.Names.MARK_NOTIFICATION_AS_READ
        })
    void canDeleteOrMarkNotificationAsRead(NotificationMethods method) {
      // Given
      var notification1 = Notification.builder().id(OBJECTID).utilisateur(new User(USERID)).build();
      var notification2 =
          Notification.builder().id(OBJECTID2).utilisateur(new User(USERID2)).build();
      when(repo.getOne(OBJECTID)).thenReturn(notification1);
      when(repo.getOne(OBJECTID2)).thenReturn(notification2);
      // when
      var res1 = underTest.canDo(method, USERID, OBJECTID);
      var res2 = underTest.canDo(method, USERID, OBJECTID2);
      // then
      assertThat(res1).isTrue();
      assertThat(res2).isFalse();
    }
  }

  @Nested
  class WhenAddingNotificationToSchedule {

    @Test
    void addNotificationToSchedule() {
      var request =
          ScheduledNotification.builder()
              .utilisateur(new User("test"))
              .instant(Instant.now().plusSeconds(6L))
              .build();
      // when(userRepo.getOne(any())).thenReturn(new User("test"));
      when(notificationRepo.save((any())))
          .thenAnswer(
              a -> {
                ScheduledNotification argument = a.getArgument(0);
                argument.setUtilisateur(new User("test"));
                return argument;
              });
      ScheduledNotification scheduledNotification = underTest.addNotificationToSchedule(request);
      assertThat(scheduledNotification).isNotNull();
    }
  }
}
