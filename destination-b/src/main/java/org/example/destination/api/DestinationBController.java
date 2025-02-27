package org.example.destination.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/target/path")
public class DestinationBController {

  @GetMapping("/b")
  public ResponseEntity<String> destinationB() {
    log.info("Destination B");
    return ResponseEntity.ok("Destination B");
  }
}
