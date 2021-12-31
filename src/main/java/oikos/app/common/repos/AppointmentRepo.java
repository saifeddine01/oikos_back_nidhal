package oikos.app.common.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import oikos.app.common.models.Appointment;
import oikos.app.common.models.Status;

public interface AppointmentRepo extends JpaRepository<Appointment, String> {
//	@Query(value = "select b from Appointment b where b.disponibility.id = ?1")
//	Appointment existsApp(String dispo);
	
	@Query(" FROM Appointment b WHERE b.disponibility.id = :dispo")
	Optional<Appointment> existsApp(String dispo);
	
//	@Query(" FROM Appointment a WHERE a.appTaker = :userId")
//	List<Appointment> myAppoitments(String userId);
//	
	@Query(" FROM Appointment a WHERE a.appTaker = :userId AND a.status=:status")
	List<Appointment> allMyapp(String userId,Status status);
	
	
}
