package oikos.app.offers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/** Created by Mohamed Haamdi on 11/05/2021. */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OfferExpirationTask {
  private final OfferRepo offerRepo;

  @Scheduled(cron = "0 0 1 * * ?")
  public void purgeExpiredOffers() {
    log.info(
        "Marking all offers that passed their end dates as expired as of the {}", LocalDate.now());
    offerRepo.markAllExpiredOffersAsExpired(LocalDate.now());
  }
}
