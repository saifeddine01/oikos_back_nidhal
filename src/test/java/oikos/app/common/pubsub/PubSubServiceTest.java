package oikos.app.common.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PubSubServiceTest {

  @Mock private Mqtt5AsyncClient mockClient;
  @Mock private ModelMapper mapper;
  @Mock private ObjectMapper jsonMapper;
  private PubSubService pubSubServiceUnderTest;

  @BeforeEach
  void setUp() {
    pubSubServiceUnderTest = new PubSubService(mockClient, mapper, jsonMapper);
  }

  @Test
  void testSendPubSubMessage() {
    // Setup

    // Run the test
    pubSubServiceUnderTest.publish(ChannelType.MESSAGES, "userID", new Object());

    // Verify the results
    verify(mockClient).connect();
  }
}
