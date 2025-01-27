package org.example.source.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.client.rest.infrastructures.context.RequestContext;
import org.example.client.rest.infrastructures.factory.RequestStrategyFactory;
import org.example.inbound.infrastructure.context.ForwardedPortContext;
import org.example.source.core.props.ServiceProperties;
import org.example.source.core.props.UrisProperties;
import org.example.source.support.handler.HttpHeaderHandler;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/port")
@RequiredArgsConstructor
public class PortForwardController {

  private final ServiceProperties serviceProperties;
  private final UrisProperties urisProperties;
  private final HttpHeaderHandler httpHeaderHandler;
  private final RequestStrategyFactory requestStrategyFactory;

  /**
   * RequestContext에 저장된 포트 정보를 이용하여 서버로 요청을 전달한다.
   *
   * @return ResponseEntity<String> 대상 서버의 응답
   */
  @GetMapping("/forward")
  public ResponseEntity<String> forward() {
    log.info("Forwarding request to the destination server.");

    if (ForwardedPortContext.isEmpty()) {
      log.error("Port information is missing in the context.");
      return ResponseEntity.badRequest().body("Invalid or missing port information.");
    }

    RequestContext<String> requestContext = RequestContext.<String>builder()
      .mediaType(MediaType.APPLICATION_JSON)
      .domain(serviceProperties.getA().getDomain())
      .path(urisProperties.getDestination())
      .port(httpHeaderHandler.getForwardedPort().orElse(null))
      .httpHeaders(httpHeaderHandler.createHeaders(HttpHeaders.EMPTY))
      .httpMethod(HttpMethod.GET)
      .responseType(String.class)
      .build();

    String execute = requestStrategyFactory.findStrategy(requestContext)
      .execute(requestContext);
    log.info("Response from the destination server: {}", execute);
    return ResponseEntity.ok(execute);
  }
}
