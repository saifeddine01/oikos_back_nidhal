package oikos.app.common.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oikos.app.common.models.PropertyFile;
import oikos.app.common.repos.PropertyFileRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @AllArgsConstructor @Slf4j public class PropFileService {
  private final PropertyFileRepo repo;

  @Transactional public PropertyFile addFile(PropertyFile file) {
    return repo.save(file);
  }


}
