package oikos.app.oikosservices;

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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import oikos.app.common.exceptions.EntityNotFoundException;
import oikos.app.serviceproviders.ProviderService;
import oikos.app.serviceproviders.models.ServiceCompany;
import oikos.app.serviceproviders.models.ServiceOwner;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Created by Mohamed Haamdi on 26/06/2021 */
@ExtendWith(MockitoExtension.class)
class OikosServiceServiceTest {
  final Pageable paging = PageRequest.of(0, 10);
  @Mock OikosServiceRepo serviceRepo;
  @Mock ProviderService providerService;
  @Mock ModelMapper mapper;
  OikosServiceService underTest;

  @Test
  void createServiceTest() {
    when(providerService.getCompany("ID"))
        .thenReturn(ServiceCompany.builder().id("ID").services(new ArrayList<>()).build());
    when(serviceRepo.save(any())).thenAnswer(returnsFirstArg());
    var res =
        underTest.createService(
            CreateServiceRequest.builder().companyID("ID").price(BigDecimal.valueOf(15L)).build());
    assertThat(res).isNotNull();
    assertThat(res.getPrice()).isEqualTo(BigDecimal.valueOf(15L));
  }

  @Test
  void getServicesByCompany() {
    underTest.getServicesByCompany("id", paging);
    verify(serviceRepo).getServicesByCompany("id", paging);
  }

  @Test
  void getServicesByType() {
    underTest.getServicesByType(ServiceType.DIAGNOSTIC, paging);
    verify(serviceRepo).getServicesByType(ServiceType.DIAGNOSTIC, paging);
  }

  @Test
  void editService() {
    var req = EditServiceRequest.builder().build();
    when(serviceRepo.findById("ID")).thenReturn(Optional.of(OikosService.builder().build()));
    when(serviceRepo.save(any())).thenAnswer(returnsFirstArg());
    var res = underTest.editService("ID", req);
    assertThat(res).isNotNull();
    verify(mapper).map(any(EditServiceRequest.class), any(OikosService.class));
  }

  @Test
  void deleteService() {
    final var company = ServiceCompany.builder().build();
    final var service = OikosService.builder().id("ID").serviceCompany(company).build();
    company.setServices(new ArrayList<>());
    company.getServices().add(service);
    when(serviceRepo.findById("ID")).thenReturn(Optional.of(service));

    underTest.deleteService("ID");

    verify(serviceRepo).delete(service);
    verify(providerService).saveCompany(any());
    assertThat(company.getServices()).isEmpty();
  }

  @Test
  void getService() {
    String id = "id";
    String unvalidId = "unvalidId";
    when(serviceRepo.findById(unvalidId)).thenReturn(Optional.empty());
    final OikosService service = OikosService.builder().id(id).build();
    when(serviceRepo.findById(id)).thenReturn(Optional.of(service));
    var res = underTest.getService(id);
    assertThat(res).isEqualTo(service);
    assertThatThrownBy(() -> underTest.getService(unvalidId))
        .isInstanceOf(EntityNotFoundException.class);
  }

  @BeforeEach
  void setUp() {
    underTest = new OikosServiceService(serviceRepo, providerService, mapper);
  }

  @Nested
  class canDo {
    @ParameterizedTest
    @EnumSource(
        value = OikosServiceService.ServiceMethods.class,
        names = {
          OikosServiceService.ServiceMethods.Names.GET_SERVICE,
          OikosServiceService.ServiceMethods.Names.GET_SERVICES_BY_COMPANY,
          OikosServiceService.ServiceMethods.Names.GET_SERVICES_BY_TYPE,
        })
    void can_GET_SERVICE_GET_SERVICES_BY_COMPANY_GET_SERVICES_BY_TYPE(
        OikosServiceService.ServiceMethods method) {
      // When
      var res = underTest.canDo(method, "userID", "objectID");
      // Then
      assertThat(res).isTrue();
    }

    @ParameterizedTest
    @EnumSource(
        value = OikosServiceService.ServiceMethods.class,
        names = {
          OikosServiceService.ServiceMethods.Names.EDIT_SERVICE,
          OikosServiceService.ServiceMethods.Names.DELETE_SERVICE
        })
    void can_EDIT_SERVICE_DELETE_SERVICE(OikosServiceService.ServiceMethods method) {
      // When
      final OikosService service =
          OikosService.builder()
              .serviceCompany(
                  ServiceCompany.builder()
                      .serviceOwner(ServiceOwner.builder().id("ownerID").build())
                      .build())
              .build();
      when(serviceRepo.findById("ID")).thenReturn(Optional.of(service));
      var res = underTest.canDo(method, "userID", "ID");
      var res2 = underTest.canDo(method, "ownerID", "ID");
      // Then
      assertThat(res2).isTrue();
      assertThat(res).isFalse();
    }

    @Test
    void can_ADD_SERVICE_TO_COMPANY() {
      var corp =
          ServiceCompany.builder()
              .id("corpID")
              .serviceOwner(ServiceOwner.builder().id("userID").build())
              .build();
      when(providerService.getCompany("corpID")).thenReturn(corp);
      var res1 =
          underTest.canDo(
              OikosServiceService.ServiceMethods.ADD_SERVICE_TO_COMPANY, "userID", "corpID");
      var res2 =
          underTest.canDo(
              OikosServiceService.ServiceMethods.ADD_SERVICE_TO_COMPANY, "userID2", "corpID");

      assertThat(res1).isTrue();
      assertThat(res2).isFalse();
    }
  }
}
