package oikos.app.oikosservices;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OikosServiceRepo extends JpaRepository<OikosService, String> {
  @Query("select o from OikosService o where o.serviceCompany.id = :id")
  Page<OikosService> getServicesByCompany(@Param("id") String companyID, Pageable paging);

  @Query("select o from OikosService o where o.serviceType = :type")
  Page<OikosService> getServicesByType(@Param("type") ServiceType type, Pageable paging);
}
