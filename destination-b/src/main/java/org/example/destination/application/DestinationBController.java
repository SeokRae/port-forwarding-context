package org.example.destination.application;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/target")
public class DestinationBController {

  @GetMapping("/path")
  public ResponseEntity<String> destinationB() {
    return ResponseEntity.ok("Destination B");
  }
}
