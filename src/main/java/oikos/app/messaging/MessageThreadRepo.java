package oikos.app.messaging;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/** Created by Mohamed Haamdi on 12/02/2021 */
public interface MessageThreadRepo extends JpaRepository<MessageThread, String> {
  @Query("SELECT t FROM MessageThread t WHERE (t.user1.id = :user and t.isUser1Deleted = false) or (t.user2.id = :user and t.isUser2Deleted = false) order by t.dateLastMessage desc")
  Page<MessageThread> findMessageThreadByUserID(@Param("user") String userID, Pageable paging);

  @Query("select t from MessageThread t where (t.user1.id = :user1 or t.user2.id = :user1) and (t.user1.id = :user2 or t.user2.id = :user2)")
  Optional<MessageThread> findMessageThreadBetweenUsersByID(
      @Param("user1") String userID1, @Param("user2") String userID2);
}
