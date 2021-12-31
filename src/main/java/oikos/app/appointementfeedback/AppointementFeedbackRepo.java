package oikos.app.appointementfeedback;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/** Created by Mohamed Haamdi on 07/05/2021. */
public interface AppointementFeedbackRepo extends JpaRepository<AppointmentFeedback, String> {
  @Query(
      "select f from AppointmentFeedback f where f.property.id = :propID order by f.createdAt desc ")
  Page<AppointmentFeedback> getFeedbackByPropID(@Param("propID") String propID, Pageable paging);

  @Query(
      "select f from AppointmentFeedback f where f.reviewer.id = :reviewerID order by f.createdAt desc")
  Page<AppointmentFeedback> getFeedbackByReviewerID(
      @Param("reviewerID") String reviewerID, Pageable paging);

  @Query(
      "select f from AppointmentFeedback f where f.property.userId.id = :ownerID order by f.createdAt desc")
  Page<AppointmentFeedback> getAllFeedbackByOwnerID(
      @Param("ownerID") String ownerID, Pageable paging);

  boolean existsByAppointmentId(String appointmentID);

  Optional<AppointmentFeedback> findFeedbackByAppointmentId(String appointmentID);
}
