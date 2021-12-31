package oikos.app.offers;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import oikos.app.common.exceptions.BaseException;
import oikos.app.common.exceptions.EntityNotFoundException;
import oikos.app.common.models.BienVendre;
import oikos.app.common.repos.BienaVendreRepo;
import oikos.app.common.utils.Authorizable;
import oikos.app.notifications.NotificationService;
import oikos.app.users.User;
import oikos.app.users.UserRepo;

/**
 * Created by Mohamed Haamdi on 11/05/2021.
 */
@Service @RequiredArgsConstructor @Slf4j @Transactional
public class OfferService implements Authorizable<OfferService.OfferMethods> {
  private static final String OFFER_ENDPOINT = "offers/";
  private final OfferRepo offerRepo;
  private final UserRepo userRepo;
  private final BienaVendreRepo propRepo;
  private final NotificationService notificationService;
  private final ModelMapper modelMapper;


  @ToString enum OfferMethods {
    ADD_OFFER(Names.ADD_OFFER), EDIT_OFFER(Names.EDIT_OFFER), WITHDRAW_OFFER(
      Names.WITHDRAW_OFFER), ACCEPT_OFFER(Names.ACCEPT_OFFER), REJECT_OFFER(
      Names.REJECT_OFFER), COUNTER_OFFER(
      Names.COUNTER_OFFER), LIST_ALL_OFFERS_FOR_USER(
      Names.LIST_ALL_OFFERS_FOR_USER), LIST_ALL_OFFERS_FOR_PROPERTY(
      Names.LIST_ALL_OFFERS_FOR_PROPERTY), GET_OFFER(Names.GET_OFFER);
    private final String label;

    OfferMethods(String label) {
      this.label = label;
    }

    public static class Names {
      public static final String ADD_OFFER = "ADD_OFFER";
      public static final String EDIT_OFFER = "EDIT_OFFER";
      public static final String ACCEPT_OFFER = "ACCEPT_OFFER";
      public static final String WITHDRAW_OFFER = "WITHDRAW_OFFER";
      public static final String REJECT_OFFER = "REJECT_OFFER";
      public static final String COUNTER_OFFER = "COUNTER_OFFER";
      public static final String LIST_ALL_OFFERS_FOR_USER =
        "LIST_ALL_OFFERS_FOR_USER";
      public static final String LIST_ALL_OFFERS_FOR_PROPERTY =
        "LIST_ALL_OFFERS_FOR_PROPERTY";
      public static final String GET_OFFER = "GET_OFFER";

      private Names() {
      }
    }
  }

  public Offer getOffer(String offerID) {
    log.info("Getting the info for the offer {}", offerID);
    return offerRepo.findById(offerID)
      .orElseThrow(() -> new EntityNotFoundException(Offer.class, offerID));
  }

  public Offer addOffer(CreateOfferRequest req, String buyerID) {
    log.info("Adding a new offer from {} on property {}", buyerID,
      req.getPropertyID());
    if (!propRepo.existsById(req.getPropertyID())) {
      throw new EntityNotFoundException(BienVendre.class, req.getPropertyID());
    }
    final var saved = offerRepo.save(Offer.builder().status(OfferStatus.PENDING)
      .recipient(
        userRepo.getOne(propRepo.getOne(req.getPropertyID()).getUserId().getId()))
      .endsAt(req.getEndsAt()).sender(userRepo.getOne(buyerID))
      .amount(req.getAmount()).property(propRepo.getOne(req.getPropertyID()))
      .build());
    notificationService.addNotification(
      propRepo.getOne(req.getPropertyID()).getUserId().getId(),
      "One of your properties recived an offer.",
      OFFER_ENDPOINT + saved.getId());
    return saved;
  }

  public Offer editOffer(EditOfferRequest req, String offerID) {
    var offer = validateOffer("Editing offer {}", offerID,
      "The offer you're trying to edit is not pending therefore cannot be changed");
    modelMapper.map(req, offer);
    offer = offerRepo.save(offer);
    notificationService.addNotification(offer.getRecipient().getId(),
        "One of the offers you received has been modified.",
        OFFER_ENDPOINT + offer.getId());
    return offer;
  }

  public Offer withdrawOffer(String offerID) {
    var offer = validateOffer("Withdrawing offer {}", offerID,
      "The offer you're trying to withdraw is not pending therefore cannot be changed");
    changeOffer(offer, OfferStatus.WITHDRAWN, offer.getRecipient(),
      "One of the offers you received has been withdrawn.");
    return offer;
  }

  public Offer acceptOffer(String offerID) {
    var offer = validateOffer("Accepting offer {}", offerID,
      "The offer you're trying to accept is not pending therefore cannot be changed");
    changeOffer(offer, OfferStatus.ACCEPTED, offer.getSender(),
      "One of the offers you created has been accepted.");
    return offer;
  }

  public Offer rejectOffer(String offerID) {
    var offer = validateOffer("Rejecting offer {}", offerID,
      "The offer you're trying to reject is not pending therefore cannot be changed");
    changeOffer(offer, OfferStatus.REJECTED, offer.getSender(),
      "One of the offers you created has been rejected.");
    return offer;
  }

  public Offer counterOffer(AddCounterOfferRequest req,
    String previousOfferID) {
    var prevOffer =
      validateOffer("Adding counter offer for {}.", previousOfferID,
        "The offer you're trying to create a counter offer for is not pending therefore that cannot be done");
    changeOffer(prevOffer, OfferStatus.COUNTER_OFFERED, prevOffer.getSender(),
      "One of the offers you created has been received a counter offer.");
    var newOffer = Offer.builder().recipient(prevOffer.getSender())
      .sender(prevOffer.getRecipient()).property(prevOffer.getProperty()).previousOffer(prevOffer)
      .amount(req.getAmount()).endsAt(req.getEndsAt())
      .status(OfferStatus.PENDING).build();
    return offerRepo.save(newOffer);
  }

  public Page<Offer> listAllOffersForUser(String username, Pageable paging) {
    log.info("Getting page {} of offers for user {}", paging.getPageNumber(),
      username);
    return offerRepo.listAllOffersForUser(username, paging);
  }

  public Page<Offer> listAllOffersForProperty(String propID, Pageable paging) {
    log
      .info("Getting page {} of offers for property {}", paging.getPageNumber(),
        propID);
    return offerRepo.listAllOffersForProperty(propID, paging);
  }

  @Override public boolean canDo(OfferMethods methodName, String userID,
    String objectID) {
    try {
      return switch (methodName) {
        case ADD_OFFER -> userRepo.getOne(userID).getBuyerProfile()
          .isValidated();
        case EDIT_OFFER, WITHDRAW_OFFER -> offerRepo.getOne(objectID)
          .getSender().getId().equals(userID);
        case ACCEPT_OFFER, REJECT_OFFER, COUNTER_OFFER -> offerRepo
          .getOne(objectID).getRecipient().getId().equals(userID);
        case LIST_ALL_OFFERS_FOR_PROPERTY -> propRepo.getOne(objectID)
          .getUserId().getId().equals(userID);
        case GET_OFFER ->
          offerRepo.getOne(objectID).getRecipient().getId().equals(userID)
            || offerRepo.getOne(objectID).getSender().getId().equals(userID);
        case LIST_ALL_OFFERS_FOR_USER -> true;
      };
    } catch (javax.persistence.EntityNotFoundException e) {
      throw new EntityNotFoundException(Offer.class, objectID);
    }
  }

  //region ChangeOfferStatus internals
  private Offer validateOffer(String logMessage, String offerID,
    String exceptionMessage) {
    log.info(logMessage, offerID);
    if (!offerRepo.existsById(offerID)) {
      throw new EntityNotFoundException(Offer.class, offerID);
    }
    var offer = offerRepo.getOne(offerID);
    if (offer.getStatus() != OfferStatus.PENDING) {
      throw new BaseException(exceptionMessage);
    }
    return offer;
  }

  private void changeOffer(Offer offer, OfferStatus status,
    User notificationReceiver, String notificationContent) {
    offer.setStatus(status);
    offer = offerRepo.save(offer);
    notificationService.addNotification(notificationReceiver.getId(),
        notificationContent, OFFER_ENDPOINT + offer.getId());
  }
  //endregion
}
