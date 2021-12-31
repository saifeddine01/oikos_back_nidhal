package oikos.app.offers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by Mohamed Haamdi on 11/05/2021.
 */
@Disabled @ActiveProfiles("testmvc") @WebMvcTest(OfferController.class)
class OfferControllerTest {
  @MockBean OfferService offerService;
  @Autowired MockMvc mockMvc;

  @BeforeEach void setUp() {
  }

  @Test void shouldWork() throws Exception {
    mockMvc.perform(get("/offers")).andExpect(status().isOk())
      .andExpect(jsonPath("$.message").value("Test successful"));

  }
}
