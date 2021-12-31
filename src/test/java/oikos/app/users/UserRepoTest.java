package oikos.app.users;

import oikos.app.common.models.Address;
import oikos.app.seller.Seller;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by Mohamed Haamdi on 05/05/2021.
 */
@DataJpaTest class UserRepoTest {

  private final Pageable paging = PageRequest.of(0, 10);
  @Autowired private UserRepo underTest;

  @AfterEach void tearDown() {
    underTest.deleteAll();
  }

  @Test void findByEmailOrPhoneNumber() {
    // Given
    var u1 = User.builder().phoneNumber("phone").build();
    var u2 = User.builder().email("email").build();
    u1 = underTest.save(u1);
    u2 = underTest.save(u2);
    // When
    var res = underTest.findIDByEmailOrPhoneNumber("phone");
    var res2 = underTest.findIDByEmailOrPhoneNumber("email");
    var res3 = underTest.findIDByEmailOrPhoneNumber("none");
    // Then
    assertThat(res).isPresent().isEqualTo(Optional.of(u1.getId()));
    assertThat(res2).isPresent().isEqualTo(Optional.of(u2.getId()));
    assertThat(res3).isEmpty();
  }

  @Test void existsByPhoneNumber() {
    // Given
    final var u1 = User.builder().phoneNumber("phone").build();
    underTest.save(u1);
    // When
    var res = underTest.existsByPhoneNumber("phone");
    var res2 = underTest.existsByPhoneNumber("none");
    // Then
    assertThat(res).isTrue();
    assertThat(res2).isFalse();
  }

  @Test void existsByEmailIgnoreCase() {
    // Given
    final var u1 = User.builder().email("email").build();
    underTest.save(u1);
    // When
    var res = underTest.existsByEmailIgnoreCase("email");
    var res2 = underTest.existsByEmailIgnoreCase("EMAIL");
    var res3 = underTest.existsByEmailIgnoreCase("none");
    // Then
    assertThat(res).isTrue();
    assertThat(res2).isTrue();
    assertThat(res3).isFalse();
  }

  @Test void getAllSellers() {
    // Given
    var userID = "valid";
    var user = new User(userID);
    Seller sellerProfile = new Seller();
    sellerProfile.setCivility(Civility.MADAME);
    sellerProfile.setAddress(Address.builder().departmentIdentifier(0).build());
    sellerProfile.setUser(user);
    user.setSellerProfile(sellerProfile);
    underTest.save(user);
    var user2 = new User(userID + "2");
    underTest.save(user2);
    // When
    final var res = underTest.getAllSellers(paging);
    // Then
    assertThat(res).hasSize(1);
    assertThat(res.getContent().get(0).getId()).isEqualTo(user.getId());
  }



}
