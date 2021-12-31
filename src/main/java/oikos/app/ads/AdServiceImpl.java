package oikos.app.ads;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;

import oikos.app.common.exceptions.EntityNotFoundException;
import oikos.app.common.models.BienVendre;
import oikos.app.common.repos.BienaVendreRepo;

@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdServiceImpl implements AdService {
  private final String serverPath;
  private final AdRepository adRepo;
  private final ModelMapper mapper;
  private final BienaVendreRepo propRepo;
  private final RestTemplate restTemplate = new RestTemplate();

  @Override
  public void createAds(CreateAdRequest req) {
    var prop =
        propRepo
            .findById(req.getPropID())
            .orElseThrow(() -> new EntityNotFoundException(BienVendre.class, req.getPropID()));
    if (req.getPlatforms().contains(AdPlatform.FacebookMarketplace)) {
      var url = createFacebookAd(prop, req);
      adRepo.save(
          Ad.builder()
              .url(url)
              .views(0)
              .prop(prop)
              .propOwner(prop.getUserId())
              .adPlatform(AdPlatform.FacebookMarketplace)
              .build());
    }
    if (req.getPlatforms().contains(AdPlatform.LeBonCoin)) {
      var url = createLeBonCoinAd(prop, req);
      adRepo.save(
          Ad.builder()
              .url(url)
              .views(0)
              .prop(prop)
              .propOwner(prop.getUserId())
              .adPlatform(AdPlatform.LeBonCoin)
              .build());
    }
    if (req.getPlatforms().contains(AdPlatform.ParuVendu)) {
      var url = createParuVenduAd(prop, req);
      adRepo.save(
          Ad.builder()
              .url(url)
              .views(0)
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
            var views =
                restTemplate.getForObject(
                    serverPath + "/views/" + it.getUrl(), AdServerViewsResponse.class);
            it.setViews(views.getViews());
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
  public Page<Ad> getAdsForUser(String userID, Pageable paging) {
    return adRepo.getAdsForUser(userID, paging);
  }

  @Override
  public void deleteAd(String adID) {
    var ad = adRepo.findById(adID).orElseThrow(() -> new EntityNotFoundException(Ad.class, adID));
    restTemplate.delete(serverPath + ad.getUrl());
    adRepo.delete(ad);
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

  private String createParuVenduAd(BienVendre prop, CreateAdRequest req) {
    var ad = mapper.map(prop, AdParuVendu.class);
    ad.setTitle(req.getTitle());
    ad.setDescription(req.getDescription());
    prop.getFileBien().forEach(it -> ad.getPhotos().add(it.getFileName()));
    var response =
        restTemplate.postForObject(serverPath + "/paruvendu", ad, AdServerResponse.class);
    return response.getUrl();
  }

  private String createLeBonCoinAd(BienVendre prop, CreateAdRequest req) {
    var ad = mapper.map(prop, AdLeBonCoin.class);
    ad.setTitle(req.getTitle());
    ad.setDescription(req.getDescription());
    prop.getFileBien().forEach(it -> ad.getPhotos().add(it.getFileName()));
    var response =
        restTemplate.postForObject(serverPath + "/leboncoin", ad, AdServerResponse.class);
    return response.getUrl();
  }

  private String createFacebookAd(BienVendre prop, CreateAdRequest req) {
    var ad = mapper.map(prop, AdFacebookMarketplace.class);
    ad.setDescription(req.getDescription());
    prop.getFileBien().forEach(it -> ad.getPhotos().add(it.getFileName()));
    var response = restTemplate.postForObject("boturl/facebook", ad, AdServerResponse.class);
    return response.getUrl();
  }
}
