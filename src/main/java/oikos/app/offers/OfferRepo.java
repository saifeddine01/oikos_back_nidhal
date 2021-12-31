package oikos.app.offers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Created by Mohamed Haamdi on 11/05/2021.
 */
public interface OfferRepo extends JpaRepository<Offer, String> {
  @Query("select o from Offer o where o.property.id = :propID order by o.createdAt desc")
  Page<Offer> listAllOffersForProperty(@Param("propID") String propID,
    Pageable paging);

  @Query("select o from Offer o where (o.sender.id = :userID or o.recipient.id = :userID) order by o.createdAt desc")
  Page<Offer> listAllOffersForUser(@Param("userID") String userID,
    Pageable paging);

  @Transactional
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("update Offer o set o.status = 'EXPIRED' where o.status = 'PENDING' and o.endsAt < :endDate")
  void markAllExpiredOffersAsExpired(@Param("endDate") LocalDate now);
}
