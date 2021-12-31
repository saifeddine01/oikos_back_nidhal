package oikos.app.oikosservices;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import oikos.app.common.exceptions.EntityNotFoundException;
import oikos.app.common.utils.Authorizable;
import oikos.app.serviceproviders.ProviderService;

/** Created by Mohamed Haamdi on 26/06/2021 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OikosServiceService implements Authorizable<OikosServiceService.ServiceMethods> {
  private final OikosServiceRepo serviceRepo;
  private final ProviderService providerService;
  private final ModelMapper mapper;

  @ToString
  enum ServiceMethods {
    ADD_SERVICE_TO_COMPANY(Names.ADD_SERVICE_TO_COMPANY),
    GET_SERVICES_BY_COMPANY(Names.GET_SERVICES_BY_COMPANY),
    GET_SERVICES_FOR_MY_COMPANY(Names.GET_SERVICES_FOR_MY_COMPANY),
    GET_SERVICES_BY_TYPE(Names.GET_SERVICES_BY_TYPE),
    EDIT_SERVICE(Names.EDIT_SERVICE),
    DELETE_SERVICE(Names.DELETE_SERVICE),
    GET_SERVICE(Names.GET_SERVICE);
    private final String label;

    ServiceMethods(String label) {
      this.label = label;
    }

    public static class Names {
      public static final String ADD_SERVICE_TO_COMPANY = "ADD_SERVICE_TO_COMPANY";
      public static final String GET_SERVICES_BY_COMPANY = "GET_SERVICES_BY_COMPANY";
      public static final String GET_SERVICES_BY_TYPE = "GET_SERVICES_BY_TYPE";
      public static final String GET_SERVICES_FOR_MY_COMPANY = "GET_SERVICES_FOR_MY_COMPANY";
      public static final String EDIT_SERVICE = "EDIT_SERVICE";
      public static final String DELETE_SERVICE = "DELETE_SERVICE";
      public static final String GET_SERVICE = "GET_SERVICE";

      private Names() {}
    }
  }

  public OikosService createService(CreateServiceRequest req) {
    log.info("Adding service {}", req);
    var company = providerService.getCompany(req.getCompanyID());
    var data =
        OikosService.builder()
            .serviceCompany(company)
            .serviceType(req.getServiceType())
            .description(req.getDescription())
            .needsAppointment(req.isNeedsAppointment())
            .price(req.getPrice())
            .build();
    serviceRepo.save(data);
    company.getServices().add(data);
    providerService.saveCompany(company);
    return data;
  }

  public Page<OikosService> getServicesByCompany(String companyID, Pageable paging) {
    log.info("Getting services for company {}", companyID);
    return serviceRepo.getServicesByCompany(companyID, paging);
  }

  public OikosService editService(String serviceID, EditServiceRequest req) {
    log.info("Editing service {}", serviceID);
    var service = getService(serviceID);
    mapper.map(req, service);
    return serviceRepo.save(service);
  }

  public void deleteService(String serviceID) {
    log.info("Deleting service {}", serviceID);
    var service = getService(serviceID);
    service.getServiceCompany().getServices().remove(service);
    serviceRepo.delete(service);
    providerService.saveCompany(service.getServiceCompany());
  }

  public Page<OikosService> getServicesByType(ServiceType serviceType, Pageable paging) {
    log.info("Getting services by type {}", serviceType);
    return serviceRepo.getServicesByType(serviceType, paging);
  }

  public OikosService getService(String serviceID) {
    log.info("Getting service {}", serviceID);
    return serviceRepo
        .findById(serviceID)
        .orElseThrow(() -> new EntityNotFoundException(OikosService.class, serviceID));
  }

  @Override public boolean canDo(ServiceMethods methodName, String userID,
    String objectID) {
    return switch (methodName) {
      case GET_SERVICE,GET_SERVICES_BY_COMPANY,GET_SERVICES_BY_TYPE,GET_SERVICES_FOR_MY_COMPANY -> true;
      case ADD_SERVICE_TO_COMPANY -> providerService.getCompany(objectID).getServiceOwner().getId().equals(userID);
      case EDIT_SERVICE,DELETE_SERVICE -> getService(objectID).getServiceCompany().getServiceOwner().getId().equals(userID);
    };
  }

  public Page<OikosService> getServicesForMyCompany(String userID, Pageable paging) {
    var companyID = providerService.getCompanyIDForUser(userID);
    return getServicesByCompany(companyID,paging);
  }
}
