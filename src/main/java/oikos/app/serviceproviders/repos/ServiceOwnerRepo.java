package oikos.app.serviceproviders.repos;

import oikos.app.serviceproviders.models.ServiceOwner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceOwnerRepo
  extends JpaRepository<ServiceOwner, String> {
}
