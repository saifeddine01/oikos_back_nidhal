package oikos.app.ads;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Random;

import oikos.app.common.exceptions.EntityNotFoundException;
import oikos.app.common.models.BienVendre;
import oikos.app.common.repos.BienaVendreRepo;

@RequiredArgsConstructor
@Transactional
public class MockAdService implements AdService {
  private final AdRepository adRepo;
  private final BienaVendreRepo propRepo;
  private final Random ran = new Random();

  @Override
  public void deleteAd(String adID) {
    var ad = adRepo.findById(adID).orElseThrow(() -> new EntityNotFoundException(Ad.class, adID));
    adRepo.delete(ad);
  }

  @Override
  public Page<Ad> getAdsForUser(String userID, Pageable paging) {
    return adRepo.getAdsForUser(userID, paging);
  }

  @Override
  public void createAds(CreateAdRequest req) {
    var prop =
        propRepo
            .findById(req.getPropID())
            .orElseThrow(() -> new EntityNotFoundException(BienVendre.class, req.getPropID()));
    if (req.getPlatforms().contains(AdPlatform.FacebookMarketplace)) {
      adRepo.save(
          Ad.builder()
              .url("facebookMock")
              .views(100 + ran.nextInt(2000 - 100 + 1))
              .prop(prop)
              .propOwner(prop.getUserId())
              .adPlatform(AdPlatform.FacebookMarketplace)
              .build());
    }
    if (req.getPlatforms().contains(AdPlatform.LeBonCoin)) {
      adRepo.save(
          Ad.builder()
              .url("leBonCoinMock")
              .views(100 + ran.nextInt(2000 - 100 + 1))
              .prop(prop)
              .propOwner(prop.getUserId())
              .adPlatform(AdPlatform.LeBonCoin)
              .build());
    }
    if (req.getPlatforms().contains(AdPlatform.ParuVendu)) {
      adRepo.save(
          Ad.builder()
              .url("ParuVenduMock")
              .views(100 + ran.nextInt(2000 - 100 + 1))
              .prop(prop)
              .propOwner(prop.getUserId())
              .adPlatform(AdPlatform.ParuVendu)
              .build());
    }
  }

  @Override
  public void updateViewsTask() {
    Pageable pageRequest = PageRequest.of(0, 20);
    Page<Ad> onePage = adRepo.findAll(pageRequest);

    while (!onePage.isLast()) {
      pageRequest = pageRequest.next();

      onePage.forEach(
          it -> {
            it.setViews(100 + ran.nextInt(2000 - 100 + 1));
            adRepo.save(it);
          });

      onePage = adRepo.findAll(pageRequest);
    }
  }

  @Override public AdStats getAdStats(String userID) {
    var stats = AdStats.builder().viewsByPlatform(new LinkedHashMap<>()).totalViews(adRepo.getTotalViewsForUser(userID)).build();
    for(AdPlatform adPlatform : AdPlatform.values()){
      stats.getViewsByPlatform().put(adPlatform,adRepo.getTotalViewsForUserByAdPlatform(userID,adPlatform));
    }
    return stats;
  }

  @Override
  public boolean canDo(AdMethods methodName, String userID, String objectID) {
    try {
      return switch (methodName){
        case CREATE_ADS -> propRepo.getOne(objectID).getUserId().getId().equals(userID);
        case GET_ADS_FOR_USER,GET_AD_STATS -> true;
        case DELETE_AD -> adRepo.getOne(objectID).getPropOwner().getId().equals(userID);
      };
    }  catch (javax.persistence.EntityNotFoundException e) {
      throw new EntityNotFoundException(Ad.class,objectID);
    }
  }
}
