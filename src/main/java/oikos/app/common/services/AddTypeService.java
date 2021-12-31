package oikos.app.common.services;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import oikos.app.common.exceptions.BaseException;
import oikos.app.common.exceptions.EntityNotFoundException;
import oikos.app.common.models.BienVendre;
import oikos.app.common.models.MyPropertyType;
import oikos.app.common.repos.PropTypeRepo;
import oikos.app.common.responses.DoneResponse;
import oikos.app.common.services.PropertyService.PropertyMethods;
import oikos.app.common.services.PropertyService.PropertyMethods.Names;
import oikos.app.common.utils.Authorizable;
import oikos.app.messaging.Message;
import oikos.app.users.Role;
import oikos.app.users.UserRepo;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AddTypeService implements Authorizable<AddTypeService.TypeMethods> {
	private final PropTypeRepo repo;
	private final UserRepo userRepo;

	@ToString
	enum TypeMethods {
		ADD_PROPERTY_TYPE(Names.ADD_PROPERTY_TYPE), GET_BY_COODE(Names.GET_BY_COODE), DELETE_TYPE(Names.DELETE_TYPE),
		FIND_ALL(Names.FIND_ALL),GET_PROPERTY_BY_TYPE(Names.GET_PROPERTY_BY_TYPE);

		private final String label;

		TypeMethods(String label) {
			this.label = label;
		}

		public static class Names {
			public static final String ADD_PROPERTY_TYPE = "ADD_PROPERTY_TYPE";
			public static final String GET_BY_COODE = "GET_BY_COODE";
			public static final String DELETE_TYPE = "DELETE_TYPE";
			public static final String FIND_ALL = "FIND_ALL";
			public static final String GET_PROPERTY_BY_TYPE="GET_PROPERTY_BY_TYPE";

			private Names() {
			}
		}

	}

	public MyPropertyType add(MyPropertyType dto) {
		log.info("save prop type ");
		if (repo.checkExistByNameANdcCode(dto.getCode(), dto.getName())) {
			throw new BaseException("this code  already exist");
		}
		return repo.save(dto);
	}

	public String getByCode(int code) {
		try {
			return repo.findbycode(code).getName();
		} catch (Exception e) {
			throw new BaseException("this code  doesn't exist");
		}
	}

	public DoneResponse deletetype(int code) {
		if (!repo.checkExist(code)) {
			throw new BaseException("this code  doesn't exist");
		}
		var prop = repo.findbycode(code);
		repo.deleteById(prop.getId());

		return new DoneResponse("Successfly deleted ");
	}

	public List<MyPropertyType> findall() {
		return repo.findAll();
	}

	@Override
	public boolean canDo(TypeMethods methodName, String userID, String objectID) {
		try {
			return switch (methodName) {
			case ADD_PROPERTY_TYPE, DELETE_TYPE -> CollectionUtils
	        .containsAny(userRepo.getOne(userID).getRoles(),
	                List.of(Role.SECRETARY, Role.ADMIN));
			case GET_BY_COODE, FIND_ALL,GET_PROPERTY_BY_TYPE -> true;

			};
		} catch (javax.persistence.EntityNotFoundException e) {
			throw new EntityNotFoundException(Message.class, objectID);
		}
	}

}
