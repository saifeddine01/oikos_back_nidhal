package oikos.app.security;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;

import oikos.app.buyer.Buyer;
import oikos.app.common.apis.EmailService;
import oikos.app.common.apis.SMSService;
import oikos.app.common.configurations.AppProperties;
import oikos.app.common.models.Address;
import oikos.app.common.utils.Authorizable;
import oikos.app.common.utils.DateUtils;
import oikos.app.common.utils.NanoIDGenerator;
import oikos.app.security.dtos.ForgotPasswordRequest;
import oikos.app.security.dtos.ResetPasswordRequest;
import oikos.app.security.exceptions.NoUserAssociatedException;
import oikos.app.security.exceptions.OAuth2AuthenticationProcessingException;
import oikos.app.security.exceptions.OldPasswordReuseException;
import oikos.app.security.exceptions.TokenExpiredException;
import oikos.app.security.exceptions.TokenNotValidException;
import oikos.app.security.exceptions.UserAccountAlreadyActivatedException;
import oikos.app.security.exceptions.UserAlreadyExistException;
import oikos.app.seller.Seller;
import oikos.app.seller.SellerBySecretarySignupRequest;
import oikos.app.seller.SellerSignupRequest;
import oikos.app.serviceproviders.ProviderService;
import oikos.app.serviceproviders.dtos.CollaboratorSignupRequest;
import oikos.app.serviceproviders.dtos.ServiceProviderSignupRequest;
import oikos.app.serviceproviders.exceptions.CompanyNotValidatedException;
import oikos.app.serviceproviders.models.Collaborator;
import oikos.app.serviceproviders.models.ServiceOwner;
import oikos.app.users.PasswordlessSignupRequest;
import oikos.app.users.Role;
import oikos.app.users.SigninRequest;
import oikos.app.users.SignupRequest;
import oikos.app.users.UpdateUserRequest;
import oikos.app.users.User;
import oikos.app.users.UserRepo;

/**
 * Created by Mohamed Haamdi on 27/04/2021.
 */
@Service @AllArgsConstructor @Slf4j public class SecurityService
  implements Authorizable<SecurityService.SecurityMethods> {
  private final PasswordEncoder encoder;
  private final AuthenticationManager authenticationManager;
  private final TokenProvider tokenProvider;
  private final ProviderService providerService;
  private final EmailService emailService;
  private final SMSService smsService;
  private final VerificationTokenRepo tokenRepo;
  private final ModelMapper mapper;
  private final UserRepo userRepo;
  private final BlacklistedJWTRepo blacklistedJWTRepo;
  private final AppProperties appProperties;


  @ToString enum SecurityMethods {
    REGISTER_NEW_SELLER_BY_SECRETARY(Names.REGISTER_NEW_SELLER_BY_SECRETARY),
    REGISTER_NEW_SECRETARY(Names.REGISTER_NEW_SECRETARY),
    REGISTER_NEW_COLLABORATOR(Names.REGISTER_NEW_COLLABORATOR);
    private final String label;

    SecurityMethods(String label) {
      this.label = label;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE) public static class Names {
      public static final String REGISTER_NEW_SELLER_BY_SECRETARY =
        "REGISTER_NEW_SELLER_BY_SECRETARY";
      public static final String REGISTER_NEW_COLLABORATOR =  "REGISTER_NEW_COLLABORATOR";
      public static final String REGISTER_NEW_SECRETARY =  "REGISTER_NEW_SECRETARY";
    }
  }

  @Transactional public String registerNewBuyer(SignupRequest req) {
    checkIfPhoneOrEmailAreAlreadyInUse(req);
    var user = createUser(req, Role.BUYER);
    var buyer = new Buyer();
    buyer.setValidated(false);
    buyer.setUser(user);
    user.setBuyerProfile(buyer);
    var token = generateVerificationToken(user, VerificationTokenType.EMAIL);
    emailService.sendUserVerificationEmail(user, token);
    userRepo.save(user);
    return token.getToken();
  }

  @Transactional public String registerNewServiceProvider(ServiceProviderSignupRequest req) {
    checkIfPhoneOrEmailAreAlreadyInUse(req);
    var user = createUser(req, Role.PROVIDER);
    var company = providerService.createCompany(req);
    var token = generateVerificationToken(user, VerificationTokenType.EMAIL);
    emailService.sendUserVerificationEmail(user, token);
    user = userRepo.save(user);
    final var owner =
      ServiceOwner.builder().serviceCompany(company).user(user).build();
    company.setServiceOwner(owner);
    providerService.saveCompanyAndOwner(owner,company);
    return token.getToken();
  }

  @Transactional public String registerNewSeller(SellerSignupRequest req) {
    checkIfPhoneOrEmailAreAlreadyInUse(req);
    var user = createUser(req, Role.BUYER, Role.SELLER);
    var token = generateVerificationToken(user, VerificationTokenType.SMS);
    smsService.sendUserVerificationSMS(user, token);
    var seller = getSeller(req);
    user.setSellerProfile(seller);
    seller.setUser(user);
    userRepo.save(user);
    return token.getToken();
  }

  @Transactional public String registerNewSellerBySecretary(
    SellerBySecretarySignupRequest req) {
    checkIfPhoneOrEmailAreAlreadyInUse(req);
    var user = createUser(req, Role.BUYER, Role.SELLER);
    var token =
      generateVerificationToken(user, VerificationTokenType.PASSWORD_RESET);
    emailService.sendPasswordResetEmail(user, token);
    var seller = getSeller(req);
    user.setSellerProfile(seller);
    seller.setUser(user);
    userRepo.save(user);
    return token.getToken();
  }
  @Transactional public String registerNewSecretary(PasswordlessSignupRequest req) {
    checkIfPhoneOrEmailAreAlreadyInUse(req);
    var user = createUser(req, Role.SECRETARY);
    var token =
      generateVerificationToken(user, VerificationTokenType.PASSWORD_RESET);
    emailService.sendPasswordResetEmail(user, token);
    userRepo.save(user);
    return token.getToken();
  }
  @Transactional
  public String registerNewCollaborator(CollaboratorSignupRequest req) {
    checkIfPhoneOrEmailAreAlreadyInUse(req);
    var user = createUser(req, Role.COLLABORATOR);
    var token =
      generateVerificationToken(user, VerificationTokenType.PASSWORD_RESET);
    emailService.sendPasswordResetEmail(user, token);
    var collaborator = new Collaborator();
    var company = providerService.getCompany(req.getCompanyID());
    if(!company.isValidated())
      throw new CompanyNotValidatedException(company.getName());
    user = userRepo.save(user);
    collaborator.setUser(user);
    collaborator.setServiceCompany(company);
    company.getCollaborators().add(collaborator);
    user.setCollaboratorProfile(collaborator);
    providerService.saveCollaborator(collaborator);
    return token.getToken();
  }

  @Transactional public String signIn(SigninRequest dto) {
    var authentication = authenticationManager.authenticate(
      new UsernamePasswordAuthenticationToken(dto.getEmailOrPhone(),
        dto.getPassword()));
    SecurityContextHolder.getContext().setAuthentication(authentication);
    return tokenProvider.generateToken(authentication);
  }

  @Transactional public String refreshToken() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    return tokenProvider.generateToken(authentication);
  }

  @Transactional
  public void confirmUser(String confirmationCode, VerificationTokenType type) {
    var token = verifyToken(confirmationCode, type);
    var user = token.getUser();
    if (user.isEnabled()) {
      throw type == VerificationTokenType.EMAIL ?
        new UserAccountAlreadyActivatedException(user.getEmail()) :
        new UserAccountAlreadyActivatedException(user.getPhoneNumber());
    }
    user.setEnabled(true);
    userRepo.save(user);
    tokenRepo.delete(token);
  }

  @Transactional public void resendUserConfirmationByEmail(String email) {
    var user = getUserAndCheckifEnabledAlready(email);
    var token = generateVerificationToken(user, VerificationTokenType.EMAIL);
    emailService.sendUserVerificationEmail(user, token);
  }

  @Transactional public void forgotPassword(ForgotPasswordRequest dto) {
    var userID = userRepo.findIDByEmailOrPhoneNumber(dto.getEmail())
      .orElseThrow(() -> new NoUserAssociatedException(dto.getEmail()));
    var user = userRepo.getOne(userID);
    if (user.getProvider() != AuthProvider.local) {
      throw new OAuth2AuthenticationProcessingException(
        "This account was created by using " + user.getProvider().name()
          .toUpperCase(Locale.ROOT) + ". Please use your " + user.getProvider()
          + " account to login and use the account");
    }
    //If the user doesn't have a password and is a local user then this is a user created by a secretary
    //So don't allow this method.
    if (user.getPassword() == null) {
      throw new AccessDeniedException(
        "This user doesn't have a password yet. Access your account using the email in your mailbox.");
    }
    var token =
      generateVerificationToken(user, VerificationTokenType.PASSWORD_RESET);
    emailService.sendPasswordResetEmail(user, token);
  }

  @Transactional
  public void resetPassword(ResetPasswordRequest dto, String tokenCode) {
    var token = verifyToken(tokenCode, VerificationTokenType.PASSWORD_RESET);
    var user = token.getUser();
    if (encoder.matches(dto.getPassword(), user.getPassword()))
      throw new OldPasswordReuseException();
    //If the user has no password and is using the password reset token then this user was
    //created by a secretary and still hasn't logged in yet. Once he sets up his password he can
    //login correctly.
    if (user.getPassword() == null
      && user.getProvider() == AuthProvider.local) {
      user.setEnabled(true);
    }
    user.setPassword(encoder.encode(dto.getPassword()));
    userRepo.save(user);
    tokenRepo.delete(token);
    revokeTokens(user.getId());
  }

  @Transactional public void resendUserConfirmationBySMS(String phonenumber) {
    var user = getUserAndCheckifEnabledAlready(phonenumber);
    var token = generateVerificationToken(user, VerificationTokenType.SMS);
    smsService.sendUserVerificationSMS(user, token);
  }
  @Transactional
  public User doUpdatePassword(UpdateUserRequest dto, User user) {
    if (dto.getPassword() != null && !dto.getPassword().isBlank()){
      revokeTokens(user.getId());
      user.setPassword(encoder.encode(dto.getPassword()));
    }
    return user;
  }

  @Override public boolean canDo(SecurityMethods methodName, String userID,
    String objectID) {
    return switch (methodName) {
      case REGISTER_NEW_SELLER_BY_SECRETARY -> userRepo.getOne(userID)
        .getRoles().stream().anyMatch(role -> role.equals(Role.SECRETARY));
      case REGISTER_NEW_SECRETARY -> userRepo.getOne(userID)
        .getRoles().stream().anyMatch(role -> role.equals(Role.ADMIN));
      case REGISTER_NEW_COLLABORATOR ->
        providerService.getCompany(objectID).getServiceOwner().getId().equals(userID);
    };
  }
  @Transactional
  public void revokeTokens(String userID) {
    blacklistedJWTRepo.save(new BlacklistedJWT(userID,Instant.now(),Instant.now().plus(appProperties.getAuth().getTokenExpirationMsec(),
      ChronoUnit.MILLIS)));
  }
  private Seller getSeller(SellerBySecretarySignupRequest req) {
    var seller = new Seller();
    seller.setAddress(Address.builder().zipCode(req.getZipCode())
      .departmentIdentifier(req.getDepartmentIdentifier())
      .street(req.getStreet()).build());
    seller.setBirthDate(req.getBirthDate());
    return seller;
  }

  private User getUserAndCheckifEnabledAlready(String phonenumber) {
    var userID = userRepo.findIDByEmailOrPhoneNumber(phonenumber)
      .orElseThrow(() -> new NoUserAssociatedException(phonenumber));
    var user = userRepo.getOne(userID);
    if (user.isEnabled()) // User already activated his account beforehand.
    {
      throw new UserAccountAlreadyActivatedException(phonenumber);
    }
    return user;
  }

  private User createUser(PasswordlessSignupRequest req, Role... roles) {
    var user = mapper.map(req, User.class);
    user.setId(null);//We sometimes get a wrong mapping here if the user is created by a company owner. So we need to ensure it is null.
    user.setProvider(AuthProvider.local);
    if (req instanceof SignupRequest) {
      user.setPassword(encoder.encode(((SignupRequest) req).getPassword()));
    } else if (req instanceof SellerSignupRequest) {
      user
        .setPassword(encoder.encode(((SellerSignupRequest) req).getPassword()));
    }
    final HashSet<Role> roleSet = new HashSet<>(Arrays.asList(roles));
    user.setRoles(roleSet);
    user.setEnabled(false);
    user = userRepo.save(user);
    return user;
  }

  private VerificationToken generateVerificationToken(User user,
    VerificationTokenType type) {
    var token = new VerificationToken();
    if (type == VerificationTokenType.SMS)
      token.setToken(NanoIDGenerator.generateSMSVerificationCode());
    else if (type == VerificationTokenType.EMAIL)
      token.setToken(NanoIDGenerator.generateEmailVerificationCode());
    else if (type == VerificationTokenType.PASSWORD_RESET)
      token.setToken(NanoIDGenerator.generateEmailVerificationCode());
    token.setUser(user);
    token.setType(type);
    token.setExpiresAt(DateUtils.calculateTokenExpiryDate(token.getType()));
    tokenRepo.save(token);
    return token;
  }

  private VerificationToken verifyToken(String tokenCode,
    VerificationTokenType type) {
    var token = tokenRepo.findByToken(tokenCode)
      .orElseThrow(() -> new TokenNotValidException(tokenCode));
    if (token.getType() != type)
      throw new TokenNotValidException(tokenCode);
    if (token.getExpiresAt().isBefore(Instant.now())) {
      throw new TokenExpiredException(tokenCode);
    }
    return token;
  }

  private void checkIfPhoneOrEmailAreAlreadyInUse(
    PasswordlessSignupRequest req) {
    if (userRepo.existsByPhoneNumber(req.getPhoneNumber()))
      throw new UserAlreadyExistException(String
        .format("Phone Number already in use : %s", req.getPhoneNumber()));
    if (userRepo.existsByEmailIgnoreCase(req.getEmail()))
      throw new UserAlreadyExistException(
        String.format("Email is already in use : %s", req.getEmail()));
  }
}
