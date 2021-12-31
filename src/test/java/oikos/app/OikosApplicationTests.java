package oikos.app;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class OikosApplicationTests {

  @Test
  void contextLoads() {
    Assertions.assertEquals(0, 0);
  }
}
