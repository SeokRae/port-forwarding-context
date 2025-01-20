package org.example.destination.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.example.destination.core.props.ServiceProperties;
import org.example.destination.core.props.UrisProperties;
import org.example.destination.support.http.RestTemplateHandler;
import org.example.inbound.support.context.ForwardedPortContext;
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

  private final RestTemplateHandler restTemplateHandler;
  private final ServiceProperties serviceProperties;
  private final UrisProperties urisProperties;

  @GetMapping("/target/path/a")
  public ResponseEntity<String> destinationA() {

    String headerKey = serviceProperties.getB().getHeader().getKey();
    Integer headerValue = ForwardedPortContext.getAttribute(headerKey).orElse(null);

    try {
      log.info("================================================== A Service Begin ==================================================");
      if (headerValue != null) {
        log.info("Forwarded Port: {}", headerValue);
        return routeB();
      }
    } catch (RuntimeException e) {
      log.error("Error occurred while processing the request.", e);
      return ResponseEntity.internalServerError()
        .body("Error occurred while processing the request.");
    } finally {
      log.info("================================================== A Service End ==================================================");
    }
    return ResponseEntity.ok().body("Destination A");
  }

  private ResponseEntity<String> routeB() {
    log.info("================================================== Routing B Begin ==================================================");
    ResponseEntity<String> stringResponseEntity = restTemplateHandler.postRequest(
      serviceProperties.getB().getDomain(),
      urisProperties.getDestination(),
      HttpHeaders.EMPTY,
      HttpMethod.GET,
      new HashMap<>(),
      Strings.EMPTY,
      String.class
    );
    log.info("================================================== Routing B End ==================================================");
    return stringResponseEntity;
  }
}
