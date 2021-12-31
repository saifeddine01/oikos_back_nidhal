package oikos.app.notifications;

import oikos.app.users.User;
import oikos.app.users.UserRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;

/** Created by Mohamed Haamdi on 05/05/2021. */
@DataJpaTest
class NotificationRepoTest {
  private final Pageable paging = PageRequest.of(0, 10);
  @Autowired private NotificationRepo underTest;
  @Autowired private UserRepo userRepo;

  @AfterEach
  void tearDown() {
    underTest.deleteAll();
  }

  @Test
  void getUnreadNotifications() {
    // Given
    final var userID = new User("userID");
    userRepo.save(userID);
    Notification build =
        Notification.builder().utilisateur(userID).etat(EtatNotification.NON_VU).build();
    Notification build2 =
        Notification.builder().utilisateur(userID).etat(EtatNotification.VU).build();
    Notification build3 =
        Notification.builder().utilisateur(userID).etat(EtatNotification.ARCHIVE).build();
    underTest.save(build);
    underTest.save(build2);
    underTest.save(build3);
    // When
    var res = underTest.getUnreadNotifications("userID", paging);
    // Then
    assertThat(res).hasSize(1);
  }

  @Test
  void getAllNotifications() {
    // Given
    final var userID = new User("userID");
    userRepo.save(userID);
    Notification build =
        Notification.builder().utilisateur(userID).etat(EtatNotification.NON_VU).build();
    Notification build2 =
        Notification.builder().utilisateur(userID).etat(EtatNotification.VU).build();
    Notification build3 =
        Notification.builder().utilisateur(userID).etat(EtatNotification.ARCHIVE).build();
    underTest.save(build);
    underTest.save(build2);
    underTest.save(build3);
    // When
    var res = underTest.getAllNotifications("userID", paging);
    // Then
    assertThat(res).hasSize(2);
  }
}
