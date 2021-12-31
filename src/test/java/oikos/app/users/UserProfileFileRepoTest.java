package oikos.app.users;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by Mohamed Haamdi on 04/06/2021.
 */
@DataJpaTest class UserProfileFileRepoTest {
  @Autowired UserProfileFileRepo underTest;
  @Autowired UserRepo userRepo;

  @BeforeEach void setUp() {
    underTest.deleteAll();
    userRepo.deleteAll();
  }

  @Test void getProfilePictureForUser() {
    //given
    var user = new User("id");
    var userProfilePicture = UserProfileFile.builder().id("profileID").user(user).build();
    user.setUserProfileFile(userProfilePicture);
    userRepo.save(user);
    underTest.save(userProfilePicture);
    //when
    var res1 = underTest.getProfilePictureForUser("id");
    var res2 = underTest.getProfilePictureForUser("id2");
    //then
    assertThat(res1).isPresent();
    assertThat(res1.get().getId()).isEqualTo("profileID");
    assertThat(res2).isEmpty();
  }
}
