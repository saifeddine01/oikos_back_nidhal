package oikos.app.security;

import oikos.app.common.apis.EmailService;
import oikos.app.common.apis.SMSService;
import oikos.app.common.configurations.AppProperties;
import oikos.app.common.exceptions.EntityNotFoundException;
import oikos.app.common.responses.DoneResponse;
import oikos.app.security.dtos.ForgotPasswordRequest;
import oikos.app.security.dtos.ResetPasswordRequest;
import oikos.app.security.exceptions.NoUserAssociatedException;
import oikos.app.security.exceptions.OAuth2AuthenticationProcessingException;
import oikos.app.security.exceptions.TokenNotValidException;
import oikos.app.seller.SellerBySecretarySignupRequest;
import oikos.app.seller.SellerSignupRequest;
import oikos.app.serviceproviders.ProviderService;
import oikos.app.serviceproviders.dtos.CollaboratorSignupRequest;
import oikos.app.serviceproviders.dtos.ServiceProviderSignupRequest;
import oikos.app.serviceproviders.exceptions.CompanyNotValidatedException;
import oikos.app.serviceproviders.models.ServiceCompany;
import oikos.app.serviceproviders.models.ServiceOwner;
import oikos.app.users.Role;
import oikos.app.users.SigninRequest;
import oikos.app.users.SignupRequest;
import oikos.app.users.UpdateUserRequest;
import oikos.app.users.User;
import oikos.app.users.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityServiceTest {

  @Mock private PasswordEncoder mockEncoder;
  @Mock private AuthenticationManager mockAuthenticationManager;
  @Mock private TokenProvider mockTokenProvider;
  @Mock private EmailService mockEmailService;
  @Mock private SMSService mockSmsService;
  @Mock private VerificationTokenRepo mockTokenRepo;
  @Mock private ModelMapper mockMapper;
  @Mock private UserRepo mockUserRepo;
  @Mock private ProviderService providerService;
  @Mock private BlacklistedJWTRepo blacklistedJWTRepo;
  @Mock private AppProperties appProperties;

  private SecurityService securityServiceUnderTest;

  @BeforeEach
  void setUp() {
    securityServiceUnderTest =
        new SecurityService(
            mockEncoder,
            mockAuthenticationManager,
            mockTokenProvider,
            providerService,
            mockEmailService,
            mockSmsService,
            mockTokenRepo,
            mockMapper,
            mockUserRepo,
            blacklistedJWTRepo,
            appProperties);
  }

  @Test
  void testRegisterNewBuyer() {
    // Setup
    final SignupRequest req = new SignupRequest();
    req.setFirstName("firstName");
    req.setLastName("lastName");
    req.setPassword("password");
    req.setEmail("email");
    req.setPhoneNumber("phoneNumber");

    when(mockUserRepo.existsByPhoneNumber("phoneNumber")).thenReturn(false);
    when(mockUserRepo.existsByEmailIgnoreCase("email")).thenReturn(false);
    when(mockMapper.map(req, User.class)).thenReturn(new User("id"));
    when(mockEncoder.encode("password")).thenReturn("result");
    when(mockUserRepo.save(any(User.class))).thenReturn(new User("id"));

    // Configure VerificationTokenRepo.save(...).
    final VerificationToken verificationToken =
        new VerificationToken("token", Instant.now(), new User("id"), VerificationTokenType.SMS);
    when(mockTokenRepo.save(any(VerificationToken.class))).thenReturn(verificationToken);

    // Run the test
    securityServiceUnderTest.registerNewBuyer(req);

    // Verify the results
    verify(mockEmailService)
        .sendUserVerificationEmail(any(User.class), any(VerificationToken.class));
  }

  @Test
  void testRegisterNewSeller() {
    // Setup
    final SellerSignupRequest req = new SellerSignupRequest();
    req.setBirthDate(LocalDate.of(2020, 1, 1));
    req.setStreet("street");
    req.setZipCode("zipCode");
    req.setDepartmentIdentifier(0);
    req.setPhoneNumber("phoneNumber");
    req.setEmail("email");
    req.setPassword("password");

    when(mockUserRepo.existsByPhoneNumber("phoneNumber")).thenReturn(false);
    when(mockUserRepo.existsByEmailIgnoreCase("email")).thenReturn(false);
    when(mockMapper.map(any(), eq(User.class))).thenReturn(new User("id"));
    when(mockEncoder.encode("password")).thenReturn("result");
    when(mockUserRepo.save(any(User.class))).thenReturn(new User("id"));

    // Configure VerificationTokenRepo.save(...).
    final VerificationToken verificationToken =
        new VerificationToken("token", Instant.now(), new User("id"), VerificationTokenType.SMS);
    when(mockTokenRepo.save(any(VerificationToken.class))).thenReturn(verificationToken);

    // Run the test
    securityServiceUnderTest.registerNewSeller(req);

    // Verify the results
    verify(mockSmsService).sendUserVerificationSMS(any(User.class), any(VerificationToken.class));
  }

  @Test
  void testCreateSellerBySecretary() {
    final SellerBySecretarySignupRequest req = new SellerBySecretarySignupRequest();
    req.setBirthDate(LocalDate.of(2020, 1, 1));
    req.setStreet("street");
    req.setZipCode("zipCode");
    req.setDepartmentIdentifier(0);
    req.setPhoneNumber("phoneNumber");
    req.setEmail("email");

    when(mockUserRepo.existsByPhoneNumber("phoneNumber")).thenReturn(false);
    when(mockUserRepo.existsByEmailIgnoreCase("email")).thenReturn(false);
    when(mockMapper.map(any(), eq(User.class))).thenReturn(new User("id"));
    when(mockUserRepo.save(any(User.class))).thenReturn(new User("id"));

    // Configure VerificationTokenRepo.save(...).
    when(mockTokenRepo.save(any(VerificationToken.class)))
        .thenAnswer(AdditionalAnswers.returnsFirstArg());

    // Run the test
    securityServiceUnderTest.registerNewSellerBySecretary(req);

    // Verify the results
    verify(mockEmailService).sendPasswordResetEmail(any(User.class), any(VerificationToken.class));
  }

  @Test
  void testRegisterNewServiceProvider() {
    final ServiceProviderSignupRequest req =
        ServiceProviderSignupRequest.builder()
            .firstName("firstName")
            .lastName("lastName")
            .password("password")
            .email("email")
            .phoneNumber("phoneNumber")
            .RIB("RIB")
            .SIRET("SIRET")
            .build();

    when(mockUserRepo.existsByPhoneNumber("phoneNumber")).thenReturn(false);
    when(mockUserRepo.existsByEmailIgnoreCase("email")).thenReturn(false);
    final User id = new User("id");
    when(mockMapper.map(req, User.class)).thenReturn(id);
    when(mockEncoder.encode("password")).thenReturn("result");
    when(providerService.createCompany(req))
        .thenReturn(ServiceCompany.builder().RIB("RIB").SIRET("SIRET").build());
    when(mockUserRepo.save(any(User.class))).thenReturn(id);

    // Configure VerificationTokenRepo.save(...).
    final VerificationToken verificationToken =
        new VerificationToken("token", Instant.now(), id, VerificationTokenType.EMAIL);
    when(mockTokenRepo.save(any(VerificationToken.class))).thenReturn(verificationToken);

    // Run the test
    securityServiceUnderTest.registerNewServiceProvider(req);

    // Verify the results
    verify(mockEmailService)
        .sendUserVerificationEmail(any(User.class), any(VerificationToken.class));
  }

  @Test
  void testSignIn() {
    // Setup
    final SigninRequest dto = new SigninRequest("emailOrPhone", "password");
    when(mockAuthenticationManager.authenticate(any()))
        .thenReturn(new UsernamePasswordAuthenticationToken(null, null));
    when(mockTokenProvider.generateToken(any())).thenReturn("result");

    // Run the test
    final var result = securityServiceUnderTest.signIn(dto);

    // Verify the results
    assertThat(result).isEqualTo("result");
  }

  @Test
  void testSignIn_AuthenticationManagerThrowsAuthenticationException() {
    // Setup
    final SigninRequest dto = new SigninRequest("emailOrPhone", "password");
    when(mockAuthenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(dto.getEmailOrPhone(), dto.getPassword())))
        .thenThrow(BadCredentialsException.class);

    // Verify the results
    assertThatThrownBy(() -> securityServiceUnderTest.signIn(dto))
        .isInstanceOf(AuthenticationException.class);
  }

  @Test
  void testRefreshToken() {
    // Setup
    final var expectedResult = "token";
    when(mockTokenProvider.generateToken(any())).thenReturn("token");

    // Run the test
    final var result = securityServiceUnderTest.refreshToken();

    // Verify the results
    assertThat(result).isEqualTo(expectedResult);
  }

  @Test
  void testConfirmUser() {
    // Configure VerificationTokenRepo.findByToken(...).
    User id = new User("id");
    id.setEnabled(false);
    final Optional<VerificationToken> verificationToken =
        Optional.of(
            new VerificationToken(
                "token", Instant.now().plus(Period.ofDays(1)), id, VerificationTokenType.SMS));
    when(mockTokenRepo.findByToken("confirmationCode")).thenReturn(verificationToken);

    when(mockUserRepo.save(any(User.class))).thenReturn(id);

    // Run the test
    securityServiceUnderTest.confirmUser("confirmationCode", VerificationTokenType.SMS);

    // Verify the results
    verify(mockTokenRepo).delete(any(VerificationToken.class));
    verify(mockUserRepo).save(any());
  }

  @Test
  void testConfirmUser_VerificationTokenRepoFindByTokenReturnsAbsent() {
    // Setup
    when(mockTokenRepo.findByToken("token")).thenReturn(Optional.empty());

    // Verify the results
    assertThatThrownBy(
            () -> securityServiceUnderTest.confirmUser("token", VerificationTokenType.SMS))
        .isInstanceOf(TokenNotValidException.class);
  }

  @Test
  void testResendUserConfirmationByEmail() {
    // Setup
    final DoneResponse expectedResult =
        new DoneResponse("Activation email for user email has been resent.");
    User id = new User("id");
    id.setEnabled(false);
    when(mockUserRepo.findIDByEmailOrPhoneNumber("email")).thenReturn(Optional.of(id.getId()));
    when(mockUserRepo.getOne(id.getId())).thenReturn(id);
    // Configure VerificationTokenRepo.save(...).
    final VerificationToken verificationToken =
        new VerificationToken("token", Instant.now(), id, VerificationTokenType.SMS);
    when(mockTokenRepo.save(any(VerificationToken.class))).thenReturn(verificationToken);

    // Run the test
    securityServiceUnderTest.resendUserConfirmationByEmail("email");

    // Verify the results
    verify(mockEmailService)
        .sendUserVerificationEmail(any(User.class), any(VerificationToken.class));
  }

  @Test
  void testResendUserConfirmationByEmail_UserRepoReturnsAbsent() {
    // Setup
    when(mockUserRepo.findIDByEmailOrPhoneNumber("email")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> securityServiceUnderTest.resendUserConfirmationByEmail("email"))
        .isInstanceOf(NoUserAssociatedException.class);
  }

  @Test
  void testForgotPassword() {
    // Setup
    final ForgotPasswordRequest dto = new ForgotPasswordRequest();
    dto.setEmail("email");
    User value = new User("id");
    value.setPassword("password");
    value.setProvider(AuthProvider.local);
    when(mockUserRepo.findIDByEmailOrPhoneNumber("email")).thenReturn(Optional.of(value.getId()));
    when(mockUserRepo.getOne(value.getId())).thenReturn(value);

    // Configure VerificationTokenRepo.save(...).
    final VerificationToken verificationToken =
        new VerificationToken("token", Instant.now(), new User("id"), VerificationTokenType.SMS);
    when(mockTokenRepo.save(any(VerificationToken.class))).thenReturn(verificationToken);

    // Run the test
    securityServiceUnderTest.forgotPassword(dto);

    // Verify the results
    verify(mockEmailService).sendPasswordResetEmail(any(User.class), any(VerificationToken.class));
  }

  @Test
  void testForgotPassword_UserRepoReturnsAbsent() {
    // Setup
    final ForgotPasswordRequest dto = new ForgotPasswordRequest();
    dto.setEmail("email");
    when(mockUserRepo.findIDByEmailOrPhoneNumber("email")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> securityServiceUnderTest.forgotPassword(dto))
        .isInstanceOf(NoUserAssociatedException.class);
  }

  @Test
  void testForgotPassword_UserIsSignedwithSocialNetwork() {
    // Setup
    final ForgotPasswordRequest dto = new ForgotPasswordRequest();
    dto.setEmail("email");
    var id = new User("id");
    id.setProvider(AuthProvider.google);
    when(mockUserRepo.findIDByEmailOrPhoneNumber("email")).thenReturn(Optional.of(id.getId()));
    when(mockUserRepo.getOne(id.getId())).thenReturn(id);

    // Run the test
    assertThatThrownBy(() -> securityServiceUnderTest.forgotPassword(dto))
        .isInstanceOf(OAuth2AuthenticationProcessingException.class);
  }

  @Test
  void testForgotPassword_UserHasNoPasswordYet() {
    // Setup
    final ForgotPasswordRequest dto = new ForgotPasswordRequest();
    dto.setEmail("email");
    var id = new User("id");
    id.setProvider(AuthProvider.local);
    when(mockUserRepo.findIDByEmailOrPhoneNumber("email")).thenReturn(Optional.of(id.getId()));
    when(mockUserRepo.getOne(id.getId())).thenReturn(id);

    // Run the test
    assertThatThrownBy(() -> securityServiceUnderTest.forgotPassword(dto))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void testResetPassword() {
    // Setup
    final ResetPasswordRequest dto = new ResetPasswordRequest();
    dto.setPassword("password");

    final DoneResponse expectedResult =
        new DoneResponse("Password reset for user null has been successful. Login now");

    // Configure VerificationTokenRepo.findByToken(...).
    final Optional<VerificationToken> verificationToken =
        Optional.of(
            new VerificationToken(
                "token",
                Instant.now().plus(Period.ofDays(1)),
                new User("id"),
                VerificationTokenType.PASSWORD_RESET));
    when(mockTokenRepo.findByToken("token")).thenReturn(verificationToken);

    when(mockEncoder.matches(any(), any())).thenReturn(false);
    when(mockEncoder.encode(any())).thenReturn("result");
    when(mockUserRepo.save(any(User.class))).thenReturn(new User("id"));
    final AppProperties.Auth t = new AppProperties.Auth();
    t.setTokenExpirationMsec(1);
    when(appProperties.getAuth()).thenReturn(t);
    // Run the test
    securityServiceUnderTest.resetPassword(dto, "token");

    // Verify the results
    verify(mockTokenRepo).delete(any(VerificationToken.class));
  }

  @Test
  void testResetPassword_VerificationTokenRepoFindByTokenReturnsAbsent() {
    // Setup
    final ResetPasswordRequest dto = new ResetPasswordRequest();
    dto.setPassword("password");
    final DoneResponse expectedResult = new DoneResponse("message");
    when(mockTokenRepo.findByToken("tokenCode")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> securityServiceUnderTest.resetPassword(dto, "tokenCode"))
        .isInstanceOf(TokenNotValidException.class);
  }

  @Test
  void testResendUserConfirmationBySMS() {
    // Setup
    final DoneResponse expectedResult =
        new DoneResponse("Activation SMS for number phonenumber has been resent.");
    User id = new User("id");
    id.setEnabled(false);
    when(mockUserRepo.findIDByEmailOrPhoneNumber("phonenumber"))
        .thenReturn(Optional.of(id.getId()));
    when(mockUserRepo.getOne(id.getId())).thenReturn(id);

    // Configure VerificationTokenRepo.save(...).
    final VerificationToken verificationToken =
        new VerificationToken("token", Instant.now(), id, VerificationTokenType.SMS);
    when(mockTokenRepo.save(any(VerificationToken.class))).thenReturn(verificationToken);

    // Run the test
    securityServiceUnderTest.resendUserConfirmationBySMS("phonenumber");

    // Verify the results
    verify(mockSmsService).sendUserVerificationSMS(any(User.class), any(VerificationToken.class));
  }

  @Test
  void testResendUserConfirmationBySMS_UserRepoReturnsAbsent() {
    // Setup
    when(mockUserRepo.findIDByEmailOrPhoneNumber("phonenumber")).thenReturn(Optional.empty());
    // Verify the results
    assertThatThrownBy(() -> securityServiceUnderTest.resendUserConfirmationBySMS("phonenumber"))
        .isInstanceOf(NoUserAssociatedException.class);
  }

  @Test
  void testDoUpdatePassword() {
    // Setup
    final UpdateUserRequest dto =
        new UpdateUserRequest("firstName", "lastName", "password", "email", "phoneNumber");
    final User user = new User("id");
    when(mockEncoder.encode("password")).thenReturn("result");
    final AppProperties.Auth t = new AppProperties.Auth();
    t.setTokenExpirationMsec(1);
    when(appProperties.getAuth()).thenReturn(t);
    // Run the test
    final User result = securityServiceUnderTest.doUpdatePassword(dto, user);

    // Verify the results
    assertThat(result.getPassword()).isEqualTo("result");
  }

  @Test
  void canCreateSellerBySecretary() {
    var secretary =
        User.builder().id("Secretary").roles(Set.of(Role.SECRETARY, Role.BUYER)).build();
    var normalUser = User.builder().id("user").roles(Set.of(Role.SELLER, Role.BUYER)).build();
    when(mockUserRepo.getOne(secretary.getId())).thenReturn(secretary);
    when(mockUserRepo.getOne(normalUser.getId())).thenReturn(normalUser);
    final var res1 =
        securityServiceUnderTest.canDo(
            SecurityService.SecurityMethods.REGISTER_NEW_SELLER_BY_SECRETARY,
            secretary.getId(),
            null);
    final var res2 =
        securityServiceUnderTest.canDo(
            SecurityService.SecurityMethods.REGISTER_NEW_SELLER_BY_SECRETARY,
            normalUser.getId(),
            null);
    assertThat(res1).isTrue().isNotEqualTo(res2);
  }

  @Test
  void CanRegisterNewCollaborator() {
    var normalUser = User.builder().id("user").roles(Set.of(Role.SELLER, Role.BUYER)).build();
    var owner = User.builder().id("owner").roles(Set.of(Role.PROVIDER)).build();
    var corp =
        ServiceCompany.builder()
            .id("corpID")
            .serviceOwner(ServiceOwner.builder().id(owner.getId()).build())
            .build();
    when(providerService.getCompany("corpID")).thenReturn(corp);
    when(mockUserRepo.getOne(normalUser.getId())).thenReturn(normalUser);
    when(providerService.getCompany("nonvalidID"))
        .thenThrow(new EntityNotFoundException(ServiceCompany.class, "nonvalidID"));
    final var res1 =
        securityServiceUnderTest.canDo(
            SecurityService.SecurityMethods.REGISTER_NEW_COLLABORATOR, owner.getId(), corp.getId());
    final var res2 =
        securityServiceUnderTest.canDo(
            SecurityService.SecurityMethods.REGISTER_NEW_SELLER_BY_SECRETARY,
            normalUser.getId(),
            corp.getId());
    assertThat(res1).isTrue();
    assertThat(res2).isFalse();
    assertThatThrownBy(
            () ->
                securityServiceUnderTest.canDo(
                    SecurityService.SecurityMethods.REGISTER_NEW_COLLABORATOR,
                    owner.getId(),
                    "nonvalidID"))
        .isInstanceOf(EntityNotFoundException.class);
  }

  @Nested
  class signupCollaborator {
    final CollaboratorSignupRequest req =
        CollaboratorSignupRequest.builder()
            .companyID("corpID")
            .phoneNumber("phoneNumber")
            .email("email")
            .build();
    final User owner = User.builder().id("owner").roles(Set.of(Role.PROVIDER)).build();
    final ServiceCompany corp =
        ServiceCompany.builder()
            .id("corpID")
            .serviceOwner(ServiceOwner.builder().id(owner.getId()).build())
            .collaborators(new ArrayList<>())
            .build();

    @Test
    void testCreateCollaboratorCompanyValidated() {
      corp.setValidated(true);
      when(providerService.getCompany("corpID")).thenReturn(corp);
      when(mockUserRepo.existsByPhoneNumber("phoneNumber")).thenReturn(false);
      when(mockUserRepo.existsByEmailIgnoreCase("email")).thenReturn(false);
      when(mockMapper.map(any(), eq(User.class))).thenReturn(new User("id"));
      when(mockUserRepo.save(any(User.class))).thenReturn(new User("id"));

      // Configure VerificationTokenRepo.save(...).
      when(mockTokenRepo.save(any(VerificationToken.class)))
          .thenAnswer(AdditionalAnswers.returnsFirstArg());

      // Run the test
      securityServiceUnderTest.registerNewCollaborator(req);

      // Verify the results
      verify(mockEmailService)
          .sendPasswordResetEmail(any(User.class), any(VerificationToken.class));
    }

    @Test
    void testCreateCollaboratorCompanyNotValidated() {

      when(providerService.getCompany("corpID")).thenReturn(corp);
      when(mockUserRepo.existsByPhoneNumber("phoneNumber")).thenReturn(false);
      when(mockUserRepo.existsByEmailIgnoreCase("email")).thenReturn(false);
      when(mockMapper.map(any(), eq(User.class))).thenReturn(new User("id"));
      when(mockUserRepo.save(any(User.class))).thenReturn(new User("id"));

      // Configure VerificationTokenRepo.save(...).
      when(mockTokenRepo.save(any(VerificationToken.class)))
          .thenAnswer(AdditionalAnswers.returnsFirstArg());

      // Run the test
      assertThatThrownBy(() -> securityServiceUnderTest.registerNewCollaborator(req))
          .isInstanceOf(CompanyNotValidatedException.class);
    }

    @Test
    void testCreateCollaboratorCompanyNotFound() {

      when(providerService.getCompany("corpID"))
          .thenThrow(new EntityNotFoundException(ServiceCompany.class, "corpID"));
      when(mockUserRepo.existsByPhoneNumber("phoneNumber")).thenReturn(false);
      when(mockUserRepo.existsByEmailIgnoreCase("email")).thenReturn(false);
      when(mockMapper.map(any(), eq(User.class))).thenReturn(new User("id"));
      when(mockUserRepo.save(any(User.class))).thenReturn(new User("id"));

      // Configure VerificationTokenRepo.save(...).
      when(mockTokenRepo.save(any(VerificationToken.class)))
          .thenAnswer(AdditionalAnswers.returnsFirstArg());
      // Run the test
      assertThatThrownBy(() -> securityServiceUnderTest.registerNewCollaborator(req))
          .isInstanceOf(EntityNotFoundException.class);
    }
  }
}
