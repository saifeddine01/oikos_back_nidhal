package oikos.app.notifications;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Created by Mohamed Haamdi on 15/02/2021. */
public interface NotificationRepo extends JpaRepository<Notification, String> {
  @Query("select n from Notification n where n.utilisateur.id = :user and n.etat = 'NON_VU' order by n.createdAt desc ")
  Page<Notification> getUnreadNotifications(@Param("user") String userId, Pageable paging);

  @Query("select n from Notification n where n.utilisateur.id = :user and n.etat <> 'ARCHIVE' order by n.createdAt desc ")
  Page<Notification> getAllNotifications(@Param("user") String userId, Pageable paging);
}
