package oikos.app.security.oauth2.user;

import oikos.app.security.AuthProvider;
import oikos.app.security.exceptions.OAuth2AuthenticationProcessingException;

import java.util.Map;

public class OAuth2UserInfoFactory {

  public static OAuth2UserInfo getOAuth2UserInfo(
      String registrationId, Map<String, Object> attributes) {
    if (AuthProvider.valueOf(registrationId) == AuthProvider.google) {
      return new GoogleOAuth2UserInfo(attributes);
    } else if (AuthProvider.valueOf(registrationId) == AuthProvider.facebook) {
      return new FacebookOAuth2UserInfo(attributes);
    } else {
      throw new OAuth2AuthenticationProcessingException(
          "Sorry! Login with " + registrationId + " is not supported yet.");
    }
  }
}
