package oikos.app.search;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BienVendreRepository extends JpaRepository<BienVendre, String> {
  @Query("select max(b.price) from bienavendre b")
  Optional<Double> getMaxPrice();

  @Query("select min(b.price) from bienavendre b")
  Optional<Double> getMinPrice();
}
