package oikos.app.common.controllers;

import java.util.List;

import javax.validation.Valid;

import org.springframework.data.domain.Page;
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
import oikos.app.common.entityResponses.DispoResponse;
import oikos.app.common.exceptions.EntityNotFoundException;
import oikos.app.common.models.Disponibility;
import oikos.app.common.repos.DisponibilityRepo;
import oikos.app.common.request.DispoRequest;
import oikos.app.security.CurrentUser;
import oikos.app.security.OikosUserDetails;
import oikos.app.common.services.DispoService;

@Slf4j @ToString @RestController(value = "dispo-controller") @AllArgsConstructor
@RequestMapping("/dispo") 
public class DisponibilityController {

  private final DispoService service;
  private final DisponibilityRepo repo;

 
  @PreAuthorize("@dispoService.canDo('ADD_DISPO',#user.username,#user.username)")
  @PostMapping("/") public DispoResponse addDispo(
    @CurrentUser OikosUserDetails user, @Valid @RequestBody DispoRequest dto) {
    return service.addDisponibility(user.getUser(), dto);
  }

  @PreAuthorize("@dispoService.canDo('GET_ONE_DISPO',#user.username,#user.username)")
  @GetMapping("/{id}")
  public Disponibility findByIdString(@PathVariable String id,
    @CurrentUser OikosUserDetails user) {

    return service.getDispoById(id, user.getUser());
  }

  // TODO Page instead of list.
  @PreAuthorize("@dispoService.canDo('GET_DISPO_BY_USER',#user.username,#user.username)")
  @GetMapping("/my-dispo") public List<Disponibility> findByCurrentUser(
    @CurrentUser OikosUserDetails user) {
    return service.getDispoByUser(user.getUser().getId());
  }

  @GetMapping("/{idUser}/user/")
  public List<Disponibility> findByUser(@PathVariable String idUser) {
    return service.getDispoByUser(idUser);
  }

  @PreAuthorize("@dispoService.canDo('DELETE_DISPO',#user.username,#idDispo)")
  @DeleteMapping(value = "/{idDispo}")
  public DoneResponse DeleteDispo(@PathVariable("idDispo") String idDispo,
    @CurrentUser OikosUserDetails user) {
    if (!repo.existsById(idDispo)) {
      throw new EntityNotFoundException(Disponibility.class, idDispo);
    }
    repo.deleteById(idDispo);
    return new DoneResponse("Successfully deleted disponibility : " + idDispo);
  }

  @PreAuthorize("@dispoService.canDo('GET_ALL_DISPO',#user.username,#user.username)")
  @GetMapping("/") public Page<Disponibility> getAllDispo(
    @RequestParam(defaultValue = "0") Integer pageNo,
    @RequestParam(defaultValue = "10") Integer pageSize,
    @RequestParam(defaultValue = "dateStart") String sortBy,
    @CurrentUser OikosUserDetails user) {
    return service.getAllDispo(pageNo, pageSize, sortBy);
  }

  @PreAuthorize("@dispoService.canDo('EDIT_DISPO',#user.username,#idDispo)")
  @PutMapping("/{idDispo}")
  public DispoResponse editDispo(@Valid @RequestBody DispoRequest dto,
    @PathVariable("idDispo") String idDispo, @CurrentUser OikosUserDetails user)
    throws EntityNotFoundException {
    return service.editDispo(user.getUser(), idDispo, dto);
  }
  
 
  @PostMapping("/add") public List<Disponibility> addDispo(
    @CurrentUser OikosUserDetails user, @Valid @RequestBody List<DispoRequest> dto) {
    return service.addDisponibilities(user.getUser(), dto);
  }

  
  @DeleteMapping(value = "/deletemanydispo")
  public DoneResponse deletedispos(@RequestBody List<String> dispos,
    @CurrentUser OikosUserDetails user) {
   return service.deleteManyDispo(dispos);
  }
  

}
