package oikos.app.common.configurations;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Created by Mohamed Haamdi on 28/04/2021. */
@Configuration
public class MqttConfig {
  @Value("${mosquitto.host}")
  private String hostname;

  @Value("${mosquitto.port}")
  private String port;

  @Bean
  Mqtt5AsyncClient client() {
    return MqttClient.builder()
        .identifier("oikos_server")
        .serverHost(hostname)
        .serverPort(Integer.parseInt(port))
        .useMqttVersion5()
        .buildAsync();
  }
}
