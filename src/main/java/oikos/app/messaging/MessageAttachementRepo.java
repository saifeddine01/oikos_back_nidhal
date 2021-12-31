package oikos.app.messaging;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MessageAttachementRepo
  extends JpaRepository<MessageAttachement, String> {
  @Query("select m from MessageAttachement m where m.message.id = :messageId")
  Optional<MessageAttachement> getAttachementForMessage(
    @Param("messageId") String messageId);

}
