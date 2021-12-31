package oikos.app.buyer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Created by Mohamed Haamdi on 16/04/2021. */
public interface BuyerRepo extends JpaRepository<Buyer, String> {
  @Query("select s from Buyer s where s.id = :user")
  Buyer getByUserID(@Param("user") String userID);
}
