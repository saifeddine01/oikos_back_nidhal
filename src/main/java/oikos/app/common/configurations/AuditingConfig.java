package oikos.app.common.configurations;

import oikos.app.security.OikosUserDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/** Created by Mohamed Haamdi on 11/05/2021. */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditingConfig {
  @Bean
  AuditorAware<String> auditorProvider() {
    return new AuditorAwareImpl();
  }

  class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication == null
          || !authentication.isAuthenticated()
          || authentication instanceof AnonymousAuthenticationToken) {
        return Optional.empty();
      }
      OikosUserDetails userPrincipal = (OikosUserDetails) authentication.getPrincipal();
      return Optional.ofNullable(userPrincipal.getName());
    }
  }
}
