package org.example.destination.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.destination.domain.service.DestinationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/target/path")
@RequiredArgsConstructor
public class DestinationAController {

  private final DestinationService destinationService;

  @GetMapping(value = "/a")
  public ResponseEntity<String> gateway() {
    log.info("Gateway Requested");
    try {
      return destinationService.processGateway();
    } finally {
      log.info("Gateway Requested End");
    }
  }
}
