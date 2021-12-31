package oikos.app.ads;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import oikos.app.common.utils.Authorizable;

public interface AdService extends Authorizable<AdMethods> {
  void deleteAd(String adID);

  Page<Ad> getAdsForUser(String userID, Pageable paging);

  void createAds(CreateAdRequest req);

  void updateViewsTask();

  AdStats getAdStats(String userID);
}
