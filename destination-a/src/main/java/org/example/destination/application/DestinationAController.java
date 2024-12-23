package org.example.destination.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.example.destination.core.props.ServiceProperties;
import org.example.destination.core.props.UrisProperties;
import org.example.destination.support.helper.RestTemplateHelper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DestinationAController {

  private final RestTemplateHelper restTemplateHelper;
  private final ServiceProperties serviceProperties;
  private final UrisProperties urisProperties;

  @GetMapping("/target/path/a")
  public ResponseEntity<String> destinationA() {

    try {
      log.info("================================================== A Service Begin ==================================================");
      return restTemplateHelper.postRequest(
        serviceProperties.getB().getDomain(),
        urisProperties.getDestination(),
        HttpHeaders.EMPTY,
        HttpMethod.GET,
        new HashMap<>(),
        Strings.EMPTY,
        String.class
      );
    } catch (RuntimeException e) {
      log.error("Error occurred while processing the request.", e);
      return ResponseEntity.internalServerError()
        .body("Error occurred while processing the request.");
    } finally {
      log.info("================================================== A Service End ==================================================");
    }
  }
}
