package oikos.app.seller;

import oikos.app.common.models.Address;
import oikos.app.users.Civility;
import oikos.app.users.User;
import oikos.app.users.UserRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

/** Created by Mohamed Haamdi on 05/05/2021. */
@DataJpaTest
class SellerRepoTest {

  @Autowired private SellerRepo underTest;
  @Autowired private UserRepo repo;

  @AfterEach
  void tearDown() {
    underTest.deleteAll();
    repo.deleteAll();
  }

  @Test
  void getByUserIDrDoesntExist() {
    // Given
    var userID = "NotInDB";
    // When
    var res = underTest.getByUserID(userID);
    // Then
    assertThat(res).isEmpty();
  }

  @Test
  void getByUserIDUserExists() {
    // Given
    var userID = "valid";
    var user = new User(userID);
    Seller sellerProfile = new Seller();
    sellerProfile.setCivility(Civility.MADAME);
    sellerProfile.setAddress(Address.builder().departmentIdentifier(0).build());
    sellerProfile.setUser(user);
    user.setSellerProfile(sellerProfile);
    repo.save(user);
    // When
    var res = underTest.getByUserID(userID);
    // Then
    assertThat(res).isPresent();
    assertThat(res.get().getCivility()).isEqualTo(Civility.MADAME);
  }
}
