package oikos.app.offers;

import oikos.app.buyer.Buyer;
import oikos.app.common.exceptions.EntityNotFoundException;
import oikos.app.common.models.BienVendre;
import oikos.app.common.repos.BienaVendreRepo;
import oikos.app.notifications.NotificationService;
import oikos.app.users.User;
import oikos.app.users.UserRepo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Created by Mohamed Haamdi on 11/05/2021. */
@ExtendWith(MockitoExtension.class)
class OfferServiceTest {
  @Mock OfferRepo offerRepo;
  @Mock UserRepo userRepo;
  @Mock BienaVendreRepo propRepo;
  @Mock NotificationService notificationService;
  @Mock ModelMapper modelMapper;
  @InjectMocks OfferService underTest;

  @Test
  void listAllOffersForUser() {
    underTest.listAllOffersForUser("id", PageRequest.of(0, 10));
    verify(offerRepo).listAllOffersForUser("id", PageRequest.of(0, 10));
  }

  @Test
  void listAllOffersForProperty() {
    underTest.listAllOffersForProperty("id", PageRequest.of(0, 10));
    verify(offerRepo).listAllOffersForProperty("id", PageRequest.of(0, 10));
  }

  @Nested
  class addOffer {
    CreateOfferRequest dto = new CreateOfferRequest("propID", LocalDate.MAX, BigDecimal.valueOf(5));

    @Test
    void addOfferPropNotFound() {
      // Given
      when(propRepo.existsById("propID")).thenReturn(false);
      // Then
      assertThatThrownBy(() -> underTest.addOffer(dto, "any"))
          .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void addOfferSuccessful() {
      // Given
      var buyerID = "buyerID";
      when(propRepo.existsById("propID")).thenReturn(true);
      when(userRepo.getOne(any())).thenAnswer(a -> new User(a.getArgument(0)));
      when(offerRepo.save(any(Offer.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());
      when(propRepo.getOne(any()))
          .thenReturn(BienVendre.builder().userId(new User("sellerID")).build());
      // When
      Offer res = underTest.addOffer(dto, buyerID);
      // Then
      assertThat(res.getStatus()).isEqualTo(OfferStatus.PENDING);
      assertThat(res.getSender().getId()).isEqualTo(buyerID);
      verify(notificationService)
          .addNotification(any(String.class), any(String.class), any(String.class));
    }
  }

  @Nested
  class editOffer {
    final EditOfferRequest dto =
        new EditOfferRequest(LocalDate.now().plusDays(15), BigDecimal.valueOf(5));
    final String offerID = "offerID";

    @Test
    void editOfferNotFound() {
      // Given
      when(offerRepo.existsById(offerID)).thenReturn(false);
      // Then
      assertThatThrownBy(() -> underTest.editOffer(dto, offerID))
          .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void editOfferNotPending() {
      // Given
      when(offerRepo.existsById(offerID)).thenReturn(true);
      when(offerRepo.getOne(offerID))
          .thenReturn(Offer.builder().status(OfferStatus.EXPIRED).build());
      // Then
      assertThatThrownBy(() -> underTest.editOffer(dto, offerID))
          .hasMessageContaining("is not pending");
    }

    @Test
    void editOfferSuccessful() {
      // Given
      when(offerRepo.existsById(offerID)).thenReturn(true);
      when(offerRepo.getOne(offerID))
          .thenReturn(
              Offer.builder()
                  .id(offerID)
                  .recipient(new User("sellerID"))
                  .amount(BigDecimal.valueOf(1))
                  .endsAt(LocalDate.MAX)
                  .status(OfferStatus.PENDING)
                  .build());
      when(offerRepo.save(any())).thenAnswer(AdditionalAnswers.returnsFirstArg());
      // When
      var res = underTest.editOffer(dto, offerID);
      // Then
      assertThat(res).isNotNull();
      verify(modelMapper).map(any(EditOfferRequest.class), any(Offer.class));
      verify(notificationService)
          .addNotification(any(String.class), any(String.class), any(String.class));
    }
  }

  @Nested
  class acceptOffer {
    final String offerID = "offerID";

    @Test
    void acceptOfferNotFound() {
      // Given
      when(offerRepo.existsById(offerID)).thenReturn(false);
      // Then
      assertThatThrownBy(() -> underTest.acceptOffer(offerID))
          .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void acceptOfferNotPending() {
      // Given
      when(offerRepo.existsById(offerID)).thenReturn(true);
      when(offerRepo.getOne(offerID))
          .thenReturn(Offer.builder().status(OfferStatus.EXPIRED).build());
      // Then
      assertThatThrownBy(() -> underTest.acceptOffer(offerID))
          .hasMessageContaining("is not pending");
    }

    @Test
    void acceptOfferSuccessful() {
      // Given
      when(offerRepo.existsById(offerID)).thenReturn(true);
      when(offerRepo.getOne(offerID))
          .thenReturn(
              Offer.builder()
                  .id(offerID)
                  .recipient(new User("sellerID"))
                  .sender(new User("buyerID"))
                  .amount(BigDecimal.valueOf(1))
                  .endsAt(LocalDate.MAX)
                  .status(OfferStatus.PENDING)
                  .build());
      when(offerRepo.save(any())).thenAnswer(AdditionalAnswers.returnsFirstArg());
      // When
      var res = underTest.acceptOffer(offerID);
      // Then
      assertThat(res).isNotNull();
      assertThat(res.getId()).isEqualTo(offerID);
      assertThat(res.getStatus()).isEqualTo(OfferStatus.ACCEPTED);
      verify(notificationService)
          .addNotification(any(String.class), any(String.class), any(String.class));
    }
  }

  @Nested
  class getOffer {
    @Test
    void getOfferNotFound() {
      // given
      when(offerRepo.findById("id")).thenReturn(Optional.empty());
      // then
      assertThatThrownBy(() -> underTest.getOffer("id"))
          .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getOfferSuccess() {
      // given
      final var id = "id";
      var expected = Offer.builder().id(id).build();
      when(offerRepo.findById(id)).thenReturn(Optional.of(expected));
      // when
      var res = underTest.getOffer(id);
      // then
      assertThat(res).isEqualTo(expected);
    }
  }

  @Nested
  class rejectOffer {
    final String offerID = "offerID";

    @Test
    void rejectOfferrNotFound() {
      // Given
      when(offerRepo.existsById(offerID)).thenReturn(false);
      // Then
      assertThatThrownBy(() -> underTest.rejectOffer(offerID))
          .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void rejectOfferNotPending() {
      // Given
      when(offerRepo.existsById(offerID)).thenReturn(true);
      when(offerRepo.getOne(offerID))
          .thenReturn(Offer.builder().status(OfferStatus.EXPIRED).build());
      // Then
      assertThatThrownBy(() -> underTest.rejectOffer(offerID))
          .hasMessageContaining("is not pending");
    }

    @Test
    void rejectOfferSuccessful() {
      // Given
      when(offerRepo.existsById(offerID)).thenReturn(true);
      when(offerRepo.getOne(offerID))
          .thenReturn(
              Offer.builder()
                  .id(offerID)
                  .recipient(new User("sellerID"))
                  .sender(new User("buyerID"))
                  .amount(BigDecimal.valueOf(1))
                  .endsAt(LocalDate.MAX)
                  .status(OfferStatus.PENDING)
                  .build());
      when(offerRepo.save(any())).thenAnswer(AdditionalAnswers.returnsFirstArg());
      // When
      var res = underTest.rejectOffer(offerID);
      // Then
      assertThat(res).isNotNull();
      assertThat(res.getId()).isEqualTo(offerID);
      assertThat(res.getStatus()).isEqualTo(OfferStatus.REJECTED);
      verify(notificationService)
          .addNotification(any(String.class), any(String.class), any(String.class));
    }
  }

  @Nested
  class withdrawOffer {
    final String offerID = "offerID";

    @Test
    void withdrawOfferNotFound() {
      // Given
      when(offerRepo.existsById(offerID)).thenReturn(false);
      // Then
      assertThatThrownBy(() -> underTest.withdrawOffer(offerID))
          .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void withdrawOfferNotPending() {
      // Given
      when(offerRepo.existsById(offerID)).thenReturn(true);
      when(offerRepo.getOne(offerID))
          .thenReturn(Offer.builder().status(OfferStatus.EXPIRED).build());
      // Then
      assertThatThrownBy(() -> underTest.withdrawOffer(offerID))
          .hasMessageContaining("is not pending");
    }

    @Test
    void withdrawOfferSuccessful() {
      // Given
      when(offerRepo.existsById(offerID)).thenReturn(true);
      when(offerRepo.getOne(offerID))
          .thenReturn(
              Offer.builder()
                  .id(offerID)
                  .recipient(new User("sellerID"))
                  .sender(new User("buyerID"))
                  .amount(BigDecimal.valueOf(1))
                  .endsAt(LocalDate.MAX)
                  .status(OfferStatus.PENDING)
                  .build());
      when(offerRepo.save(any())).thenAnswer(AdditionalAnswers.returnsFirstArg());
      // When
      var res = underTest.withdrawOffer(offerID);
      // Then
      assertThat(res).isNotNull();
      assertThat(res.getId()).isEqualTo(offerID);
      assertThat(res.getStatus()).isEqualTo(OfferStatus.WITHDRAWN);
      verify(notificationService)
          .addNotification(any(String.class), any(String.class), any(String.class));
    }
  }

  @Nested
  class counterOffer {
    String previousOfferID = "prevID";
    AddCounterOfferRequest request =
        new AddCounterOfferRequest(LocalDate.MAX, BigDecimal.valueOf(1));

    @Test
    void counterOfferNotFound() {
      // Given
      when(offerRepo.existsById(previousOfferID)).thenReturn(false);
      // Then
      assertThatThrownBy(() -> underTest.counterOffer(request, previousOfferID))
          .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void counterOfferNotPending() {
      // Given
      when(offerRepo.existsById(previousOfferID)).thenReturn(true);
      when(offerRepo.getOne(previousOfferID))
          .thenReturn(Offer.builder().status(OfferStatus.EXPIRED).build());
      // Then
      assertThatThrownBy(() -> underTest.counterOffer(request, previousOfferID))
          .hasMessageContaining("is not pending");
    }

    @Test
    void counterOfferSuccessful() {
      // Given
      when(offerRepo.existsById(previousOfferID)).thenReturn(true);
      when(offerRepo.getOne(previousOfferID))
          .thenReturn(
              Offer.builder()
                  .id(previousOfferID)
                  .recipient(new User("sellerID"))
                  .sender(new User("buyerID"))
                  .amount(BigDecimal.valueOf(1))
                  .endsAt(LocalDate.MAX)
                  .status(OfferStatus.PENDING)
                  .build());
      when(offerRepo.save(any())).thenAnswer(AdditionalAnswers.returnsFirstArg());
      // When
      var res = underTest.counterOffer(request, previousOfferID);
      // Then
      assertThat(res).isNotNull();
      assertThat(res.getPreviousOffer().getId()).isEqualTo(previousOfferID);
      assertThat(res.getSender().getId())
          .isEqualTo(offerRepo.getOne(previousOfferID).getRecipient().getId());
      assertThat(res.getRecipient().getId())
          .isEqualTo(offerRepo.getOne(previousOfferID).getSender().getId());
      assertThat(offerRepo.getOne(previousOfferID).getStatus())
          .isEqualTo(OfferStatus.COUNTER_OFFERED);
      assertThat(res.getStatus()).isEqualTo(OfferStatus.PENDING);
      verify(notificationService)
          .addNotification(any(String.class), any(String.class), any(String.class));
    }
  }

  @Nested
  class canDo {
    final String invalidID = "invalidID";

    @Test
    void canAddOffer() {
      // Given
      final var nonValidatedBuyerID = "nonValidatedBuyerID";
      final var validatedBuyerID = "validatedBuyerID";
      when(userRepo.getOne(invalidID)).thenThrow(new javax.persistence.EntityNotFoundException());
      when(userRepo.getOne(validatedBuyerID))
          .thenReturn(
              User.builder()
                  .id(validatedBuyerID)
                  .buyerProfile(Buyer.builder().isValidated(true).build())
                  .build());
      when(userRepo.getOne(nonValidatedBuyerID))
          .thenReturn(
              User.builder()
                  .id(nonValidatedBuyerID)
                  .buyerProfile(Buyer.builder().isValidated(false).build())
                  .build());
      // When
      var shouldBeTrue =
          underTest.canDo(OfferService.OfferMethods.ADD_OFFER, validatedBuyerID, null);
      var shouldBeFalse =
          underTest.canDo(OfferService.OfferMethods.ADD_OFFER, nonValidatedBuyerID, null);
      // Then
      assertThatThrownBy(
              () -> underTest.canDo(OfferService.OfferMethods.ADD_OFFER, invalidID, null))
          .isInstanceOf(EntityNotFoundException.class);
      assertThat(shouldBeTrue).isTrue();
      assertThat(shouldBeFalse).isFalse();
    }

    @ParameterizedTest
    @EnumSource(
        value = OfferService.OfferMethods.class,
        names = {
          OfferService.OfferMethods.Names.EDIT_OFFER,
          OfferService.OfferMethods.Names.WITHDRAW_OFFER
        })
    void canEditAndWithdrawOffer(OfferService.OfferMethods method) {
      // Given
      when(offerRepo.getOne("offerID"))
          .thenReturn(Offer.builder().sender(new User("userID")).build());
      when(offerRepo.getOne("offerID2"))
          .thenReturn(Offer.builder().sender(new User("userID2")).build());
      // When
      var shouldBeTrue = underTest.canDo(method, "userID", "offerID");
      var shouldBeFalse = underTest.canDo(method, "userID", "offerID2");
      // Then
      assertThat(shouldBeFalse).isFalse();
      assertThat(shouldBeTrue).isTrue();
    }

    @ParameterizedTest
    @EnumSource(
        value = OfferService.OfferMethods.class,
        names = {
          OfferService.OfferMethods.Names.ACCEPT_OFFER,
          OfferService.OfferMethods.Names.REJECT_OFFER,
          OfferService.OfferMethods.Names.COUNTER_OFFER
        })
    void canAcceptRejectAndCounterOffer(OfferService.OfferMethods method) {
      // Given
      when(offerRepo.getOne("offerID"))
          .thenReturn(Offer.builder().recipient(new User("userID")).build());
      when(offerRepo.getOne("offerID2"))
          .thenReturn(Offer.builder().recipient(new User("userID2")).build());
      // When
      var shouldBeTrue = underTest.canDo(method, "userID", "offerID");
      var shouldBeFalse = underTest.canDo(method, "userID", "offerID2");
      // Then
      assertThat(shouldBeFalse).isFalse();
      assertThat(shouldBeTrue).isTrue();
    }

    @Test
    void canListAllOffersForProp() {
      // Given
      when(propRepo.getOne("propID"))
          .thenReturn(BienVendre.builder().userId(new User("userID")).id("propID").build());
      // When
      var shouldBeTrue =
          underTest.canDo(
              OfferService.OfferMethods.LIST_ALL_OFFERS_FOR_PROPERTY, "userID", "propID");
      var shouldBeFalse =
          underTest.canDo(
              OfferService.OfferMethods.LIST_ALL_OFFERS_FOR_PROPERTY, "userID2", "propID");
      // Then
      assertThat(shouldBeFalse).isFalse();
      assertThat(shouldBeTrue).isTrue();
    }

    @Test
    void canGetOffer() {
      // Given
      when(offerRepo.getOne("offerID"))
          .thenReturn(
              Offer.builder()
                  .recipient(new User("recipientID"))
                  .sender(new User("senderID"))
                  .build());
      // When
      var shouldBeTrue =
          underTest.canDo(OfferService.OfferMethods.GET_OFFER, "recipientID", "offerID");
      var shouldBeTrue2 =
          underTest.canDo(OfferService.OfferMethods.GET_OFFER, "senderID", "offerID");
      var shouldBeFalse =
          underTest.canDo(OfferService.OfferMethods.GET_OFFER, "anotherUser", "offerID");
      // Then
      assertThat(shouldBeTrue).isEqualTo(shouldBeTrue2).isTrue();
      assertThat(shouldBeFalse).isFalse();
    }

    @Test
    void canListAllOffersForUser() {
      var res = underTest.canDo(OfferService.OfferMethods.LIST_ALL_OFFERS_FOR_USER, "any", "any");
      assertThat(res).isTrue();
    }
  }
}
