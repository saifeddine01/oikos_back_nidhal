package oikos.app.common.services;

import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import oikos.app.common.entityResponses.DispoResponse;
import oikos.app.common.exceptions.BaseException;
import oikos.app.common.exceptions.EntityNotFoundException;
import oikos.app.common.models.BienVendre;
import oikos.app.common.models.Disponibility;
import oikos.app.common.repos.DisponibilityRepo;
import oikos.app.common.request.DispoRequest;
import oikos.app.common.responses.DoneResponse;
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

import java.util.List;

@Service @AllArgsConstructor @Slf4j public class DispoService {
  private final DisponibilityRepo repo;
  private final UserRepo userRepo;

  private final ModelMapper mapper;


  @ToString enum DispoMethods {
    ADD_DISPO(Names.ADD_DISPO), EDIT_DISPO(Names.EDIT_DISPO), GET_ONE_DISPO(
      Names.GET_ONE_DISPO), GET_ALL_DISPO(Names.GET_ALL_DISPO), DELETE_DISPO(
      Names.DELETE_DISPO), GET_DISPO_BY_USER(Names.GET_DISPO_BY_USER);

    private final String label;

    DispoMethods(String label) {
      this.label = label;
    }

    public static class Names {
      public static final String ADD_DISPO = "ADD_DISPO";
      public static final String EDIT_DISPO = "EDIT_DISPO";
      public static final String GET_ONE_DISPO = "GET_ONE_DISPO";
      public static final String GET_ALL_DISPO = "GET_ALL_DISPO";
      public static final String DELETE_DISPO = "DELETE_DISPO";
      public static final String GET_DISPO_BY_USER = "GET_DISPO_BY_USER";

      private Names() {
      }
    }
  }

  public boolean canDo(DispoMethods methodName, String userID,
    String objectID) {
    try {
      return switch (methodName) {
        case ADD_DISPO, GET_ONE_DISPO, GET_DISPO_BY_USER -> true;
        case GET_ALL_DISPO -> CollectionUtils
          .containsAny(userRepo.getOne(userID).getRoles(),
            List.of(Role.SECRETARY, Role.ADMIN));
        case DELETE_DISPO, EDIT_DISPO -> repo.getOne(objectID).getUserId()
          .equals(userID);
      };
    } catch (javax.persistence.EntityNotFoundException e) {
      throw new EntityNotFoundException(BienVendre.class, objectID);
    }
  }

  public DispoResponse editDispo(User user, String id, DispoRequest dto) {
    Disponibility dispo = repo.findById(id)
      .orElseThrow(() -> new EntityNotFoundException(getClass(), id));

    if (dispo.getUserDispo().getId().equals(user.getId())) {
      if (dto.getDateStart() != null) {
        dispo.setDateStart(dto.getDateStart());
      }
      if (dto.getDateEnd() != null) {
        dispo.setDateEnd(dto.getDateEnd());
      }
      if (dto.getTitle() != null) {
        dispo.setTitle(dto.getTitle());
      }
      if (dto.getDescription() != null) {
        dispo.setDescription(dto.getDescription());
      }
      if(dto.getDispotype() != null) {
    	  dispo.setDispotype(dto.getDispotype());
      }
      try {
        if (dto.getIsAllDay() != null) {
          dispo.setAllDay(dto.getIsAllDay());
        } else if (dto.getIsAllDay() == null) {
          dispo.setAllDay(dispo.isAllDay());
        }

      } catch (NullPointerException e) {
        // TODO: handle exception
      }
      dispo = repo.save(dispo);
    }

    return DispoResponse.builder().dateStart(dispo.getDateStart())
      .dateEnd(dispo.getDateEnd()).allDay(dispo.isAllDay())
      .title(dispo.getTitle()).id(dispo.getId())
      .description(dispo.getDescription()).userId(user.getId()).build();
  }

  public List<Disponibility> getDispoByUser(String userId) {
    return repo.findByUserId(userId);
  }

  public Disponibility getDispoById(String id, User user) {

    return repo.findById(id)
      .orElseThrow(() -> new EntityNotFoundException(Disponibility.class, id));
  }

  public DispoResponse addDisponibility(User user, DispoRequest dto) {

    Disponibility did = Disponibility.builder().dateEnd(dto.getDateEnd())
      .dateStart(dto.getDateStart()).isAllDay(dto.getIsAllDay())
      .title(dto.getTitle()).userDispo(userRepo.getOne(user.getId())).dispotype(dto.getDispotype())
      .description(dto.getDescription()).userId(user.getId()).build();
    if (did.getDateStart().isBefore(did.getDateEnd())) {
      did = repo.save(did);
    } else {
      throw new BaseException("Date start should be before data end !");
    }
    return DispoResponse.builder().dateStart(did.getDateStart()).dispotype(did.getDispotype())
      .dateEnd(did.getDateEnd()).allDay(did.isAllDay()).title(did.getTitle())
      .id(did.getId()).description(did.getDescription()).userId(user.getId())
      .build();
  }
  public List<Disponibility> addDisponibilities(User user, List<DispoRequest> dispoRequest) {

	 for (DispoRequest dto : dispoRequest) {
		 Disponibility did = Disponibility.builder().dateEnd(dto.getDateEnd())
			      .dateStart(dto.getDateStart()).isAllDay(dto.getIsAllDay())
			      .title(dto.getTitle()).userDispo(userRepo.getOne(user.getId())).dispotype(dto.getDispotype())
			      .description(dto.getDescription()).userId(user.getId()).build();
		 repo.save(did);
		
	}
	    return getDispoByUser(user.getId());
	  }
  public DoneResponse deleteManyDispo(List<String> ids) {
	  try {
			for (String idDispo : ids) {
				repo.deleteById(idDispo);
			}
			return new DoneResponse("You have succefully delete many disponibilities");
			
		}
			catch (Exception e) {
			throw new BaseException("Sorry we could delete your disponibilities please check if they already exist or try again ");
			}
  }

  public Page<Disponibility> getAllDispo(Integer pageNo, Integer pageSize,
    String sortBy) {
    Pageable paging =
      PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());

    return repo.findAll(paging);

  }
}
