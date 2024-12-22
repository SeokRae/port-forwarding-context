package org.example.destination.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.example.destination.core.props.ServiceProperties;
import org.example.destination.core.props.UrisProperties;
import org.example.destination.support.context.ForwardedPortContext;
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
    ForwardedPortContext context = ForwardedPortContext.getContext();
    String headerKey = serviceProperties.getB().getHeader().getKey();
    Integer headerValue = context.getAttribute(headerKey, Integer.class).orElse(null);

    try {
      log.info("================================================== A Service Begin ==================================================");
      if (headerValue != null) {
        log.info("Header found: {} -> Delegating to destination B", headerKey);
        log.info("Initiating request to service: {}, port: {}", headerKey, headerValue);
        return destinationB();
      } else {
        log.info("Header not found: {} -> Responding with 'Destination A'", headerKey);
        return ResponseEntity.ok("Destination A");
      }
    } catch (RuntimeException e) {
      log.error("Error occurred while processing the request.", e);
      return ResponseEntity.internalServerError()
        .body("Error occurred while processing the request.");
    } finally {
      log.info("================================================== A Service End ==================================================");
    }
  }

  private ResponseEntity<String> destinationB() {
    log.info("================================================== B Service Call ==================================================");
    ForwardedPortContext context = ForwardedPortContext.getContext();
    String headerKey = serviceProperties.getB().getHeader().getKey();

    Integer port = context.getAttribute(headerKey, Integer.class).orElse(null);

    if (port == null) {
      log.error("Port information is missing in the context. Header Key: {}", headerKey);
      return ResponseEntity.badRequest().body("Invalid or missing port information.");
    }

    HttpHeaders headers = createHeaders(context, headerKey);
    log.info("Forwarding request to service: {}, port: {}", headerKey, port);
    try {

      return restTemplateHelper.postRequest(
        serviceProperties.getB().getDomain(),
        urisProperties.getDestination(),
        port,
        headers,
        HttpMethod.GET,
        new HashMap<>(),
        Strings.EMPTY,
        String.class
      );
    } catch (Exception e) {
      log.error("Error occurred while processing the request.", e);
      return ResponseEntity.badRequest().body("Error occurred while processing the request.");
    } finally {
      log.info("================================================== B Service End ==================================================");
    }
  }

  private HttpHeaders createHeaders(ForwardedPortContext context, String excludeKey) {
    HttpHeaders headers = new HttpHeaders();
    context.getAttributes()
      .entrySet()
      .stream()
      .filter(entry -> !entry.getKey().equals(excludeKey))
      .forEach(entry -> headers.add(entry.getKey(), String.valueOf(entry.getValue())));
    return headers;
  }
}
