package oikos.app.oikosservices;

import lombok.AllArgsConstructor;
import oikos.app.common.responses.DoneResponse;
import oikos.app.common.utils.Monitor;
import oikos.app.security.CurrentUser;
import oikos.app.security.OikosUserDetails;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

import static oikos.app.common.utils.NanoIDGenerator.NANOID_SIZE;

/**
 * Created by Mohamed Haamdi on 26/06/2021
 */
@RestController
@AllArgsConstructor
@Validated
@Monitor
public class OikosServiceController {
    private final OikosServiceService service;
    private final ModelMapper modelMapper;

    @PreAuthorize(
            "@oikosServiceService.canDo('ADD_SERVICE_TO_COMPANY',#user.username,#req.companyID)")
    @PostMapping("/services")
    public OikosServiceResponse addServiceToCompany(
            @Validated @RequestBody CreateServiceRequest req, @CurrentUser OikosUserDetails user) {
        var data = service.createService(req);
        return modelMapper.map(data, OikosServiceResponse.class);
    }

    @PreAuthorize("@oikosServiceService.canDo('GET_SERVICES_BY_COMPANY',#user.username,null)")
    @GetMapping("/providercompanies/{companyID}/services")
    public Page<OikosServiceResponse> getServicesByCompany(
            @CurrentUser OikosUserDetails user,
            @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String companyID,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
        Pageable paging = PageRequest.of(page, size);
        var data = service.getServicesByCompany(companyID, paging);
        return data.map(item -> modelMapper.map(item, OikosServiceResponse.class));
    }

    @PreAuthorize("@oikosServiceService.canDo('GET_SERVICES_FOR_MY_COMPANY',#user.username,null)")
    @GetMapping("/providercompanies/me/services")
    public Page<OikosServiceResponse> getServicesForMyCompany(
            @CurrentUser OikosUserDetails user,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
        Pageable paging = PageRequest.of(page, size);
        var data = service.getServicesForMyCompany(user.getUsername(), paging);
        return data.map(item -> modelMapper.map(item, OikosServiceResponse.class));
    }

    @PreAuthorize("@oikosServiceService.canDo('GET_SERVICES_BY_TYPE',#user.username,null)")
    @GetMapping("/services")
    public Page<OikosServiceResponse> getServicesByType(
            @CurrentUser OikosUserDetails user,
            @RequestParam ServiceType serviceType,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
        Pageable paging = PageRequest.of(page, size);
        var data = service.getServicesByType(serviceType, paging);
        return data.map(item -> modelMapper.map(item, OikosServiceResponse.class));
    }

    @PreAuthorize("@oikosServiceService.canDo('EDIT_SERVICE',#user.username,#serviceID)")
    @PutMapping("/services/{serviceID}")
    public OikosServiceResponse editService(
            @Validated @RequestBody EditServiceRequest req,
            @CurrentUser OikosUserDetails user,
            @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String serviceID) {
        var data = service.editService(serviceID, req);
        return modelMapper.map(data, OikosServiceResponse.class);
    }

    @PreAuthorize("@oikosServiceService.canDo('DELETE_SERVICE',#user.username,#serviceID)")
    @DeleteMapping("/services/{serviceID}")
    public DoneResponse deleteService(
            @CurrentUser OikosUserDetails user,
            @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String serviceID) {
        service.deleteService(serviceID);
        return new DoneResponse("Service " + serviceID + "has been deleted");
    }

    @PreAuthorize("@oikosServiceService.canDo('GET_SERVICE',#user.username,#serviceID)")
    @GetMapping("/services/{serviceID}")
    public OikosServiceResponse getService(
            @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String serviceID,
            @CurrentUser OikosUserDetails user
    ) {
        var data = service.getService(serviceID);
        return modelMapper.map(data, OikosServiceResponse.class);
    }
}
