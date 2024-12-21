package org.example.source.application.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.source.core.props.ServiceProperties;
import org.example.source.core.props.UrisProperties;
import org.example.source.support.context.RequestContext;
import org.example.source.support.helper.RestTemplateHelper;
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

  private final RestTemplateHelper restTemplateHelper;
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
    RequestContext context = RequestContext.getContext();

    Integer port = context
      .getAttribute(headerKey, Integer.class)
      .orElse(null);

    HttpHeaders headers = createHeadersExcluding(context, headerKey);

    log.info("Forwarding request to service: {}, port: {}", headerKey, port);

    return restTemplateHelper.postRequest(
      serviceProperties.getA().getDomain(),
      urisProperties.getDestination(),
      port,
      headers,
      HttpMethod.GET,
      new HashMap<>(),
      "",
      String.class
    );
  }

  /**
   * RequestContext의 속성들을 HttpHeaders로 변환하되, 지정된 키는 제외합니다.
   *
   * @param context 요청 컨텍스트
   * @param excludeKey 제외할 헤더 키
   * @return 변환된 HttpHeaders
   */
  private HttpHeaders createHeadersExcluding(RequestContext context, String excludeKey) {
    HttpHeaders headers = new HttpHeaders();
    context.getAttributesExcluding(excludeKey)
      .forEach((headerName, headerValue) -> 
        headers.add(headerName, String.valueOf(headerValue))
      );
    return headers;
  }
}
