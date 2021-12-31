package oikos.app.common.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import oikos.app.common.models.Disponibility;

public interface DisponibilityRepo
  extends JpaRepository<Disponibility, String> {
  @Query("select d from Disponibilities d where d.userId = ?1")
  List<Disponibility> findByUserId(String id);
  
  @Modifying
  @Query("delete from Disponibilities f where f.id=:id")
  void deleteDispoById(@Param("id") String id);
}
