package oikos.app.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.time.Period;

import static org.assertj.core.api.Assertions.assertThat;

/** Created by Mohamed Haamdi on 05/05/2021. */
@DataJpaTest
class VerificationTokenRepoTest {
  @Autowired private VerificationTokenRepo underTest;

  @AfterEach
  void tearDown() {
    underTest.deleteAll();
  }

  @Test
  void findByToken() {
    // Given
    VerificationToken token = VerificationToken.builder().token("tokenID").build();
    underTest.save(token);
    // When
    var res1 = underTest.findByToken("tokenID");
    var res2 = underTest.findByToken("invalid");
    // Then
    assertThat(res1).isPresent();
    assertThat(res2).isEmpty();
  }

  @Test
  void deleteAllExpiredSince() {
    // Given
    VerificationToken token =
        VerificationToken.builder()
            .token("tokenID")
            .expiresAt(Instant.now().plus(Period.ofDays(2)))
            .build();
    underTest.save(token);
    VerificationToken token2 =
        VerificationToken.builder().token("tokenID2").expiresAt(Instant.EPOCH).build();
    underTest.save(token2);
    // When
    underTest.deleteAllExpiredSince(Instant.now());
    var res1 = underTest.findByToken("tokenID");
    var res2 = underTest.findByToken("tokenID2");
    // Then
    assertThat(res1).isPresent();
    assertThat(res2).isEmpty();
  }
}
