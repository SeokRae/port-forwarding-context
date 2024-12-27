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

    if (ForwardedPortContext.isEmpty()) {
      log.error("Port information is missing in the context.");
      return ResponseEntity.badRequest().body("Invalid or missing port information.");
    }

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

}
