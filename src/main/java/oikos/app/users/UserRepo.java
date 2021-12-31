package oikos.app.users;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** Created by Mohamed Haamdi on 08/02/2021 */
@Repository
public interface UserRepo extends JpaRepository<User, String> {
  @Query(
      "select u.id from oikosuser u where u.email = :emailOrPhone or u.phoneNumber = :emailOrPhone")
  Optional<String> findIDByEmailOrPhoneNumber(@Param("emailOrPhone") String emailOrPhoneNumber);

  boolean existsByPhoneNumber(String phoneNumber);

  boolean existsByEmailIgnoreCase(String email);

  @Query("select o from oikosuser o where o.sellerProfile.version is not null")
  Page<UserInfo> getAllSellers(Pageable pageable);

  Optional<UserInfo> getUserInfoById(String id);
}
