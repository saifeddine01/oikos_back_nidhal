package oikos.app.common.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oikos.app.common.models.RendezVous;
import oikos.app.common.repos.DisponibilityRepo;
import oikos.app.common.repos.RendezVousRepo;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class RendezVousService {
  private final DisponibilityRepo repo;

  private final RendezVousRepo rendezvousRepo;

  @Transactional
  public RendezVous addRendezVous(RendezVous dto) {
    return rendezvousRepo.save(dto);
  }

  @Transactional
  public void addRendezVousVoid(RendezVous dto) {
    rendezvousRepo.save(dto);
  }
}
