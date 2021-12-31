package oikos.app.common.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import oikos.app.common.models.LikedProperty;
import oikos.app.common.models.Likes;

@Repository
public interface PropertyLikesRepo extends JpaRepository<LikedProperty, String> {
	@Query(value = "SELECT p FROM property_likes p WHERE p.prop.id = ?1 AND p.userId = ?2")
	LikedProperty getLikeOrDislikeByUser(String propId, String userId);
	
	@Query(value = "SELECT p FROM property_likes p WHERE p.userId = ?1 AND p.status = ?2")
	List<LikedProperty> myLikes( String userId,Likes status);
	
	@Query(value = "SELECT p FROM property_likes p WHERE p.prop.id = ?1")
	List<LikedProperty> getLikesByPrOP( String propId);
	//MAX(p.prop.id)
	@Query(value = "SELECT COUNT(p.prop.id),p.prop.id FROM property_likes p WHERE p.status = ?1 GROUP BY p.prop ORDER BY p.prop.id ASC")
	List<String> mostlikedprop(Likes status);
	
	@Query(value = "SELECT p.prop.id,COUNT(p.prop.id) FROM property_likes p WHERE p.status = ?1 GROUP BY p.prop ORDER BY p.prop.id ASC")
	List<String> stats(Likes status);
	
	@Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM property_likes p WHERE p.userId = :userId AND p.prop.id =:propId")
	boolean checkLike(@Param("userId") String userId,@Param("propId") String propId);


//	

}
