package oikos.app.security.oauth2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oikos.app.buyer.Buyer;
import oikos.app.common.exceptions.InternalServerError;
import oikos.app.security.AuthProvider;
import oikos.app.security.OikosUserDetails;
import oikos.app.security.exceptions.OAuth2AuthenticationProcessingException;
import oikos.app.security.oauth2.user.OAuth2UserInfo;
import oikos.app.security.oauth2.user.OAuth2UserInfoFactory;
import oikos.app.users.Role;
import oikos.app.users.User;
import oikos.app.users.UserRepo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
@Slf4j
@RequiredArgsConstructor @Service public class CustomOAuth2UserService
  extends DefaultOAuth2UserService {

  private final UserRepo userRepository;

  @Override public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest)
    throws OAuth2AuthenticationException {
    var oAuth2User = super.loadUser(oAuth2UserRequest);

    try {
      return processOAuth2User(oAuth2UserRequest, oAuth2User);
    } catch (AuthenticationException ex) {
      throw ex;
    } catch (Exception ex) {
      // Throwing an instance of AuthenticationException will trigger the
      // OAuth2AuthenticationFailureHandler
      log.error("oauth2userservice.loaduser",ex);
      throw new InternalServerError(ex.getMessage(),ex);
    }
  }

  @Transactional
  public User registerNewUser(OAuth2UserRequest oAuth2UserRequest,
    OAuth2UserInfo oAuth2UserInfo) {
    var user = new User();
    user.setRoles(Set.of(Role.BUYER, Role.SELLER));
    user.setBuyerProfile(new Buyer());
    user.setProvider(AuthProvider
      .valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()));
    user.setProviderId(oAuth2UserInfo.getId());
    user.setFirstName(oAuth2UserInfo.getFirstName());
    user.setEmail(oAuth2UserInfo.getEmail());
    user.setLastName(oAuth2UserInfo.getLastName());
    return userRepository.save(user);
  }

  @Transactional public User updateExistingUser(User existingUser,
    OAuth2UserInfo oAuth2UserInfo) {
    existingUser.setLastName(oAuth2UserInfo.getLastName());
    existingUser.setFirstName(oAuth2UserInfo.getFirstName());
    return userRepository.save(existingUser);
  }

  private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest,
    OAuth2User oAuth2User) {
    var oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
      oAuth2UserRequest.getClientRegistration().getRegistrationId(),
      oAuth2User.getAttributes());
    if (StringUtils.isEmpty(oAuth2UserInfo.getEmail())) {
      throw new OAuth2AuthenticationProcessingException(
        "Email not found from OAuth2 provider");
    }

    var idOpt =
      userRepository.findIDByEmailOrPhoneNumber(oAuth2UserInfo.getEmail());
    User user;
    if (idOpt.isPresent()) {
      user = userRepository.getOne(idOpt.get());
      if (!user.getProvider().equals(AuthProvider.valueOf(
        oAuth2UserRequest.getClientRegistration().getRegistrationId()))) {
        throw new OAuth2AuthenticationProcessingException(
          "Looks like you're signed up with " + user.getProvider()
            + " account. Please use your " + user.getProvider()
            + " account to login.");
      }
      user = updateExistingUser(user, oAuth2UserInfo);
    } else {
      user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
    }

    return OikosUserDetails.create(user, oAuth2User.getAttributes());
  }
}
