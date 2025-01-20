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
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class DestinationService {

  private final RestTemplateHandler restTemplateHandler;
  private final ServiceProperties serviceProperties;
  private final UrisProperties urisProperties;

  public ResponseEntity<String> processGateway() {
    String headerKey = serviceProperties.getB().getHeader().getKey();
    Integer headerValue = ForwardedPortContext.getAttribute(headerKey).orElse(null);

    if (headerValue != null) {
      log.info("Forwarded Port: {}", headerValue);
      return handleDestinationB(); // Forward to B
    }

    return handleDestinationA(); // 처리할 A 메서드 호출
  }

  private ResponseEntity<String> handleDestinationA() {
    log.info("================================================== A Service Begin ==================================================");
    ResponseEntity<String> destinationA = ResponseEntity.ok().body("Destination A");
    log.info("================================================== A Service End ==================================================");
    return destinationA;
  }

  private ResponseEntity<String> handleDestinationB() {
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
