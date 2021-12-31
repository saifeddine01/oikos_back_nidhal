package oikos.app.ads;

import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import oikos.app.common.responses.DoneResponse;
import oikos.app.common.utils.Monitor;
import oikos.app.security.CurrentUser;
import oikos.app.security.OikosUserDetails;

@RestController
@AllArgsConstructor
@Validated
@Monitor
public class AdController {
  private final AdService service;
  private final ModelMapper modelMapper;

  @PreAuthorize("@adServerConfiguration.adService.canDo('CREATE_ADS',#user.username,#req.propID)")
  @PostMapping("/ads")
  public DoneResponse createAds(
      @RequestBody CreateAdRequest req, @CurrentUser OikosUserDetails user) {
    service.createAds(req);
    return new DoneResponse(
        "The ads for property " + req.getPropID() + " have been created succesfully.");
  }

  @PreAuthorize("@adServerConfiguration.adService.canDo('GET_ADS_FOR_USER',#user.username,null)")
  @GetMapping("/ads/me")
  public Page<AdResponse> getAdsForCurrentUser(
      @CurrentUser OikosUserDetails user,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
    Pageable paging = PageRequest.of(page, size);
    var data = service.getAdsForUser(user.getUsername(), paging);
    return data.map(item -> modelMapper.map(item, AdResponse.class));
  }

  @PreAuthorize("@adServerConfiguration.adService.canDo('DELETE_AD',#user.username,#adID)")
  @DeleteMapping("/ads/{adID}")
  public DoneResponse deleteAd(@CurrentUser OikosUserDetails user, @PathVariable String adID) {
    service.deleteAd(adID);
    return new DoneResponse("The ad " + adID + " have been deleted succesfully.");
  }

  @PreAuthorize("@adServerConfiguration.adService.canDo('GET_AD_STATS',#user.username,null)")
  @GetMapping("/ads/me/stats")
  public AdStats getAdStats(@CurrentUser OikosUserDetails user) {
    return service.getAdStats(user.getUsername());
  }
}
