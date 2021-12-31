package oikos.app.common.services;

import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import oikos.app.common.entityResponses.AppointmentResponse;
import oikos.app.common.exceptions.BaseException;
import oikos.app.common.exceptions.EntityNotFoundException;
import oikos.app.common.models.Appointment;
import oikos.app.common.models.BienVendre;
import oikos.app.common.models.Disponibility;
import oikos.app.common.models.Status;
import oikos.app.common.repos.AppointmentRepo;
import oikos.app.common.repos.DisponibilityRepo;
import oikos.app.common.request.AppointmentRequest;
import oikos.app.common.responses.DoneResponse;
import oikos.app.notifications.CreateNotificationRequest;
import oikos.app.notifications.NotificationService;
import oikos.app.users.Role;
import oikos.app.users.User;
import oikos.app.users.UserRepo;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;

@Service @AllArgsConstructor @Slf4j public class AppointmentService {
  private static String appLink="appointment/";
  private final AppointmentRepo repo;
  private final DisponibilityRepo dispoRep;
  private final UserRepo userRepo;
  private final NotificationService notif;
  private final ModelMapper mapper;


  @ToString enum AppointmentsMethods {
    ADD_APPOINTMENT(Names.ADD_APPOINTMENT), GET_ALL_APPOINTMENT(
      Names.GET_ALL_APPOINTMENT), DELETE_APPOINTMENT(
      Names.DELETE_APPOINTMENT), REJECT_APPOINTMENT(
      Names.REJECT_APPOINTMENT), EDIT_APPOINTMENT(
      Names.EDIT_APPOINTMENT), MY_APPOINTMENT(
      Names.MY_APPOINTMENT), FIND_BY_ID_APPOINTMENT(
      Names.FIND_BY_ID_APPOINTMENT), APPROVE_APPOINTMENT(
      Names.APPROVE_APPOINTMENT);

    private final String label;

    AppointmentsMethods(String label) {
      this.label = label;
    }

    public static class Names {
      public static final String ADD_APPOINTMENT = "ADD_APPOINTMENT";
      public static final String GET_ALL_APPOINTMENT = "GET_ALL_APPOINTMENT";
      public static final String DELETE_APPOINTMENT = "DELETE_APPOINTMENT";
      public static final String APPROVE_APPOINTMENT = "APPROVE_APPOINTMENT";
      public static final String REJECT_APPOINTMENT = "REJECT_APPOINTMENT";
      public static final String EDIT_APPOINTMENT = "EDIT_APPOINTMENT";
      public static final String MY_APPOINTMENT = "MY_APPOINTMENT";
      public static final String FIND_BY_ID_APPOINTMENT =
        "FIND_BY_ID_APPOINTMENT";

      private Names() {
      }
    }
  }

  public boolean canDo(AppointmentsMethods methodName, String userID,
    String objectID) {
    var user= userRepo.getOne(userID);
    try {
      return switch (methodName) {
        case ADD_APPOINTMENT ,MY_APPOINTMENT -> isBuyerOnly(user) ? isApprovedBuyer(user): true;
        case FIND_BY_ID_APPOINTMENT ->
          repo.getOne(objectID).getDisponibility().getUserId().equals(userID)
            || repo.getOne(objectID).getAppTaker().equals(userID) ;
        case GET_ALL_APPOINTMENT -> CollectionUtils
        .containsAny(userRepo.getOne(userID).getRoles(),
                List.of(Role.ADMIN,Role.SECRETARY));
        case APPROVE_APPOINTMENT, REJECT_APPOINTMENT ->
          repo.getOne(objectID).getDisponibility().getUserId().equals(userID)
            || userRepo.getOne(userID).getRoles().stream()
            .anyMatch(i -> i.equals(Role.SECRETARY));
        case DELETE_APPOINTMENT, EDIT_APPOINTMENT -> userRepo.getOne(userID).getRoles().stream()
          .anyMatch(i -> i.equals(Role.SECRETARY)) || repo.getOne(objectID)
          .getAppTaker().equals(objectID) || repo.getOne(objectID)
          .getDisponibility().getUserDispo().getId().equals(objectID);
      };
    } catch (javax.persistence.EntityNotFoundException e) {
      throw new EntityNotFoundException(BienVendre.class, objectID);
    }
  }

  @Transactional
  public Appointment addAppointment(AppointmentRequest dto, User user) {
    Disponibility dispo = dispoRep.findById(dto.getIdDispo()).orElseThrow(
      () -> new EntityNotFoundException(Disponibility.class, dto.getIdDispo()));
    Appointment app = Appointment.builder().title(dto.getTitle())
      .description(dto.getDescription()).disponibility(dispo)
      .appTaker(user.getId()).dateStart(dto.getDateStartApp())
      .dateEnd(dto.getDateEndApp()).build();
    if (app.getDateStart().isBefore(dispo.getDateEnd()) && app.getDateStart()
      .isAfter(dispo.getDateStart()) && app.getDateEnd()
      .isAfter(dispo.getDateStart()) && app.getDateEnd()
      .isBefore(dispo.getDateEnd())) {

    	CreateNotificationRequest notifreq = CreateNotificationRequest.builder()
				.content(user.getFirstName()+" "+user.getLastName() +" took an appointment with you ").userId(dispo.getUserId()).lien(appLink+app.getId())
				.build();
		notif.addNotification(notifreq);
      return repo.save(app);
    } else {
      throw new BaseException(
        "Please verify that date is between " + dispo.getDateStart() + " and "
          + dispo.getDateEnd());
    }
  }

  @Transactional public DoneResponse approveappointment(String id) {

    Appointment app = repo.findById(id)
      .orElseThrow(() -> new EntityNotFoundException(Appointment.class, id));

    app.setStatus(Status.Approved);
    repo.save(app);
    CreateNotificationRequest reqAppTaker =
      CreateNotificationRequest.builder().userId(app.getAppTaker())
        .content("Your appointment request has been accepted ")

        .lien(appLink+app.getId()).build();
    CreateNotificationRequest reqDispoUser = CreateNotificationRequest.builder()
      .userId(app.getDisponibility().getUserId())
      .content("Your appointment request has been accepted ")
      .lien(appLink+app.getId()).build();
    notif.addNotification(reqAppTaker);
    notif.addNotification(reqDispoUser);
    return new DoneResponse(
      "Appointment with id " + app.getId() + "has been APPROVED");
  }

  @Transactional public DoneResponse rejectappointment(String id) {

    Appointment app = repo.findById(id)
      .orElseThrow(() -> new EntityNotFoundException(Appointment.class, id));

    app.setStatus(Status.Rejected);
    repo.save(app);
    CreateNotificationRequest reqDispoUser = CreateNotificationRequest.builder()
      .userId(app.getAppTaker())
      .content("Your appointment request has been rejected ")
      //change this link to api url of entity
      .lien(appLink+app.getId()).build();
    notif.addNotification(reqDispoUser);
    return new DoneResponse(
      "Appointment with id " + app.getId() + "has been REJECTED");
  }

  public Page<Appointment> getAllAppointment(Integer pageNo, Integer pageSize,
    String sortBy) {
    Pageable paging =
      PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());

    return repo.findAll(paging);
  }

  public AppointmentResponse getOneAppointment(String appId) {
    Appointment app = repo.findById(appId)
      .orElseThrow(() -> new EntityNotFoundException(Appointment.class, appId));
    log.info(app.toString());
    AppointmentResponse map = mapper.map(app, AppointmentResponse.class);
    map.setDispoId(app.getDisponibility().getId());
    return map;
  }

  public boolean exists(String id_dispo) {
    Appointment app;
    boolean res = false;
    try {
      app = repo.existsApp(id_dispo).orElseThrow(
        () -> new EntityNotFoundException(Appointment.class, id_dispo));
      // app.getDisponibility().getDateStart().isBefore(app.getDateEnd()
      if (!app.getId().isEmpty() && app.getStatus().equals(Status.Approved)) {
        log.info("appointment exists");
        res = true;
      }
    } catch (Exception e) {
      log.info("there is no appointment");
    }

    return res;
  }

  public List<Appointment> allMyapp(User user, String status) {
    List<Appointment> list = null;
    Status st = Status.Pending;
    try {
      st = Status.valueOf(status);
    } catch (IllegalArgumentException e) {
     throw new BaseException("Sorry we couldn't find an appointment with this status");
    }
    list = repo.allMyapp(user.getId(), st);
    if (list.isEmpty()) {
      throw new EntityNotFoundException(Appointment.class,
        "You don't have any appointment with " + status + " Status ");
    } else {
      return list;
    }
  }

  private boolean isBuyerOnly(User u){
    return u.getRoles().equals(Set.of(Role.BUYER));
  }
  private boolean isApprovedBuyer(User u){
    return u.getBuyerProfile().isValidated();
  }
}
