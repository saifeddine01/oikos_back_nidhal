package oikos.app.common.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oikos.app.messaging.MessageResponse;
import oikos.app.notifications.NotificationResponse;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/** Created by Mohamed Haamdi on 28/04/2021. */
@Service
@AllArgsConstructor
@Slf4j
public class PubSubService {
  private final Mqtt5AsyncClient client;
  private final ModelMapper mapper;
  private final ObjectMapper jsonMapper;
  @Async
  public void publish(ChannelType type, String userID, Object message) {
    Object dto = getDto(message,type);
    try {
      var data = jsonMapper.writeValueAsBytes(dto);
      var topic = type.name() + "/" + userID;
      client.connect().
        thenCompose(ack -> client.publishWith().topic(topic).payload(data).send())
        .thenCompose(res -> client.disconnect()).
        thenRun((() -> log.info("Sent pubsub message to {}",topic)));
    } catch (Exception e) {
      log.error("Exception while sending PubSub message {}", e.getMessage());
    }
  }

  private Object getDto(Object content,ChannelType type) {
    return switch (type) {
      case MESSAGES -> mapper.map(content, MessageResponse.class);
      case NOTIFICATIONS -> mapper.map(content, NotificationResponse.class);
    };
  }
}
