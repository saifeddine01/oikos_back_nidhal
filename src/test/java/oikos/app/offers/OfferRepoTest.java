package oikos.app.offers;

import oikos.app.common.models.BienVendre;
import oikos.app.common.models.Location;
import oikos.app.common.models.PiecesOfProperty;
import oikos.app.common.models.PropExport;
import oikos.app.common.models.PropertyAddress;
import oikos.app.common.models.PropertyLocation;
import oikos.app.common.models.PropertyStanding;
import oikos.app.common.models.PropertyVue;
import oikos.app.common.models.TypeImmobilier;
import oikos.app.common.repos.BienaVendreRepo;
import oikos.app.users.User;
import oikos.app.users.UserRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by Mohamed Haamdi on 11/05/2021.
 */
@DataJpaTest class OfferRepoTest {
  Pageable paging = PageRequest.of(0, 10);
  BienVendre prop1;
  BienVendre prop2;
  User user1;
  User user2;
  User user3;
  @Autowired private BienaVendreRepo propRepo;
  @Autowired private UserRepo userRepo;
  @Autowired private OfferRepo underTest;

  @BeforeEach void setUp() {
    user1 = userRepo.save(new User("user1"));
    user2 = userRepo.save(new User("user2"));
    user3 = userRepo.save(new User("user3"));
    prop1 = propRepo.save(buildProp("prop1", user1));
    prop2 = propRepo.save(buildProp("prop2", user1));
  }

  @AfterEach void tearDown() {
    propRepo.deleteAll();
    userRepo.deleteAll();
    underTest.deleteAll();
  }

  @Test void listAllOffersForPropertySuccess() {
    // given
    var offer1 = underTest.save(
      Offer.builder().property(prop1).recipient(user1).sender(user2).build());
    var offer2 = underTest.save(
      Offer.builder().property(prop1).sender(user1).recipient(user2)
        .previousOffer(offer1).build());
    var offer3 = underTest.save(
      Offer.builder().property(prop2).recipient(user2).sender(user1).build());
    // when
    Page<Offer> res = underTest.listAllOffersForProperty(prop1.getId(), paging);
    // then
    assertThat(res).containsExactlyInAnyOrder(offer1, offer2)
      .doesNotContain(offer3);
  }

  @Test void listAllOffersForSeller() {
    // Given
    var offer1 = underTest.save(
      Offer.builder().property(prop1).sender(user1).recipient(user2).build());
    var offer2 = underTest.save(
      Offer.builder().property(prop1).sender(user2).recipient(user1).build());
    var offer3 = underTest.save(
      Offer.builder().property(prop2).sender(user1).recipient(user2).build());
    // When
    Page<Offer> res = underTest.listAllOffersForUser(user2.getId(), paging);
    // Then
    assertThat(res).containsExactlyInAnyOrder(offer1, offer3, offer2);
  }

  @Test void listAllOffersForBuyer() {
    // Given
    var offer1 = underTest.save(
      Offer.builder().property(prop1).sender(user1).recipient(user2).build());
    var offer2 = underTest.save(
      Offer.builder().property(prop1).sender(user2).recipient(user1).build());
    var offer3 = underTest.save(
      Offer.builder().property(prop2).sender(user1).recipient(user2).build());
    var offer4 = underTest.save(
      Offer.builder().property(prop2).sender(user1).recipient(user3).build());
    // When
    Page<Offer> res = underTest.listAllOffersForUser(user2.getId(), paging);
    // Then
    assertThat(res).containsExactlyInAnyOrder(offer2, offer1, offer3)
      .doesNotContain(offer4);
  }

  @Test void markAllExpiredOffersAsExpired() {
    // Given
    var offer1 = underTest.save(
      Offer.builder().endsAt(LocalDate.now().minusDays(5l))
        .status(OfferStatus.PENDING).build());
    var offer2 = underTest.save(
      Offer.builder().endsAt(LocalDate.now().plusDays(6l))
        .status(OfferStatus.PENDING).build());
    var offer3 = underTest.save(
      Offer.builder().endsAt(LocalDate.now().minusDays(1l))
        .status(OfferStatus.PENDING).build());
    var offer4 = underTest.save(
      Offer.builder().endsAt(LocalDate.now().minusDays(1l))
        .status(OfferStatus.ACCEPTED).build());
    var offer5 = underTest.save(
      Offer.builder().endsAt(LocalDate.now().plusDays(1l))
        .status(OfferStatus.ACCEPTED).build());
    var offer6 = underTest.save(
      Offer.builder().endsAt(LocalDate.now()).status(OfferStatus.PENDING)
        .build());
    // When
    var before = underTest.findAll();
    underTest.markAllExpiredOffersAsExpired(LocalDate.now());
    var res = underTest.findAll();
    // Then
    assertThat(before)
      .containsExactlyInAnyOrder(offer1, offer2, offer3, offer4, offer5,
        offer6);
    for (var item : res) {
      if (item.getStatus() == OfferStatus.PENDING)
        assertThat(item.getEndsAt()).isAfterOrEqualTo(LocalDate.now());
      if (item.getStatus() == OfferStatus.EXPIRED)
        assertThat(item.getEndsAt()).isBefore(LocalDate.now());
    }
    assertThat(offer5.getStatus()).isEqualTo(OfferStatus.ACCEPTED);
    assertThat(offer4.getStatus()).isEqualTo(OfferStatus.ACCEPTED);
  }

  // region save prop
  private BienVendre buildProp(String id) {
    return BienVendre.builder().address(
      PropertyAddress.builder().city("").stateFull("").street("").zipCode("")
        .build()).piecesOfProperty(PiecesOfProperty.builder().build())
      .propLocation(PropertyLocation.Bruyant)
      .propStanding(PropertyStanding.Standard)
      .typeofprop(TypeImmobilier.Appartement.name()).vueProp(PropertyVue.Degagee)
      .propExport(PropExport.builder().isEst(true).isNord(false).isOuest(false)
        .isSud(false).build())
      .location(Location.builder().latitude(0.0).longitude(0.0).build()).id(id)
      .build();
  }

  private BienVendre buildProp(String id, User userID) {
    final var bienVendre = buildProp(id);
    bienVendre.setUserId(userID);
    return bienVendre;
  }
  // endregion
}
