package oikos.app.security;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;

/** Created by Mohamed Haamdi on 26/03/2021. */
public interface VerificationTokenRepo extends JpaRepository<VerificationToken, String> {
  Optional<VerificationToken> findByToken(String token);

  @Modifying
  @Query("delete from VerificationToken t where t.expiresAt <= ?1")
  void deleteAllExpiredSince(Instant now);
}
