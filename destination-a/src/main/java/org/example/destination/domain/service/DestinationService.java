package org.example.destination.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.client.rest.infrastructures.context.RequestContext;
import org.example.client.rest.infrastructures.factory.RequestStrategyFactory;
import org.example.destination.core.props.ServiceProperties;
import org.example.destination.core.props.UrisProperties;
import org.example.destination.support.handler.HttpHeaderHandler;
import org.example.inbound.infrastructure.context.ForwardedPortContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DestinationService {

  private final RequestStrategyFactory requestStrategyFactory;
  private final ServiceProperties serviceProperties;
  private final UrisProperties urisProperties;
  private final HttpHeaderHandler httpHeaderHandler;

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

    RequestContext<String> requestContext = RequestContext.<String>builder()
      .mediaType(MediaType.APPLICATION_JSON)
      .domain(serviceProperties.getB().getDomain())
      .path(urisProperties.getDestination())
      .port(httpHeaderHandler.getForwardedPort().orElse(null))
      .httpHeaders(httpHeaderHandler.createHeaders(HttpHeaders.EMPTY))
      .httpMethod(HttpMethod.GET)
      .responseType(String.class)
      .build();

    String execute = requestStrategyFactory.findStrategy(requestContext)
      .execute(requestContext);
    log.info("================================================== Routing B End ==================================================");
    return ResponseEntity.ok(execute);
  }
}
