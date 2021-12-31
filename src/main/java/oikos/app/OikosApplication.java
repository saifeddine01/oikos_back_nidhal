package oikos.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import oikos.app.common.configurations.AppProperties;

/** Created by Mohamed Haamdi on 31/01/2021 */
@SuppressWarnings("deprecation")
@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class OikosApplication {

	public static void main(String[] args) {
		SpringApplication.run(OikosApplication.class, args);
	}

}
