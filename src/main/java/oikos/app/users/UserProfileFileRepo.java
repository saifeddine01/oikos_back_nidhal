package oikos.app.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Created by Mohamed Haamdi on 02/06/2021.
 */
public interface UserProfileFileRepo
  extends JpaRepository<UserProfileFile, String> {
  @Query("select u from UserProfileFile u where u.user.id = :id")
  Optional<UserProfileFile> getProfilePictureForUser(@Param("id") String id);
}
