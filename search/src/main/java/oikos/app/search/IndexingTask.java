package oikos.app.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.search.jpa.Search;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class IndexingTask {
  private final EntityManager entityManager;
  private final BienVendreRepository repo;
  private final List<Integer> rangeCutoffs = new ArrayList<>();
  private boolean isCutoffsReady = false;

  public List<Integer> getRangeCutoffs() {
    return rangeCutoffs;
  }

  public boolean isCutoffsReady() {
    return isCutoffsReady;
  }

  @Scheduled(cron = "0 0 0 * * ?")
  public void indexProperties() throws InterruptedException {
    Search.getFullTextEntityManager(entityManager)
        .createIndexer()
        .purgeAllOnStart(true)
        .optimizeAfterPurge(true)
        .optimizeOnFinish(true)
        .threadsToLoadObjects(5)
        .purgeAllOnStart(true)
        .startAndWait();
    generateCutoffs();
  }

  public void generateCutoffs() {
    var min = repo.getMinPrice().orElse(0.0);
    var max = repo.getMaxPrice().orElse(Double.MAX_VALUE);
    var step = (max - min) / 5;
    for (int i = 1; i < 5; i++) {
      final double unroundedCutoff = min + (i * step);
      final int cutoff = (int) (Math.ceil(unroundedCutoff / 10000.0) * 10000.0);
      rangeCutoffs.add(cutoff);
    }
    isCutoffsReady = true;
  }
}
