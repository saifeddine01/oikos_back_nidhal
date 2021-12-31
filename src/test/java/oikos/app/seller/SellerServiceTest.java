package oikos.app.seller;

import oikos.app.common.exceptions.EntityNotFoundException;
import oikos.app.common.models.Address;
import oikos.app.common.request.AddressDTO;
import oikos.app.users.Civility;
import oikos.app.users.MaritalStatus;
import oikos.app.users.Role;
import oikos.app.users.User;
import oikos.app.users.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static oikos.app.seller.SellerService.SellerMethods.Names.GET_ALL_SELLERS;
import static oikos.app.seller.SellerService.SellerMethods.Names.UPDATE_SELLER_PROFILE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Created by Mohamed Haamdi on 28/04/2021. */
@ExtendWith(MockitoExtension.class)
class SellerServiceTest {

  @Mock private UserRepo mockUserRepo;
  @Mock private SellerRepo mockSellerRepo;
  @Mock private ModelMapper mockMapper;

  private SellerService sellerServiceUnderTest;

  @BeforeEach
  void setUp() {
    sellerServiceUnderTest = new SellerService(mockUserRepo, mockSellerRepo, mockMapper);
  }

  @Test
  void testUpdateMySellerProfile() {
    // Setup
    final UpdateSellerRequest dto = new UpdateSellerRequest();
    dto.setBirthDate(LocalDate.of(2020, 1, 1));
    dto.setMaritalStatus(MaritalStatus.CELIBATAIRE);
    dto.setCivility(Civility.MONSIEUR);
    dto.setAddress(
        AddressDTO.builder().street("street").zipCode("zipCode").departmentIdentifier(0).build());
    var id = new User("id");
    id.setRoles(new HashSet<>());
    when(mockUserRepo.getOne("id")).thenReturn(id);
    when(mockUserRepo.save(id)).thenReturn(id);

    // Configure SellerRepo.getByUserID(...).
    final Optional<Seller> seller =
        Optional.of(
            Seller.builder()
                .id("id")
                .birthDate(LocalDate.of(2020, 1, 1))
                .maritalStatus(MaritalStatus.CELIBATAIRE)
                .civility(Civility.MONSIEUR)
                .address(new Address("street", "zipCode", 0))
                .build());
    when(mockSellerRepo.getByUserID("id")).thenReturn(seller);

    // Run the test
    final User result = sellerServiceUnderTest.updateMySellerProfile(dto, "id");

    // Verify the results
    verify(mockMapper).map(eq(dto), any(Seller.class));
    assertThat(result).isNotNull();
    assertThat(result.getSellerProfile().getMaritalStatus()).isEqualTo(dto.getMaritalStatus());
  }

  @Test
  void testUpdateMySellerProfile_SellerRepoReturnsAbsent() {
    // Setup
    final UpdateSellerRequest dto = new UpdateSellerRequest();
    dto.setBirthDate(LocalDate.of(2020, 1, 1));
    dto.setMaritalStatus(MaritalStatus.CELIBATAIRE);
    dto.setCivility(Civility.MONSIEUR);
    dto.setAddress(
        AddressDTO.builder().street("street").zipCode("zipCode").departmentIdentifier(0).build());

    var id = new User("id");
    id.setRoles(new HashSet<>());
    when(mockUserRepo.getOne("id")).thenReturn(id);
    when(mockUserRepo.save(any(User.class))).thenReturn(id);
    when(mockSellerRepo.getByUserID("id")).thenReturn(Optional.empty());

    // Run the test
    final User result = sellerServiceUnderTest.updateMySellerProfile(dto, "id");

    // Verify the results
    verify(mockMapper).map(eq(dto), any(Seller.class));
    assertThat(result.getRoles()).contains(Role.SELLER);
  }

  @Test
  void testUpdateSellerProfile() {
    // Setup
    final UpdateSellerRequest dto = new UpdateSellerRequest();
    dto.setBirthDate(LocalDate.of(2020, 1, 1));
    dto.setMaritalStatus(MaritalStatus.CELIBATAIRE);
    dto.setCivility(Civility.MONSIEUR);
    dto.setAddress(
        AddressDTO.builder().street("street").zipCode("zipCode").departmentIdentifier(0).build());

    User id = new User("id");
    id.setRoles(new HashSet<>());
    when(mockUserRepo.findById("id")).thenReturn(Optional.of(id));
    when(mockUserRepo.save(any(User.class))).thenReturn(id);

    // Configure SellerRepo.getByUserID(...).
    /* final Optional<Seller> seller =
    Optional.of(
        new Seller(
            LocalDate.of(2020, 1, 1),
            id,
            MaritalStatus.CELIBATAIRE,
            Civility.MONSIEUR,
            new Address("street", "zipCode", 0)));*/
    when(mockSellerRepo.getByUserID("id")).thenReturn(Optional.empty());

    // Run the test
    final User result = sellerServiceUnderTest.updateSellerProfile("id", dto);

    // Verify the results
    verify(mockMapper).map(eq(dto), any(Seller.class));
  }

  @Test
  void testUpdateSellerProfile_UserRepoFindByIdReturnsAbsent() {
    // Setup
    final UpdateSellerRequest dto = new UpdateSellerRequest();

    when(mockUserRepo.findById("id")).thenReturn(Optional.empty());

    // Verify the results
    assertThatThrownBy(() -> sellerServiceUnderTest.updateSellerProfile("id", dto))
        .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void testUpdateSellerProfile_SellerRepoReturnsAbsent() {
    // Setup
    final UpdateSellerRequest dto = new UpdateSellerRequest();
    dto.setBirthDate(LocalDate.of(2020, 1, 1));
    dto.setMaritalStatus(MaritalStatus.CELIBATAIRE);
    dto.setCivility(Civility.MONSIEUR);
    dto.setAddress(
        AddressDTO.builder().street("street").zipCode("zipCode").departmentIdentifier(0).build());

    var user = new User("id");
    user.setRoles(new HashSet<>());
    when(mockUserRepo.findById("id")).thenReturn(Optional.of(user));
    when(mockUserRepo.save(any(User.class))).thenReturn(user);
    when(mockSellerRepo.getByUserID("id")).thenReturn(Optional.empty());

    // Run the test
    final User result = sellerServiceUnderTest.updateSellerProfile("id", dto);

    // Verify the results
    verify(mockMapper).map(eq(dto), any(Seller.class));
    assertThat(user.getRoles()).contains(Role.SELLER);
  }

  @Test
  void testCanUpdateMySellerProfile() {
    // Run the test
    final boolean result =
        sellerServiceUnderTest.canDo(
            SellerService.SellerMethods.UPDATE_MY_SELLER_PROFILE, "id", "objectID");

    // Verify the results
    assertThat(result).isTrue();
  }

  @ParameterizedTest
  @EnumSource(
      value = SellerService.SellerMethods.class,
      names = {UPDATE_SELLER_PROFILE, GET_ALL_SELLERS})
  void testCanUpdateSellerProfileGetAllSellerGetSellerInfoBySellerID(
      SellerService.SellerMethods method) {
    // Given
    var u = new User("id");
    u.setRoles(Set.of(Role.ADMIN));
    var u2 = new User("id2");
    u2.setRoles(Set.of(Role.BUYER));
    when(mockUserRepo.getOne("id")).thenReturn(u);
    when(mockUserRepo.getOne("id2")).thenReturn(u2);
    // when
    boolean canDo = sellerServiceUnderTest.canDo(method, u.getId(), any());
    boolean cantDo = sellerServiceUnderTest.canDo(method, u2.getId(), any());
    // then
    assertThat(canDo).isTrue();
    assertThat(cantDo).isFalse();
  }
}
