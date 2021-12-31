package oikos.app.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oikos.app.common.configurations.AppProperties;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Date;

@RequiredArgsConstructor
@Slf4j
@Service
public class TokenProvider {

  private final AppProperties appProperties;

  public String generateToken(Authentication authentication) {
    var oikosUserDetails = (OikosUserDetails) authentication.getPrincipal();

    var now = new Date();
    var expiryDate = new Date(now.getTime() + appProperties.getAuth().getTokenExpirationMsec());

    return Jwts.builder()
        .setSubject(oikosUserDetails.getName())
        .setIssuedAt(new Date())
        .setExpiration(expiryDate)
        .signWith(SignatureAlgorithm.HS512, appProperties.getAuth().getTokenSecret())
        .compact();
  }

  // FIXME: This should be deleted when the testing phase is over.
  //  CRITICAL: This is really a bad security practice.
  public String generateToken(Authentication authentication, Date expiryDate) {

    OikosUserDetails userPrincipal = (OikosUserDetails) authentication.getPrincipal();
    return Jwts.builder()
        .setSubject(userPrincipal.getUsername())
        .setIssuedAt(new Date())
        .setExpiration(expiryDate)
        .signWith(SignatureAlgorithm.HS512, appProperties.getAuth().getTokenSecret())
        .compact();
  }

  public String getUserIdFromJWT(String token) {
    var claims =
        Jwts.parser()
            .setSigningKey(appProperties.getAuth().getTokenSecret())
            .parseClaimsJws(token)
            .getBody();

    return claims.getSubject();
  }

  public Claims getFromJWT(String token) {
    return Jwts.parser()
        .setSigningKey(appProperties.getAuth().getTokenSecret())
        .parseClaimsJws(token)
        .getBody();
  }

  public boolean validateToken(String authToken) {
    try {
      Jwts.parser()
          .setSigningKey(appProperties.getAuth().getTokenSecret())
          .parseClaimsJws(authToken);
      return true;
    } catch (SignatureException ex) {
      log.error("Invalid JWT signature");
    } catch (MalformedJwtException ex) {
      log.error("Invalid JWT token");
    } catch (ExpiredJwtException ex) {
      log.error("Expired JWT token");
    } catch (UnsupportedJwtException ex) {
      log.error("Unsupported JWT token");
    } catch (IllegalArgumentException ex) {
      log.error("JWT claims string is empty.");
    }
    return false;
  }
}
