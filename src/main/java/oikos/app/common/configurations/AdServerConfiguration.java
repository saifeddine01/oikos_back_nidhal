package oikos.app.common.configurations;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import oikos.app.ads.AdRepository;
import oikos.app.ads.AdService;
import oikos.app.ads.AdServiceImpl;
import oikos.app.ads.MockAdService;
import oikos.app.common.repos.BienaVendreRepo;

@AllArgsConstructor
@Configuration
@Slf4j
public class AdServerConfiguration {
  private static final String SERVER_PATH = "http://localhost:8080/api/v1";
  private final AdRepository adRepo;
  private final ModelMapper mapper;
  private final BienaVendreRepo propRepo;
  private final ApplicationContext context;

  @ConditionalOnProperty(name = "ad.type", havingValue = "mock")
  @Bean
  AdService mockAdService() {
    return new MockAdService(adRepo, propRepo);
  }

  @ConditionalOnProperty(name = "ad.type", havingValue = "impl")
  @Bean
  AdService adServiceImpl() {
    return new AdServiceImpl(SERVER_PATH, adRepo, mapper, propRepo);
  }

  public AdService getAdService() {
    var x = context.getBean(AdService.class);
    return x;
  }
}
