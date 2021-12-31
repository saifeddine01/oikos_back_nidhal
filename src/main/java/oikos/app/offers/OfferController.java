package oikos.app.offers;

import lombok.AllArgsConstructor;
import oikos.app.common.utils.Monitor;
import oikos.app.security.CurrentUser;
import oikos.app.security.OikosUserDetails;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

import static oikos.app.common.utils.NanoIDGenerator.NANOID_SIZE;

/**
 * Created by Mohamed Haamdi on 11/05/2021.
 */
@RestController @AllArgsConstructor @Validated @Monitor public class OfferController {
  private final OfferService offerService;
  private final ModelMapper modelMapper;

  @PreAuthorize("@offerService.canDo('ADD_OFFER',#user.username,#user.username)")
  @PostMapping("/offers")
  public OfferResponse addOffer(@Validated @RequestBody CreateOfferRequest req,
    @CurrentUser OikosUserDetails user) {
    var data = offerService.addOffer(req, user.getUsername());
    return modelMapper.map(data, OfferResponse.class);
  }

  @PreAuthorize("@offerService.canDo('EDIT_OFFER',#user.username,#offerID)")
  @PutMapping("/offers/{offerID}")
  public OfferResponse editOffer(@Validated @RequestBody EditOfferRequest req,
    @CurrentUser OikosUserDetails user,
    @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String offerID) {
    var data = offerService.editOffer(req, offerID);
    return modelMapper.map(data, OfferResponse.class);
  }

  @PreAuthorize("@offerService.canDo('WITHDRAW_OFFER',#user.username,#offerID)")
  @GetMapping("/offers/{offerID}/withdraw")
  public OfferResponse withdrawOffer(@CurrentUser OikosUserDetails user,
    @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String offerID) {
    var data = offerService.withdrawOffer(offerID);
    return modelMapper.map(data, OfferResponse.class);
  }

  @PreAuthorize("@offerService.canDo('ACCEPT_OFFER',#user.username,#offerID)")
  @GetMapping("/offers/{offerID}/accept")
  public OfferResponse acceptOffer(@CurrentUser OikosUserDetails user,
    @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String offerID) {
    var data = offerService.acceptOffer(offerID);
    return modelMapper.map(data, OfferResponse.class);
  }

  @PreAuthorize("@offerService.canDo('REJECT_OFFER',#user.username,#offerID)")
  @GetMapping("/offers/{offerID}/reject")
  public OfferResponse rejectOffer(@CurrentUser OikosUserDetails user,
    @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String offerID) {
    var data = offerService.rejectOffer(offerID);
    return modelMapper.map(data, OfferResponse.class);
  }

  @PreAuthorize("@offerService.canDo('COUNTER_OFFER',#user.username,#offerID)")
  @PostMapping("/offers/{offerID}/counter") public OfferResponse counterOffer(
    @Validated @RequestBody AddCounterOfferRequest request,
    @CurrentUser OikosUserDetails user,
    @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String offerID) {
    var data = offerService.counterOffer(request, offerID);
    return modelMapper.map(data, OfferResponse.class);
  }

  @PreAuthorize("@offerService.canDo('GET_OFFER',#user.username,#offerID)")
  @GetMapping("/offers/{offerID}")
  public OfferResponse getOffer(@CurrentUser OikosUserDetails user,
    @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String offerID) {
    var data = offerService.getOffer(offerID);
    return modelMapper.map(data, OfferResponse.class);
  }

  @PreAuthorize("@offerService.canDo('LIST_ALL_OFFERS_FOR_PROPERTY',#user.username,#propID)")
  @GetMapping("/properties/{propID}/offers")
  public Page<OfferResponse> listFeedbacksForProperty(
    @CurrentUser OikosUserDetails user,
    @RequestParam(defaultValue = "0") @Min(0) int page,
    @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size,
    @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String propID) {
    Pageable paging = PageRequest.of(page, size);
    var res = offerService.listAllOffersForProperty(propID, paging);
    return res.map(offer -> modelMapper.map(offer, OfferResponse.class));
  }

  @PreAuthorize("@offerService.canDo('LIST_ALL_OFFERS_FOR_USER',#user.username,#user.username)")
  @GetMapping("/offers/me") public Page<OfferResponse> listFeedbacksForUser(
    @CurrentUser OikosUserDetails user,
    @RequestParam(defaultValue = "0") @Min(0) int page,
    @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
    Pageable paging = PageRequest.of(page, size);
    var res = offerService.listAllOffersForUser(user.getUsername(), paging);
    return res.map(offer -> modelMapper.map(offer, OfferResponse.class));
  }
}
