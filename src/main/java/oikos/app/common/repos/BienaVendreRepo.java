package oikos.app.common.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import oikos.app.common.models.BienVendre;
import oikos.app.common.models.Status;

@Repository
public interface BienaVendreRepo extends JpaRepository<BienVendre, String> {
	@Query(value = "SELECT b FROM bienavendre b WHERE b.userId.id = ?1")
	List<BienVendre> findPropByUser(String userId);

	
	@Query(value = "SELECT b FROM bienavendre b WHERE b.status= ?1")
	List<BienVendre> findPropByStatus(Status status);
	
	
	@Query(value = "SELECT b FROM bienavendre b WHERE b.userId.id = ?1 AND b.status= ?2")
	List<BienVendre> findPropByUserandStatus(String userId,Status status);
	
	
	@Query(value = "SELECT b FROM bienavendre b WHERE b.address.city = ?1 AND b.status='Approved'")
	List<BienVendre> findPropByLiexuandStatus(String city);
	
	
}
