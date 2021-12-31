package oikos.app.common.repos;

import oikos.app.common.models.PropertyFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PropertyFileRepo extends JpaRepository<PropertyFile, String> {
  //	@Query(" FROM Appointment b WHERE b.disponibility.id = :dispo")
  //	Optional<Appointment> existsApp(String dispo);

  @Query(" FROM PropertyFile f WHERE f.bien.id = :idBien")
  List<PropertyFile> findByPropId(String idBien);
  
  @Modifying
  @Query("delete from PropertyFile f where f.id=:id")
  void deletePropById(@Param("id") String id);
}
