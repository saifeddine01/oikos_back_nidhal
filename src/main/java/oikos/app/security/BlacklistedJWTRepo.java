package oikos.app.security;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;

public interface BlacklistedJWTRepo extends JpaRepository<BlacklistedJWT, String> {

  @Modifying
  @Query("delete from BlacklistedJWT t where t.purgeAt <= ?1")
  void deleteAllPurgable(Instant now);
}
