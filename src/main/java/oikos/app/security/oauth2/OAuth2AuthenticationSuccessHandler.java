package oikos.app.security.oauth2;

import lombok.RequiredArgsConstructor;
import oikos.app.common.configurations.AppProperties;
import oikos.app.common.exceptions.BaseException;
import oikos.app.security.TokenProvider;
import oikos.app.common.utils.CookieUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static oikos.app.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;

@RequiredArgsConstructor
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final TokenProvider tokenProvider;

  private final AppProperties appProperties;

  private final HttpCookieOAuth2AuthorizationRequestRepository
      httpCookieOAuth2AuthorizationRequestRepository;

  @Override
  protected String determineTargetUrl(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
    Optional<String> redirectUri =
        CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME).map(Cookie::getValue);

    if (redirectUri.isPresent() && !isAuthorizedRedirectUri(redirectUri.get())) {
      throw new BaseException(
          "Sorry! We've got an Unauthorized Redirect URI and can't proceed with the authentication");
    }

    String targetUrl = redirectUri.orElse(getDefaultTargetUrl());

    String token = tokenProvider.generateToken(authentication);

    return UriComponentsBuilder.fromUriString(targetUrl)
        .queryParam("token", token)
        .build()
        .toUriString();
  }

  protected void clearAuthenticationAttributes(
      HttpServletRequest request, HttpServletResponse response) {
    super.clearAuthenticationAttributes(request);
    httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(
        request, response);
  }

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException, ServletException {
    String targetUrl = determineTargetUrl(request, response, authentication);

    if (response.isCommitted()) {
      logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
      return;
    }

    clearAuthenticationAttributes(request, response);
    getRedirectStrategy().sendRedirect(request, response, targetUrl);
  }

  private boolean isAuthorizedRedirectUri(String uri) {
    // var clientRedirectUri = URI.create(uri);
    // FIXME: This is always true because I don't have the redirect uri of the app from both
    // frontend and mobile teams
    return true;
    /*return appProperties.getOauth2().getAuthorizedRedirectUris().stream()
    .anyMatch(
        authorizedRedirectUri -> {
          // Only validate host and port. Let the clients use different paths if they want to
          var authorizedURI = URI.create(authorizedRedirectUri);
          return authorizedURI.getHost()
            .equalsIgnoreCase(clientRedirectUri.getHost())
            && authorizedURI.getPort() == clientRedirectUri.getPort();
        });*/
  }
}
