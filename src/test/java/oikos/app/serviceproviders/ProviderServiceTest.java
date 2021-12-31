package oikos.app.serviceproviders;

import oikos.app.common.exceptions.EntityNotFoundException;
import oikos.app.common.request.AddressDTO;
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
import oikos.app.users.User;
import oikos.app.users.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Created by Mohamed Haamdi on 26/06/2021 */
@ExtendWith(MockitoExtension.class)
class ProviderServiceTest {
  final Pageable paging = PageRequest.of(0, 10);
  @Mock UserRepo userRepo;
  @Mock ServiceCompanyRepo companyRepo;
  @Mock ServiceOwnerRepo ownerRepo;
  @Mock CollaboratorRepo collaboratorRepo;
  @Mock ModelMapper mapper;
  ProviderService underTest;

  @BeforeEach
  void setup() {
    underTest = new ProviderService(userRepo, companyRepo, ownerRepo, collaboratorRepo, mapper);
  }

  @Test
  void saveCompany() {
    final var company = new ServiceCompany();
    when(companyRepo.save(company)).thenReturn(company);
    underTest.saveCompany(company);
    verify(companyRepo).save(any());
  }

  @Test
  void saveCompanyAndOwner() {
    final var company = new ServiceCompany();
    final var owner = new ServiceOwner();
    owner.setUser(new User("ID"));
    when(companyRepo.save(company)).thenReturn(company);
    when(ownerRepo.save(owner)).thenReturn(owner);
    underTest.saveCompanyAndOwner(owner, company);
    verify(companyRepo).save(any());
    verify(ownerRepo).save(any());
  }

  @Test
  void getCompany() {
    String id = "id";
    String unvalidId = "unvalidId";
    when(companyRepo.findById(unvalidId)).thenReturn(Optional.empty());
    final ServiceCompany company = ServiceCompany.builder().id(id).build();
    when(companyRepo.findById(id)).thenReturn(Optional.of(company));
    var res = underTest.getCompany(id);
    assertThat(res).isEqualTo(company);
    assertThatThrownBy(() -> underTest.getCompany(unvalidId))
        .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void saveCollaborator() {
    Collaborator collaborator = new Collaborator();
    collaborator.setUser(new User("ID"));
    when(collaboratorRepo.save(collaborator)).thenReturn(collaborator);
    var res = underTest.saveCollaborator(collaborator);
    assertThat(res).isEqualTo(collaborator);
    verify(collaboratorRepo).save(any());
  }

  @Test
  void approveCompany() {
    when(companyRepo.findById("ID"))
        .thenReturn(Optional.of(ServiceCompany.builder().id("ID").build()));
    when(companyRepo.save(any())).thenAnswer(returnsFirstArg());
    var res = underTest.approveCompany("ID");
    assertThat(res.isValidated()).isTrue();
  }

  @Test
  void rejectCompany() {
    when(companyRepo.findById("ID"))
        .thenReturn(Optional.of(ServiceCompany.builder().id("ID").build()));
    when(companyRepo.save(any())).thenAnswer(returnsFirstArg());
    var res = underTest.rejectCompany("ID");
    assertThat(res.isValidated()).isFalse();
  }

  @Test
  void getCompanyForUser() {
    String id = "id";
    String unvalidId = "unvalidId";
    when(companyRepo.findByOwnerID(unvalidId)).thenReturn(Optional.empty());
    final ServiceCompany company =
        ServiceCompany.builder().serviceOwner(ServiceOwner.builder().id(id).build()).build();
    when(companyRepo.findByOwnerID(id)).thenReturn(Optional.of(company));
    var res = underTest.getCompanyForUser(id);
    assertThat(res.getServiceOwner().getId()).isEqualTo(id);
    assertThatThrownBy(() -> underTest.getCompanyForUser(unvalidId))
        .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void editCompany() {
    when(companyRepo.findById("ID"))
        .thenReturn(Optional.of(ServiceCompany.builder().id("ID").build()));
    when(companyRepo.save(any())).thenAnswer(returnsFirstArg());
    var dto = new EditCompanyRequest();
    dto.setAddress(AddressDTO.builder().build());
    underTest.editCompany("ID", dto);
    verify(mapper).map(any(EditCompanyRequest.class), any(ServiceCompany.class));
  }

  @Test
  void getAllCompanies() {
    underTest.getAllCompanies(paging);
    verify(companyRepo).getAllCompaniesByActivationStatus(true, paging);
  }

  @Test
  void getAllUnapprovedCompanies() {
    underTest.getAllUnapprovedCompanies(paging);
    verify(companyRepo).getAllCompaniesByActivationStatus(false, paging);
  }

  @Nested
  class createCompany {
    ServiceProviderSignupRequest req =
        ServiceProviderSignupRequest.builder()
            .SIRET("SIRET")
            .name("name")
            .departmentIdentifier(0)
            .RIB("RIB")
            .street("street")
            .zipCode("zipCode")
            .build();

    @Test
    void createCompanySIRETAlreadyUsed() {
      when(companyRepo.existsBySIRET(req.getSIRET())).thenReturn(true);
      assertThatThrownBy(() -> underTest.createCompany(req))
          .isInstanceOf(SIRETAlreadyUsedException.class);
    }

    @Test
    void createCompany() {
      when(companyRepo.existsBySIRET(req.getSIRET())).thenReturn(false);
      var res = underTest.createCompany(req);
      assertThat(res).isNotNull();
      assertThat(res.getSIRET()).isEqualTo(req.getSIRET());
      assertThat(res.getName()).isEqualTo(req.getName());
    }
  }

  @Nested
  class canDo {
    @ParameterizedTest
    @EnumSource(
        value = ProviderService.ProviderMethods.class,
        names = {
          ProviderService.ProviderMethods.Names.GET_COMPANY,
          ProviderService.ProviderMethods.Names.GET_MY_COMPANY,
          ProviderService.ProviderMethods.Names.GET_COMPANIES,
        })
    void catGetCompanyGetOwnCompanyGetAllCompanies(ProviderService.ProviderMethods method) {
      // When
      var res = underTest.canDo(method, "userID", "objectID");
      // Then
      assertThat(res).isTrue();
    }

    @ParameterizedTest
    @EnumSource(
        value = ProviderService.ProviderMethods.class,
        names = {
          ProviderService.ProviderMethods.Names.APPROVE_COMPANY,
          ProviderService.ProviderMethods.Names.REJECT_COMPANY,
          ProviderService.ProviderMethods.Names.GET_UNAPPROVED_COMPANIES
        })
    void canApproveRejectCompaniesListUnapprovedCompanies(ProviderService.ProviderMethods method) {
      // When
      final var adminID = new User("adminID");
      adminID.setRoles(Set.of(Role.ADMIN, Role.PROVIDER));
      when(userRepo.getOne("adminID")).thenReturn(adminID);
      final var userID = new User("userID");
      userID.setRoles(Set.of(Role.SELLER));
      when(userRepo.getOne("userID")).thenReturn(userID);
      var res1 = underTest.canDo(method, "adminID", null);
      var res2 = underTest.canDo(method, "userID", null);
      // Then
      assertThat(res1).isTrue();
      assertThat(res2).isFalse();
    }

    @Test
    void canEditCompany() {
      when(companyRepo.findById("id"))
          .thenReturn(
              Optional.of(
                  ServiceCompany.builder()
                      .serviceOwner(ServiceOwner.builder().id("userID").build())
                      .build()));
      var res1 = underTest.canDo(ProviderService.ProviderMethods.EDIT_COMPANY, "userID", "id");
      var res2 = underTest.canDo(ProviderService.ProviderMethods.EDIT_COMPANY, "diffUserID", "id");
      assertThat(res1).isTrue();
      assertThat(res2).isFalse();
    }
  }
}
