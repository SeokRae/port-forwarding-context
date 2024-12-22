package org.example.source.application.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PortForwardControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void testSinglePortForwarding() throws Exception {
    mockMvc.perform(get("/port/forward")
        .header("service-a-forwarded-port", 8081))
      .andExpect(status().isOk());
  }

  @Test
  void testMultiplePortForwarding() throws Exception {
    mockMvc.perform(get("/port/forward")
        .header("service-a-forwarded-port", 8081)
        .header("service-b-forwarded-port", 8082))
      .andExpect(status().isOk());
  }

  @Test
  void testInvalidPortForwarding() {
    // Invalid port number
    assertThrows(Exception.class, () -> mockMvc.perform(get("/port/forward")
        .header("service-a-forwarded-port", "A"))
      .andExpect(status().isBadRequest()));
  }
}