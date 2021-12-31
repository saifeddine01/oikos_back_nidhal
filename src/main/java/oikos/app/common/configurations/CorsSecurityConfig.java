package oikos.app.common.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/** Created by Mohamed Haamdi on 30/04/2021. */
@Configuration
public class CorsSecurityConfig {

  @Bean
  public CorsFilter corsFilter() {

    var source = new UrlBasedCorsConfigurationSource();
    var config = new CorsConfiguration();
    // config.setAllowCredentials(true); // you USUALLY want this
    config.addAllowedOrigin("*");
    config.addAllowedHeader("*");
    config.addAllowedMethod("OPTIONS");
    config.addAllowedMethod("HEAD");
    config.addAllowedMethod("GET");
    config.addAllowedMethod("PUT");
    config.addAllowedMethod("POST");
    config.addAllowedMethod("DELETE");
    config.addAllowedMethod("PATCH");
    source.registerCorsConfiguration("/**", config);
    return new CorsFilter(source);
  }
}
