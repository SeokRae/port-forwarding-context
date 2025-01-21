package org.example.source.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
      .andExpect(status().isOk())
      .andExpect(content().string("Destination A"));
  }

  @Test
  void testMultiplePortForwarding() throws Exception {
    mockMvc.perform(get("/port/forward")
        .header("service-a-forwarded-port", 8081)
        .header("service-b-forwarded-port", 8082)
        .header("service-c-forwarded-port", 8082)
      )
      .andExpect(status().isOk())
      .andExpect(content().string("Destination B"));
  }

  @Test
  void testInvalidPortForwarding() throws Exception {
    // Invalid port number
    mockMvc.perform(get("/port/forward"))
      .andExpect(status().isBadRequest())
      .andExpect(content().string("Invalid or missing port information."));

  }
}