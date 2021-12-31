package oikos.app.appointementfeedback;

import oikos.app.common.models.BienVendre;
import oikos.app.common.models.Location;
import oikos.app.common.models.PiecesOfProperty;
import oikos.app.common.models.PropExport;
import oikos.app.common.models.PropertyAddress;
import oikos.app.common.models.PropertyLocation;
import oikos.app.common.models.PropertyStanding;
import oikos.app.common.models.PropertyVue;
import oikos.app.common.repos.BienaVendreRepo;
import oikos.app.users.User;
import oikos.app.users.UserRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;

/** Created by Mohamed Haamdi on 07/05/2021. */
@DataJpaTest
class AppointmentAppointementFeedbackRepoTest {
  Pageable paging = PageRequest.of(0, 10);
  @Autowired private AppointementFeedbackRepo underTest;
  @Autowired private BienaVendreRepo propRepo;
  @Autowired private UserRepo userRepo;

  @Test
  void getFeedbackByPropID() {
    // Given
    final var propID = propRepo.save(buildProp("propID"));
    var test1 = AppointmentFeedback.builder().property(propID).build();
    final var propID2 = propRepo.save(buildProp("propID2"));
    var test2 = AppointmentFeedback.builder().property(propID2).build();
    underTest.save(test1);
    underTest.save(test2);
    // When
    var res = underTest.getFeedbackByPropID("propID", paging);
    // Then
    assertThat(res).hasSize(1);
  }

  @Test
  void getFeedbackByReviewerID() {
    // Given
    var test1 =
        underTest.save(
            AppointmentFeedback.builder().reviewer(userRepo.save(new User("userID"))).build());
    var test2 =
        underTest.save(
            AppointmentFeedback.builder().reviewer(userRepo.save(new User("userID2"))).build());
    // When
    var res = underTest.getFeedbackByReviewerID("userID", paging);
    // Then
    assertThat(res).hasSize(1);
  }

  @Test
  void getAllFeedbackByOwnerID() {
    // Given
    var u = userRepo.save(new User("userID"));
    var u2 = userRepo.save(new User("userID2"));
    var u3 = userRepo.save(new User("userID3"));
    final var propID = propRepo.save(buildProp("propID", u));
    var test1 = underTest.save(AppointmentFeedback.builder().property(propID).build());
    final var propID2 = propRepo.save(buildProp("propID2", u));
    var test2 = underTest.save(AppointmentFeedback.builder().property(propID2).build());
    final var propID3 = propRepo.save(buildProp("propID3", u2));
    var test3 = underTest.save(AppointmentFeedback.builder().property(propID3).build());
    // When
    var res1 = underTest.getAllFeedbackByOwnerID(u.getId(), paging);
    var res2 = underTest.getAllFeedbackByOwnerID(u2.getId(), paging);
    var res3 = underTest.getAllFeedbackByOwnerID(u3.getId(), paging);
    // Then
    assertThat(res1).hasSize(2);
    assertThat(res2).hasSize(1);
    assertThat(res3).isEmpty();
  }

  @Test
  void existsByAppointmentIdMethodExists() {
    assertThat(underTest.existsByAppointmentId("any")).isFalse();
  }

  @Test
  void findFeedbackByAppointmentIdMethodExists() {
    assertThat(underTest.findFeedbackByAppointmentId("any")).isEmpty();
  }

  private BienVendre buildProp(String id) {
    return BienVendre.builder()
        .address(PropertyAddress.builder().city("").stateFull("").street("").zipCode("").build())
        .piecesOfProperty(PiecesOfProperty.builder().build())
        .propLocation(PropertyLocation.Bruyant)
        .propStanding(PropertyStanding.Standard)
        .typeofprop("type")
        .vueProp(PropertyVue.Degagee)
        .propExport(
            PropExport.builder().isEst(true).isNord(false).isOuest(false).isSud(false).build())
        .location(Location.builder().latitude(0.0).longitude(0.0).build())
        .id(id)
        .build();
  }

  private BienVendre buildProp(String id, User userID) {
    final var bienVendre = buildProp(id);
    bienVendre.setUserId(userID);
    return bienVendre;
  }
}
