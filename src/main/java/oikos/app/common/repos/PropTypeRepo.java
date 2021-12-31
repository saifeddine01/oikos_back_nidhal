package oikos.app.common.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import oikos.app.common.models.MyPropertyType;

@Repository
public interface PropTypeRepo extends JpaRepository<MyPropertyType, String> {
	@Query(value = "SELECT p FROM MyPropertyType p WHERE p.code = ?1")
	MyPropertyType findbycode(int code);
	
	
	@Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM MyPropertyType p WHERE p.code = :codeId")
	boolean checkExist(@Param("codeId") int codeId);

	@Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM MyPropertyType p WHERE p.code = :codeId OR p.name = :name")
	boolean checkExistByNameANdcCode(@Param("codeId") int codeId,@Param("name") String name);

}
