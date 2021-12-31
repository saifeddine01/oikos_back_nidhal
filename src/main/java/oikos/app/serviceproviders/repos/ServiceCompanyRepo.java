package oikos.app.serviceproviders.repos;

import oikos.app.serviceproviders.models.ServiceCompany;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ServiceCompanyRepo extends JpaRepository<ServiceCompany, String> {
  @Query("select s from ServiceCompany s where s.isValidated = :isValidated")
  Page<ServiceCompany> getAllCompaniesByActivationStatus(@Param("isValidated") boolean isValidated, Pageable pageable);

  @Query("select (count(s) > 0) from ServiceCompany s where s.SIRET = :SIRET")
  boolean existsBySIRET(@Param("SIRET") String SIRET);

  @Query("select s from ServiceCompany s where s.serviceOwner.id = :userID")
  Optional<ServiceCompany> findByOwnerID(@Param("userID") String userID);
}
