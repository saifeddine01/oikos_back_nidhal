package oikos.app.buyer;

import oikos.app.users.User;
import oikos.app.users.UserRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

/** Created by Mohamed Haamdi on 05/05/2021. */
@DataJpaTest
class BuyerRepoTest {
  @Autowired private BuyerRepo underTest;
  @Autowired private UserRepo repo;

  @AfterEach
  void tearDown() {
    underTest.deleteAll();
    repo.deleteAll();
  }

  @Test
  void getByUserIDUserDoesntExist() {
    // Given
    var userID = "NotInDB";
    // When
    var res = underTest.getByUserID(userID);
    // Then
    assertThat(res).isNull();
  }

  @Test
  void getByUserIDUserExists() {
    // Given
    var userID = "valid";
    var user = new User(userID);
    Buyer buyerProfile = new Buyer();
    buyerProfile.setValidated(true);
    buyerProfile.setUser(user);
    user.setBuyerProfile(buyerProfile);
    repo.save(user);
    // When
    var res = underTest.getByUserID(userID);
    // Then
    assertThat(res.isValidated()).isTrue();
  }
}
