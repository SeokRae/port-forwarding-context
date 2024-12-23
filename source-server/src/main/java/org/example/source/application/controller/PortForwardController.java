package org.example.source.application.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.example.source.core.props.ServiceProperties;
import org.example.source.core.props.UrisProperties;
import org.example.source.support.context.ForwardedPortContext;
import org.example.source.support.http.RestTemplateHandler;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/port")
@RequiredArgsConstructor
public class PortForwardController {

  private final RestTemplateHandler restTemplateHandler;
  private final ServiceProperties serviceProperties;
  private final UrisProperties urisProperties;

  /**
   * RequestContext에 저장된 포트 정보를 이용하여 서버로 요청을 전달한다.
   *
   * @return ResponseEntity<String> 대상 서버의 응답
   */
  @GetMapping("/forward")
  public ResponseEntity<String> forward() {

    String headerKey = serviceProperties.getA().getHeader().getKey();
    Integer port = ForwardedPortContext.getAttribute(headerKey).orElse(null);

    if (port == null) {
      log.error("Port information is missing in the context. Header Key: {}", headerKey);
      return ResponseEntity.badRequest().body("Invalid or missing port information.");
    }

    log.info("Forwarding request to service: {}, port: {}", headerKey, port);

    return restTemplateHandler.postRequest(
      serviceProperties.getA().getDomain(),
      urisProperties.getDestination(),
      HttpHeaders.EMPTY,
      HttpMethod.GET,
      new HashMap<>(),
      Strings.EMPTY,
      String.class
    );
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
