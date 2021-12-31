package oikos.app.common.configurations;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@EnableConfigurationProperties @ConfigurationProperties(prefix = "app")
@Component @Getter public class AppProperties {
  private final Auth auth = new Auth();
  private final OAuth2 oauth2 = new OAuth2();
  private final Files files = new Files();


  @Getter @Setter public static class Auth {
    private String tokenSecret;
    private long tokenExpirationMsec;
  }


  @Getter @Setter public static final class OAuth2 {
    private List<String> authorizedRedirectUris = new ArrayList<>();
  }


  @Getter @Setter public static final class Files {
    private String users;
    private String messages;
    private String pdf;
  }
}
