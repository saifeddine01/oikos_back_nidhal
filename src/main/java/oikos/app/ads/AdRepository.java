package oikos.app.ads;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdRepository extends JpaRepository<Ad, String> {
  @Query("select sum(a.views) from Ad a where a.propOwner.id = :id")
  int getTotalViewsForUser(@Param("id") String id);

  @Query("select sum(a.views) from Ad a where a.propOwner.id = :id and a.adPlatform = :adPlatform")
  int getTotalViewsForUserByAdPlatform(
      @Param("id") String id, @Param("adPlatform") AdPlatform adPlatform);

  @Query("select a from Ad a where a.propOwner.id = :userid order by a.createdAt DESC")
  Page<Ad> getAdsForUser(@Param("userid") String userid, Pageable pageable);
}
