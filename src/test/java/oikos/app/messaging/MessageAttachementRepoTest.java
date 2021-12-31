package oikos.app.messaging;

import oikos.app.users.User;
import oikos.app.users.UserProfileFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Mohamed Haamdi on 04/06/2021.
 */
@DataJpaTest
class MessageAttachementRepoTest {
  @Autowired MessageAttachementRepo underTest;
  @Autowired MessageRepo messageRepo;
  @BeforeEach void setUp() {
    underTest.deleteAll();
    messageRepo.deleteAll();
  }

  @Test void getAttachementForMessage() {
    var msg = Message.builder().id("msgID").build();
    var attachement = MessageAttachement.builder().id("atchID").message(msg).build();
    msg.setMessageAttachement(attachement);
    messageRepo.save(msg);
    underTest.save(attachement);
    //when
    var res1 = underTest.getAttachementForMessage("msgID");
    var res2 = underTest.getAttachementForMessage("msgID2");
    //then
    assertThat(res1).isPresent();
    assertThat(res1.get().getId()).isEqualTo("atchID");
    assertThat(res2).isEmpty();
  }
}
