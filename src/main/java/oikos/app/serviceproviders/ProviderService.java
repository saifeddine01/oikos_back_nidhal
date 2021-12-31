package oikos.app.serviceproviders;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import oikos.app.common.exceptions.EntityNotFoundException;
import oikos.app.common.models.Address;
import oikos.app.common.utils.Authorizable;
import oikos.app.serviceproviders.dtos.EditCompanyRequest;
import oikos.app.serviceproviders.dtos.ServiceProviderSignupRequest;
import oikos.app.serviceproviders.exceptions.SIRETAlreadyUsedException;
import oikos.app.serviceproviders.models.Collaborator;
import oikos.app.serviceproviders.models.ServiceCompany;
import oikos.app.serviceproviders.models.ServiceOwner;
import oikos.app.serviceproviders.repos.CollaboratorRepo;
import oikos.app.serviceproviders.repos.ServiceCompanyRepo;
import oikos.app.serviceproviders.repos.ServiceOwnerRepo;
import oikos.app.users.Role;
import oikos.app.users.UserRepo;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static oikos.app.common.utils.AddressUtils.editAddressFromDTO;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProviderService implements
  Authorizable<ProviderService.ProviderMethods> {
  private final UserRepo userRepo;
  private final ServiceCompanyRepo companyRepo;
  private final ServiceOwnerRepo ownerRepo;
  private final CollaboratorRepo collaboratorRepo;
  private final ModelMapper mapper;

  @ToString
  enum ProviderMethods {
    APPROVE_COMPANY(Names.APPROVE_COMPANY),
    REJECT_COMPANY(Names.REJECT_COMPANY),
    GET_COMPANY(Names.GET_COMPANY),
    GET_UNAPPROVED_COMPANIES(Names.GET_UNAPPROVED_COMPANIES),
    GET_COMPANIES(Names.GET_COMPANIES),
    EDIT_COMPANY(Names.EDIT_COMPANY),
    GET_MY_COMPANY(Names.GET_MY_COMPANY);
    private final String label;

    ProviderMethods(String label) {
      this.label = label;
    }

    public static class Names {
      public static final String APPROVE_COMPANY = "APPROVE_COMPANY";
      public static final String REJECT_COMPANY = "REJECT_COMPANY";
      public static final String GET_COMPANY = "GET_COMPANY";
      public static final String GET_UNAPPROVED_COMPANIES = "GET_UNAPPROVED_COMPANIES";
      public static final String GET_COMPANIES = "GET_COMPANIES";
      public static final String EDIT_COMPANY = "EDIT_COMPANY";
      public static final String GET_MY_COMPANY = "GET_MY_COMPANY";

      private Names() {}
    }
  }

  public ServiceCompany createCompany(ServiceProviderSignupRequest req) {
    log.info("Creating company {}", req);
    if (companyRepo.existsBySIRET(req.getSIRET())) {
      throw new SIRETAlreadyUsedException(
          "SIRET " + req.getSIRET() + " is already taken in OIKOS.");
    }
    return ServiceCompany.builder()
        .address(
            Address.builder()
                .departmentIdentifier(req.getDepartmentIdentifier())
                .street(req.getStreet())
                .zipCode(req.getZipCode())
                .build())
        .isValidated(false)
        .RIB(req.getRIB())
        .SIRET(req.getSIRET())
        .name(req.getName())
        .build();
  }

  public void saveCompanyAndOwner(ServiceOwner owner, ServiceCompany company) {
    log.info(
        "Saving company with SIRET {} and Owner {}", company.getSIRET(), owner.getUser().getId());
    ownerRepo.save(owner);
    saveCompany(company);
  }

  public ServiceCompany saveCompany(ServiceCompany company) {
    log.info("Saving company with SIRET {} ", company.getSIRET());
    return companyRepo.save(company);
  }

  public ServiceCompany getCompany(String companyID) {
    log.info("Getting company {}", companyID);
    return companyRepo
        .findById(companyID)
        .orElseThrow(() -> new EntityNotFoundException(ServiceCompany.class, companyID));
  }

  public Collaborator saveCollaborator(Collaborator collaborator) {
    log.info("Saving Collaborator {}", collaborator.getUser().getId());
    return collaboratorRepo.save(collaborator);
  }

  public ServiceCompany approveCompany(String companyID) {
    log.info("Approving company {}", companyID);
    var company = getCompany(companyID);
    company.setValidated(true);
    return saveCompany(company);
  }

  public ServiceCompany rejectCompany(String companyID) {
    log.info("Rejecting company {}", companyID);
    var company = getCompany(companyID);
    company.setValidated(false);
    return saveCompany(company);
  }

  public ServiceCompany getCompanyForUser(String userID) {
    log.info("Getting company of owner{}", userID);
    return companyRepo
        .findByOwnerID(userID)
        .orElseThrow(() -> new EntityNotFoundException(ServiceCompany.class, userID));
  }
  public String getCompanyIDForUser(String userID) {
    log.info("Getting company ID of owner{}", userID);
    return companyRepo
      .findByOwnerID(userID)
      .orElseThrow(() -> new EntityNotFoundException(ServiceCompany.class, userID)).getId();
  }

  public ServiceCompany editCompany(String companyID, EditCompanyRequest req) {
    log.info("Editing company {} with {}", companyID, req);
    var company = getCompany(companyID);
    mapper.map(req, company);
    if (company.getAddress() == null) {
      company.setAddress(new Address());
    }
    editAddressFromDTO(company.getAddress(), req.getAddress());
    return saveCompany(company);
  }

  public Page<ServiceCompany> getAllCompanies(Pageable paging) {
    log.info("Getting page {} of all companies", paging.getPageNumber());
    return companyRepo.getAllCompaniesByActivationStatus(true, paging);
  }

  public Page<ServiceCompany> getAllUnapprovedCompanies(Pageable paging) {
    log.info("Getting page {} of unapproved companies", paging.getPageNumber());
    return companyRepo.getAllCompaniesByActivationStatus(false, paging);
  }

  @Override public boolean canDo(ProviderMethods methodName, String userID,
    String objectID) {
    return switch(methodName){
      case GET_COMPANY,GET_MY_COMPANY,GET_COMPANIES -> true;
      case APPROVE_COMPANY,REJECT_COMPANY,GET_UNAPPROVED_COMPANIES -> userRepo.getOne(userID).getRoles().contains(
        Role.ADMIN);
      case EDIT_COMPANY -> getCompany(objectID).getServiceOwner().getId().equals(userID);
    };
  }
}
