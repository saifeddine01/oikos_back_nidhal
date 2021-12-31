package oikos.app.appointementfeedback;

import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import oikos.app.common.exceptions.BaseException;
import oikos.app.common.exceptions.EntityNotFoundException;
import oikos.app.common.models.Appointment;
import oikos.app.common.models.BienVendre;
import oikos.app.common.repos.AppointmentRepo;
import oikos.app.common.repos.BienaVendreRepo;
import oikos.app.common.utils.Authorizable;
import oikos.app.notifications.NotificationService;
import oikos.app.users.UserRepo;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Created by Mohamed Haamdi on 07/05/2021. */
@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class AppointementFeedbackService
  implements Authorizable<AppointementFeedbackService.FeedbackMethods> {
  private final BienaVendreRepo propRepo;
  private final AppointmentRepo appointmentRepo;
  private final UserRepo userRepo;
  private final AppointementFeedbackRepo appointementFeedbackRepo;
  private final ModelMapper mapper;
  private final NotificationService notificationService;

  @ToString
  enum FeedbackMethods {
    ADD_FEEDBACK(Names.ADD_FEEDBACK),
    DELETE_FEEDBACK(Names.DELETE_FEEDBACK),
    EDIT_FEEDBACK(Names.EDIT_FEEDBACK),
    GET_ONE_FEEDBACK(Names.GET_ONE_FEEDBACK),
    LIST_FEEDBACKS_FOR_PROP(Names.LIST_FEEDBACKS_FOR_PROP),
    LIST_FEEDBACKS_FOR_REVIEWER(Names.LIST_FEEDBACKS_FOR_REVIEWER),
    LIST_FEEDBACKS_FOR_OWNER(Names.LIST_FEEDBACKS_FOR_OWNER),
    GET_BY_APPOINTMENT(Names.GET_BY_APPOINTMENT);
    private final String label;

    FeedbackMethods(String label) {
      this.label = label;
    }

    public static class Names {
      public static final String ADD_FEEDBACK = "ADD_FEEDBACK";
      public static final String DELETE_FEEDBACK = "DELETE_FEEDBACK";
      public static final String EDIT_FEEDBACK = "EDIT_FEEDBACK";
      public static final String GET_ONE_FEEDBACK = "GET_ONE_FEEDBACK";
      public static final String LIST_FEEDBACKS_FOR_PROP = "LIST_FEEDBACKS_FOR_PROP";
      public static final String LIST_FEEDBACKS_FOR_REVIEWER = "LIST_FEEDBACKS_FOR_REVIEWER";
      public static final String LIST_FEEDBACKS_FOR_OWNER = "LIST_FEEDBACKS_FOR_OWNER";
      public static final String GET_BY_APPOINTMENT = "GET_BY_APPOINTMENT";

      private Names() {}
    }
  }

  @Override
  public boolean canDo(FeedbackMethods methodName, String userID, String objectID) {
    try {
      return switch (methodName) {
        //FIXME Get_By_appointment is always true for now until appointment gets userID in it.
        case ADD_FEEDBACK, LIST_FEEDBACKS_FOR_OWNER,LIST_FEEDBACKS_FOR_REVIEWER,GET_BY_APPOINTMENT-> true;
        case EDIT_FEEDBACK,DELETE_FEEDBACK -> appointementFeedbackRepo.getOne(objectID).getReviewer().getId()
          .equals(userID);
        //Either the reviewer or the property owner can see a feedback.
        case GET_ONE_FEEDBACK -> appointementFeedbackRepo.getOne(objectID).getReviewer().getId()
          .equals(userID) || appointementFeedbackRepo.getOne(objectID).getProperty()
          .getUserId().getId().equals(userID);
        case LIST_FEEDBACKS_FOR_PROP -> propRepo.getOne(objectID).getUserId().getId().equals(userID);
      };
    } catch (javax.persistence.EntityNotFoundException e) {
      throw new EntityNotFoundException(AppointmentFeedback.class,objectID);
    }
  }

  public AppointmentFeedback addFeedback(AddAppointementFeedbackRequest req, String reviewerID) {
    if (!propRepo.existsById(req.getPropertyID()))
      throw new EntityNotFoundException(BienVendre.class, req.getPropertyID());
    if (!appointmentRepo.existsById(req.getAppointmentID()))
      throw new EntityNotFoundException(Appointment.class, req.getAppointmentID());
    if(appointementFeedbackRepo.existsByAppointmentId(req.getAppointmentID()))
      throw new BaseException("This appointment already has feedback. Edit it instead");
    log.info("Adding feedback for property {} following up appointment {} by user {}",req.getPropertyID(),req.getAppointmentID(),reviewerID);
    AppointmentFeedback toSave =
        AppointmentFeedback.builder()
            .opinion(req.getOpinion())
            .priceOpinion(req.getPriceOpinion())
            .promisePoints(req.getPromisePoints())
            .intrest(req.getIntrest())
            .reviewer(userRepo.getOne(reviewerID))
            .property(propRepo.getOne(req.getPropertyID()))
            .appointment(appointmentRepo.getOne(req.getAppointmentID()))
            .build();
    final var feedback = appointementFeedbackRepo.save(toSave);
    notificationService.addNotification(
      propRepo.getOne(req.getPropertyID()).getUserId().getId()
        ,"One of your properties received a new feedback."
        ,"/feedback/"+feedback.getId());
    return feedback;
  }

  public AppointmentFeedback getOneFeedback(String feedbackID) {
    log.info("Getting feedback {}", feedbackID);
    return appointementFeedbackRepo.findById(feedbackID).orElseThrow
      (()-> new EntityNotFoundException(AppointmentFeedback.class,feedbackID));
  }

  public AppointmentFeedback editFeedback(String feedbackID, EditAppointementFeedbackRequest req) {
    log.info("Editing feedback {}", feedbackID);
    var feedback = appointementFeedbackRepo.findById(feedbackID).orElseThrow(()-> new EntityNotFoundException(
      AppointmentFeedback.class, feedbackID));
    mapper.map(req,feedback);
    notificationService.addNotification(
      feedback.getProperty().getUserId().getId()
        ,"One of your properties got a feedback modified."
        ,"/feedback/"+feedback.getId());
    return     appointementFeedbackRepo.save(feedback);
  }

  public void deleteFeedback(String feedbackID) {
    log.info("Deleting feedback {}", feedbackID);
    if(!appointementFeedbackRepo.existsById(feedbackID))
      throw new EntityNotFoundException(AppointmentFeedback.class,feedbackID);
    appointementFeedbackRepo.deleteById(feedbackID);
  }

  public Page<AppointmentFeedback> listFeedbacksForOwner(String username, Pageable paging) {
    log.info("Getting page {} of feedbacks for owner {}", paging.getPageNumber(),username);
    return appointementFeedbackRepo.getAllFeedbackByOwnerID(username,paging);
  }

  public Page<AppointmentFeedback> listFeedbacksForReviewer(String username, Pageable paging) {
    log.info("Getting page {} of feedbacks for reviewer {}", paging.getPageNumber(),username);
    return appointementFeedbackRepo.getFeedbackByReviewerID(username,paging);
  }

  public Page<AppointmentFeedback> listFeedbacksForProperty(String propID, Pageable paging) {
    log.info("Getting page {} of feedbacks for property {}", paging.getPageNumber(),propID);
    return appointementFeedbackRepo.getFeedbackByPropID(propID,paging);
  }

  public AppointmentFeedback getFeedbackByAppointment(String appointmentID) {
    log.info("Getting feedbacks for appointment {}",appointmentID);
    return appointementFeedbackRepo.findFeedbackByAppointmentId(appointmentID).
      orElseThrow(()-> new EntityNotFoundException(Appointment.class,appointmentID));
  }
}
