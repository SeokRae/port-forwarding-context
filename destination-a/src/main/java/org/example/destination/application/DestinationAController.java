package org.example.destination.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.example.destination.core.props.ServiceProperties;
import org.example.destination.core.props.UrisProperties;
import org.example.destination.support.context.RequestContext;
import org.example.destination.support.helper.RestTemplateHelper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/target")
@RequiredArgsConstructor
public class DestinationAController {

  private final RestTemplateHelper restTemplateHelper;
  private final ServiceProperties serviceProperties;
  private final UrisProperties urisProperties;

  @GetMapping("/path")
  public ResponseEntity<String> destinationA() {

    // 서버 내 설정 정보 기반 Context에서 Lookup 하여 처리
    String headerKey = serviceProperties.getB().getHeader().getKey();
    RequestContext context = RequestContext.getContext();

    Integer port = context
      .getAttribute(headerKey, Integer.class)
      .orElse(null);

    HttpHeaders headers = createHeadersExcluding(context, headerKey);

    log.info("Forwarding request to service: {}, port: {}", headerKey, port);

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
  }

  /**
   * RequestContext의 속성들을 HttpHeaders로 변환하되, 지정된 키는 제외합니다.
   *
   * @param context    요청 컨텍스트
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
