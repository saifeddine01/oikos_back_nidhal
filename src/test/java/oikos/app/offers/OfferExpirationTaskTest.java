package oikos.app.offers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

/** Created by Mohamed Haamdi on 11/05/2021. */
@ExtendWith(MockitoExtension.class)
class OfferExpirationTaskTest {
  @Mock private OfferRepo offerRepo;
  @InjectMocks private OfferExpirationTask underTest;

  @Test
  void purgeExpiredTokens() {
    // When
    underTest.purgeExpiredOffers();
    // Then
    verify(offerRepo).markAllExpiredOffersAsExpired(any());
  }
}
