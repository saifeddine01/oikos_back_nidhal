package oikos.app.common.controllers;

import java.util.List;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import oikos.app.common.responses.DoneResponse;
import oikos.app.common.entityResponses.AppointmentResponse;
import oikos.app.common.exceptions.AppointmentAlreadyTaken;
import oikos.app.common.exceptions.EntityNotFoundException;
import oikos.app.common.models.Appointment;
import oikos.app.common.models.Disponibility;
import oikos.app.common.repos.AppointmentRepo;
import oikos.app.common.repos.DisponibilityRepo;
import oikos.app.common.request.AppointmentRequest;
import oikos.app.security.CurrentUser;
import oikos.app.security.OikosUserDetails;
import oikos.app.common.services.AppointmentService;

@Slf4j @ToString @RestController(value = "appointment-controller")
@AllArgsConstructor @RequestMapping("/appointment")
public class AppointmentController {
  private final AppointmentRepo repo;
  private final DisponibilityRepo dispoRepo;
  private final AppointmentService service;

  private final ModelMapper mapper;

  @PreAuthorize("@appointmentService.canDo('ADD_APPOINTMENT',#user.username,#user.username)")
  @PostMapping("/") public Appointment createAppointment(
    @Valid @RequestBody AppointmentRequest dto,
    @CurrentUser OikosUserDetails user) throws AppointmentAlreadyTaken {
    if (service.exists(dto.getIdDispo())) {
      throw new AppointmentAlreadyTaken("Appointment already taken.");
    }
    return service.addAppointment(dto, user.getUser());
  }

  @PreAuthorize("@appointmentService.canDo('GET_ALL_APPOINTMENT',#user.username,#user.username)")
  @GetMapping("/")
  public Page<Appointment> getAllEmployees(@CurrentUser OikosUserDetails user,
    @RequestParam(defaultValue = "0") Integer pageNo,
    @RequestParam(defaultValue = "10") Integer pageSize,
    @RequestParam(defaultValue = "dateStart") String sortBy) {
    return service.getAllAppointment(pageNo, pageSize, sortBy);
  }

  @PreAuthorize("@appointmentService.canDo('FIND_BY_ID_APPOINTMENT',#user.username,#appId)")
  @GetMapping("/{appId}") public AppointmentResponse getOneAppointment(
    @CurrentUser OikosUserDetails user, @PathVariable String appId) {
    return service.getOneAppointment(appId);
  }

  @PreAuthorize("@appointmentService.canDo('DELETE_APPOINTMENT',#user.username,#id)")
  @DeleteMapping("/{id}")
  public DoneResponse DeleteApp(@PathVariable("id") String id,
    @CurrentUser OikosUserDetails user) {

    if (repo.existsById(id)) {
      repo.deleteById(id);
      return new DoneResponse(
        "Appointment with id " + id + "has been deleted with success");
    } else {
      return new DoneResponse(
        "Appointment with id " + id + "could not be found ");
    }

  }

  @PreAuthorize("@appointmentService.canDo('APPROVE_APPOINTMENT',#user.username,#idAppointment)")
  @GetMapping("/{idAppointment}/approve")
  public DoneResponse approve(@PathVariable String idAppointment,
    @CurrentUser OikosUserDetails user) {
    return service.approveappointment(idAppointment);
  }

  @PreAuthorize("@appointmentService.canDo('REJECT_APPOINTMENT',#user.username,#idAppointment)")
  @GetMapping("/{idAppointment}/reject")
  public DoneResponse reject(@PathVariable String idAppointment,
    @CurrentUser OikosUserDetails user) {
    return service.rejectappointment(idAppointment);
  }
  // TODO Page instead of list

  @PreAuthorize("@appointmentService.canDo('MY_APPOINTMENT',#user.username,#user.username)")
  @GetMapping("/my-appointment")
  public List<Appointment> getAllAppoitment(@CurrentUser OikosUserDetails user,
    @RequestParam(required = false) String status) {
    return service.allMyapp(user.getUser(), status);
  }

  @PreAuthorize("@appointmentService.canDo('EDIT_APPOINTMENT',#user.username,#idAppointment)")
  @PutMapping("/{idAppointment}")
  public ResponseEntity<Appointment> editAppointment(
    @Valid @RequestBody AppointmentRequest dto,
    @PathVariable String idAppointment, @CurrentUser OikosUserDetails user)
    throws EntityNotFoundException {
    Appointment app = repo.findById(idAppointment).orElseThrow(
      () -> new EntityNotFoundException(getClass(), idAppointment));
    Disponibility dispo = dispoRepo.findById(idAppointment).orElseThrow(
      () -> new EntityNotFoundException(getClass(), idAppointment));
    //TODO use modelmapper instead.
    app.setDescription(dto.getDescription());
    app.setTitle(dto.getTitle());
    app.setDisponibility(dispo);
    try {
      if (dto.getDateStartApp().isAfter(dto.getDateEndApp())) {
        app.setDateStart(dto.getDateStartApp());
        app.setDateEnd(dto.getDateEndApp());
      }
    } catch (NullPointerException e) {
      // TODO: handle exception
    }

    repo.save(app);
    return ResponseEntity.ok(app);
  }
}
