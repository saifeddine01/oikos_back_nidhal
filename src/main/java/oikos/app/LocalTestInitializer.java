package oikos.app;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oikos.app.buyer.Buyer;
import oikos.app.buyer.BuyerRepo;
import oikos.app.common.models.Address;
import oikos.app.common.repos.BienaVendreRepo;
import oikos.app.oikosservices.OikosService;
import oikos.app.oikosservices.OikosServiceRepo;
import oikos.app.oikosservices.ServiceType;
import oikos.app.security.AuthProvider;
import oikos.app.security.TokenProvider;
import oikos.app.seller.Seller;
import oikos.app.seller.SellerRepo;
import oikos.app.serviceproviders.models.ServiceCompany;
import oikos.app.serviceproviders.models.ServiceOwner;
import oikos.app.serviceproviders.repos.ServiceCompanyRepo;
import oikos.app.serviceproviders.repos.ServiceOwnerRepo;
import oikos.app.users.Civility;
import oikos.app.users.MaritalStatus;
import oikos.app.users.Role;
import oikos.app.users.User;
import oikos.app.users.UserRepo;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
// Testing Jenkins again.
/** Created by Mohamed Haamdi on 16/04/2021. */
@Component
@Slf4j
@RequiredArgsConstructor
public class LocalTestInitializer {
  private static final String SELLER_ID = "00000000";
  private static final String SELLER = "Seller";
  private static final String BUYER_ID = "00000001";
  private static final String BUYER = "Buyer";
  private static final String ADMIN_ID = "00000002";
  private static final String ADMIN = "Admin";
  private static final String SECRETARY_ID = "00000003";
  private static final String SECRETARY = "Secretary";
  private static final String PROVIDER_ID = "00000004";
  private static final String PROVIDER = "Provider";
  private static final String COMPANY_ID = SELLER_ID;
  private static final String COMPANY = "Company";

  private final UserRepo userRepo;
  private final PasswordEncoder encoder;
  private final BuyerRepo buyerRepo;
  private final SellerRepo sellerRepo;
  private final AuthenticationManager authenticationManager;
  private final ServiceCompanyRepo companyRepo;
  private final ServiceOwnerRepo ownerRepo;
  private final OikosServiceRepo serviceRepo;
  private final TokenProvider tokenProvider;
  private final BienaVendreRepo repo;

  @Transactional
  public void onApplicationEvent(ContextRefreshedEvent event) {

    log.info("Creating default test users");
    // we add a seller first
    if (userRepo.findById(SELLER_ID).isEmpty()) {
      var user = getUser(SELLER_ID, SELLER, "Seller@seller.com");
      user.setRoles(Set.of(Role.SELLER, Role.BUYER));
      user = userRepo.save(user);
      var seller = new Seller();
      seller.setUser(user);
      seller.setAddress(
          Address.builder().street(SELLER).zipCode(SELLER_ID).departmentIdentifier(1).build());
      seller.setCivility(Civility.MONSIEUR);
      seller.setBirthDate(LocalDate.of(2000, 1, 1));
      seller.setMaritalStatus(MaritalStatus.CELIBATAIRE);
      user.setSellerProfile(seller);
      sellerRepo.save(seller);
      generateJWT(SELLER_ID, "JWT for SELLER : ");
    }

    if (userRepo.findById(BUYER_ID).isEmpty()) {
      User user = getUser(BUYER_ID, BUYER, "BUYER@BUYER.com");
      user.setRoles(Set.of(Role.BUYER));
      user = userRepo.save(user);
      Buyer buyer = new Buyer();
      buyer.setUser(user);
      buyer.setValidated(false);
      user.setBuyerProfile(buyer);
      buyerRepo.save(buyer);
      generateJWT(BUYER_ID, "JWT for BUYER : ");
    }

    if (userRepo.findById(ADMIN_ID).isEmpty()) {
      User user = getUser(ADMIN_ID, ADMIN, "Admin@oikos.com");
      user.setRoles(Set.of(Role.ADMIN));
      userRepo.save(user);
      generateJWT(ADMIN_ID, "JWT for ADMIN : ");
    }

    if (userRepo.findById(SECRETARY_ID).isEmpty()) {
      User user = getUser(SECRETARY_ID, SECRETARY, "Secretary@oikos.com");
      user.setRoles(Set.of(Role.SECRETARY));
      userRepo.save(user);
      generateJWT(SECRETARY_ID, "JWT for SECRETARY : ");
    }

    if (userRepo.findById(PROVIDER_ID).isEmpty()) {
      var user = getUser(PROVIDER_ID, PROVIDER, "PROVIDER@oikos.com");
      user.setRoles(Set.of(Role.PROVIDER));
      user = userRepo.save(user);
      generateJWT(PROVIDER_ID, "JWT for PROVIDER : ");
      var owner = ownerRepo.save(ServiceOwner.builder().user(user).build());
      ServiceCompany serviceCompany =
          ServiceCompany.builder()
              .serviceOwner(owner)
              .id(COMPANY_ID)
              .isValidated(true)
              .RIB(COMPANY)
              .SIRET(COMPANY)
              .name(COMPANY)
              .address(
                  Address.builder()
                      .street("Street 54520")
                      .zipCode("90247")
                      .departmentIdentifier(75)
                      .build())
              .build();
      serviceCompany = companyRepo.save(serviceCompany);
      serviceCompany.setServices(new ArrayList<>());
      final var service =
          OikosService.builder()
              .serviceType(ServiceType.DIAGNOSTIC)
              .serviceCompany(serviceCompany)
              .id(COMPANY_ID)
              .isActive(true)
              .price(BigDecimal.TEN)
              .build();
      serviceCompany.getServices().add(service);
      owner.setServiceCompany(serviceCompany);
      serviceRepo.save(service);
      ownerRepo.save(owner);
    }
  }

  private void generateJWT(String userID, String message) {
    final var authentication =
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userID, userID));
    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = tokenProvider.generateToken(authentication, new Date(Long.MAX_VALUE));
    log.info(message);
    log.info(jwt);
  }

  private User getUser(String userid, String username, String email) {
    final var user = new User();
    user.setId(userid);
    user.setFirstName(username);
    user.setLastName(username);
    user.setEnabled(true);
    user.setPassword(encoder.encode(userid));
    user.setEmail(email);
    user.setPhoneNumber(userid);
    user.setProvider(AuthProvider.local);
    return user;
  }
}
