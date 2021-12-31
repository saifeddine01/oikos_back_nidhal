package oikos.app.messaging;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/** Created by Mohamed Haamdi on 12/02/2021 */
public interface MessageRepo extends JpaRepository<Message, String> {

  /**
   * This method returns a list of undeleted messages by the specified user in the specified thread.
   *
   * @param threadId The id of the thread to list messages from
   * @param userId The user id of the request
   * @param pageable paging information
   * @return A list of messages in that thread that are not deleted by the user requesting them
   */
  @Query(
      value =
          "select m from Message m where m.thread.id = :thread and ((m.sender.id = :user and m.isSenderDeleted = false) or (m.recipient.id = :user and m.isRecipientDeleted = false)) order by m.createdAt desc")
  Page<Message> getMessagesInThread(
      @Param("thread") String threadId, @Param("user") String userId, Pageable pageable);

  @Query(
      value =
          "select m from Message m where m.id = :messageID and ((m.sender.id = :userID and m.isSenderDeleted = false) or (m.recipient.id = :userID and m.isRecipientDeleted = false))")
  Optional<Message> getMessageForUser(
      @Param("messageID") String messageID, @Param("userID") String userID);
}
