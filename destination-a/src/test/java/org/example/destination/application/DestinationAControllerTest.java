package org.example.destination.application;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DestinationAControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void testSinglePortForwarding() throws Exception {
    mockMvc.perform(get("/target/path")
        .header("service-b-forwarded-port", 8082))
      .andExpect(status().isOk());
  }
}