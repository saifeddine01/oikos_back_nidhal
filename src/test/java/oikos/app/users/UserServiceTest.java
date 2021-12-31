package oikos.app.users;

import oikos.app.buyer.BuyerInfoResponse;
import oikos.app.common.configurations.AppProperties;
import oikos.app.departements.Department;
import oikos.app.departements.DepartmentService;
import oikos.app.common.exceptions.EntityNotFoundException;
import oikos.app.common.models.Address;
import oikos.app.common.models.FileInfo;
import oikos.app.security.AuthProvider;
import oikos.app.security.SecurityService;
import oikos.app.security.exceptions.UserAlreadyExistException;
import oikos.app.seller.SellerInfoResponse;
import oikos.app.common.services.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) class UserServiceTest {

  @Mock private ModelMapper mockMapper;
  @Mock private UserRepo mockUserRepo;
  @Mock private SecurityService mockSecurityService;
  @Mock private DepartmentService departmentService;
  @Mock private UserProfileFileRepo fileRepository;
  @Mock private FileService fileService;
  @Mock private AppProperties appProperties;

  private UserService userServiceUnderTest;

  @BeforeEach void setUp() {
    userServiceUnderTest =
      new UserService(mockMapper, mockUserRepo, mockSecurityService,
         fileRepository, fileService, appProperties);
  }

  @Test void testUpdateMe() {
    // Setup
    final UpdateUserRequest dto =
      new UpdateUserRequest("firstName", "lastName", "password", "email",
        "phoneNumber");
    final var expectedResult = new User("id");
    User id = new User("id");
    id.setEmail("a");
    id.setPhoneNumber("b");
    when(mockUserRepo.getOne("id")).thenReturn(id);
    when(mockSecurityService.doUpdatePassword(eq(
      new UpdateUserRequest("firstName", "lastName", "password", "email",
        "phoneNumber")), any(User.class))).thenReturn(new User("id"));
    when(mockUserRepo.save(any(User.class))).thenReturn(new User("id"));
    when(mockUserRepo.existsByPhoneNumber(dto.getPhoneNumber()))
      .thenReturn(false);
    when(mockUserRepo.existsByEmailIgnoreCase(dto.getEmail()))
      .thenReturn(false);
    // Run the test
    final var result = userServiceUnderTest.updateMe(dto, "id");

    // Verify the results
    assertThat(result.getId()).isEqualTo(expectedResult.getId());
    verify(mockMapper).map(eq(
      new UpdateUserRequest("firstName", "lastName", "password", "email",
        "phoneNumber")), any(User.class));
  }

  @Test void testUpdateMe_UserRepoPhoneNumberAlreadyInuse() {
    // Setup
    final UpdateUserRequest dto =
      new UpdateUserRequest("firstName", "lastName", "password", "email",
        "phoneNumber");
    User id = new User("id");
    id.setPhoneNumber("a");
    when(mockUserRepo.getOne("id")).thenReturn(id);
    // Verify the results
    when(mockUserRepo.existsByPhoneNumber(dto.getPhoneNumber()))
      .thenReturn(true);
    assertThatThrownBy(() -> userServiceUnderTest.updateMe(dto, "id"))
      .isInstanceOf(UserAlreadyExistException.class);
  }

  @Test void testUpdateMe_UserRepoEmailAlreadyInUse() {
    // Setup
    final UpdateUserRequest dto =
      new UpdateUserRequest("firstName", "lastName", "password", "email",
        "phoneNumber");
    User id = new User("id");
    id.setPhoneNumber("a");
    id.setEmail("b");
    when(mockUserRepo.getOne("id")).thenReturn(id);
    // Verify the results
    when(mockUserRepo.existsByPhoneNumber(dto.getPhoneNumber()))
      .thenReturn(false);
    when(mockUserRepo.existsByEmailIgnoreCase(dto.getEmail())).thenReturn(true);
    assertThatThrownBy(() -> userServiceUnderTest.updateMe(dto, "id"))
      .isInstanceOf(UserAlreadyExistException.class);
  }

  @Test void updatePicture_PictureNotPresent() throws IOException {
    //given
    when(fileRepository.getProfilePictureForUser("id"))
      .thenReturn(Optional.empty());
    final byte[] bytes = "SampleData".getBytes(StandardCharsets.UTF_8);
    final MockMultipartFile file = new MockMultipartFile("imageID", bytes);
    final var user = new User("id");
    final FileInfo fileInfo = new FileInfo("imageID", "imageID.jpg", file);
    when(fileService.uploadFile(any(), any())).thenReturn(fileInfo);
    when(mockUserRepo.getOne("id")).thenReturn(user);
    when(fileRepository.save(any()))
      .thenAnswer(AdditionalAnswers.returnsFirstArg());
    when(mockUserRepo.save(any()))
      .thenAnswer(AdditionalAnswers.returnsFirstArg());
    //when
    var res = userServiceUnderTest.updatePicture(user.getId(), file);
    //then
    assertThat(res.getUser()).isEqualTo(user);
    assertThat(res.getFileName()).isEqualTo(fileInfo.getNewName());
    assertThat(res.getSize()).isEqualTo(file.getSize());
  }

  @Test void updatePicture_PicturePresent() throws IOException {
    //given
    final var user = new User("id");
    final UserProfileFile profileFile =
      UserProfileFile.builder().user(user).build();
    when(fileRepository.getProfilePictureForUser("id"))
      .thenReturn(Optional.of(profileFile));
    final byte[] bytes = "SampleData".getBytes(StandardCharsets.UTF_8);
    final MockMultipartFile file = new MockMultipartFile("imageID", bytes);
    final FileInfo fileInfo = new FileInfo("imageID", "imageID.jpg", file);
    when(fileService.uploadFile(any(), any())).thenReturn(fileInfo);
    when(mockUserRepo.getOne("id")).thenReturn(user);
    when(fileRepository.save(any()))
      .thenAnswer(AdditionalAnswers.returnsFirstArg());
    when(mockUserRepo.save(any()))
      .thenAnswer(AdditionalAnswers.returnsFirstArg());
    //when
    var res = userServiceUnderTest.updatePicture(user.getId(), file);
    //then
    assertThat(res.getUser()).isEqualTo(user);
    verify(fileRepository).delete(profileFile);
  }

  @Test void deletePicture_PictureNotPresent() {
    User user = User.builder().id("id")
      .userProfileFile(UserProfileFile.builder().id("imageID").build()).build();
    when(fileRepository.getProfilePictureForUser(user.getId()))
      .thenReturn(Optional.empty());
    when(mockUserRepo.getOne(user.getId())).thenReturn(user);
    when(mockUserRepo.save(any()))
      .thenAnswer(AdditionalAnswers.returnsFirstArg());
    //when
    userServiceUnderTest.deleteProfilePicture("id");
    //then
    ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);
    verify(mockUserRepo).save(argument.capture());
    assertThat(argument.getValue().getUserProfileFile()).isNull();
  }

  @Test void loadProfilePicture_NotFound() {
    when(fileRepository.getProfilePictureForUser("id"))
      .thenReturn(Optional.empty());
    assertThatThrownBy(() -> userServiceUnderTest.loadProfilePicture("id"))
      .isInstanceOf(EntityNotFoundException.class);
  }

  @Test void loadProfilePicture() {
    when(fileRepository.getProfilePictureForUser("id"))
      .thenReturn(Optional.of(UserProfileFile.builder().build()));
    final var multipartFile = new MockMultipartFile("filename", (byte[]) null);
    when(fileService.loadResource(any(), any()))
      .thenReturn(multipartFile.getResource());
    //when
    var res = userServiceUnderTest.loadProfilePicture("id");
    //then
    assertThat(res).isEqualTo(multipartFile.getResource());
  }
}
