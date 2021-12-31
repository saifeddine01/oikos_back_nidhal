package oikos.app.seller;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/** Created by Mohamed Haamdi on 15/04/2021. */
public interface SellerRepo extends JpaRepository<Seller, String> {
  @Query("select s from Seller s where s.id = :user")
  Optional<Seller> getByUserID(@Param("user") String userID);
}
