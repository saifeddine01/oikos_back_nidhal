package oikos.app.security;

import lombok.Getter;
import lombok.ToString;
import oikos.app.users.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/** Created by Mohamed Haamdi on 08/02/2021 */
@Getter
@ToString
public class OikosUserDetails implements OAuth2User, UserDetails {
  private final User user;
  private Map<String, Object> attributes;

  public OikosUserDetails(User user) {
    this.user = user;
  }

  public static OikosUserDetails create(User user) {
    return new OikosUserDetails(user);
  }

  public static OikosUserDetails create(User user, Map<String, Object> attributes) {
    var userPrincipal = OikosUserDetails.create(user);
    userPrincipal.setAttributes(attributes);
    return userPrincipal;
  }

  @Override
  public String getPassword() {
    return user.getPassword();
  }

  /** @return the userID */
  @Override
  public String getUsername() {
    return user.getId();
  }

  @Override
  public boolean isAccountNonExpired() {
    return !user.isAccountExpired();
  }

  @Override
  public boolean isAccountNonLocked() {
    return !user.isAccountLocked();
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return !user.isCredentialsExpired();
  }

  @Override
  public boolean isEnabled() {
    return user.isEnabled();
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return user.getRoles().stream()
        .map(role -> new SimpleGrantedAuthority((role.name())))
        .collect(Collectors.toSet());
  }

  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  public void setAttributes(Map<String, Object> attributes) {
    this.attributes = attributes;
  }

  @Override
  public String getName() {
    return String.valueOf(user.getId());
  }
}
