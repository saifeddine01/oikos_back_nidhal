package oikos.app.serviceproviders.repos;

import oikos.app.serviceproviders.models.Collaborator;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollaboratorRepo
  extends JpaRepository<Collaborator, String> {
}
