package oikos.app.common.controllers;

import java.util.List;

import javax.websocket.server.PathParam;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import oikos.app.FirstRunInitializer;
import oikos.app.common.models.MyPropertyType;
import oikos.app.common.repos.PropTypeRepo;
import oikos.app.common.request.AddPropertyTypeRequest;
import oikos.app.common.responses.DoneResponse;
import oikos.app.common.services.AddTypeService;
import oikos.app.security.CurrentUser;
import oikos.app.security.OikosUserDetails;

@Slf4j
@ToString
@RestController(value = "addtype-controller")
@AllArgsConstructor
@RequestMapping("/typeofprop")
public class AddTypeController {
	private final AddTypeService serviceAdd;
	private final PropTypeRepo proptypeRepo;
	
	@PreAuthorize("@addTypeService.canDo('ADD_PROPERTY_TYPE',#user.username,#objectID)")
	@PostMapping("/addtype")
	public MyPropertyType add(@RequestBody AddPropertyTypeRequest dto,@CurrentUser OikosUserDetails user) {
		MyPropertyType req = MyPropertyType.builder().name(dto.getName()).code(dto.getCode()).build();
		var res = serviceAdd.add(req);
		for (MyPropertyType type : serviceAdd.findall()) {
			FirstRunInitializer.typeof.put(type.getCode(), type.getName());
		}
		return res;
	}
	@PreAuthorize("@addTypeService.canDo('DELETE_TYPE',#user.username,#user.username)")
	@DeleteMapping("/deletetype/{code}")
	public DoneResponse deletetype(@PathVariable("code") Integer code,@CurrentUser OikosUserDetails user) {
		return serviceAdd.deletetype(code);
	}
	@PreAuthorize("@addTypeService.canDo('GET_BY_COODE',#user.username,#user.username)")
	@GetMapping("/getnamebycode")
	public String getbycode(@PathParam(value = "code") int code,@CurrentUser OikosUserDetails user) {
		return serviceAdd.getByCode(code);
	}
	@PreAuthorize("@addTypeService.canDo('FIND_ALL',#user.username,#user.username)")
	@GetMapping("/findalltypes")
	public List<MyPropertyType> findalltypes(@CurrentUser OikosUserDetails user) {
		return serviceAdd.findall();
	}
	@PreAuthorize("@addTypeService.canDo('GET_PROPERTY_BY_TYPE',#user.username,#user.username)")
	@GetMapping("/type/{id}")
	public MyPropertyType getPropertyByType(@PathVariable("id") String id,@CurrentUser OikosUserDetails user) {
		return proptypeRepo.findById(id).orElseThrow(() -> new javax.persistence.EntityNotFoundException(
				"Could not found property type with id " + id));
	}

	
}
