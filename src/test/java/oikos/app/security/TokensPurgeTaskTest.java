package oikos.app.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

/** Created by Mohamed Haamdi on 11/05/2021. */
@ExtendWith(MockitoExtension.class)
class TokensPurgeTaskTest {
  @Mock private VerificationTokenRepo repo;
  @InjectMocks private TokensPurgeTask underTest;

  @Test
  void purgeExpiredTokens() {
    // When
    underTest.purgeExpiredTokens();
    // Then
    verify(repo).deleteAllExpiredSince(any());
  }
}
