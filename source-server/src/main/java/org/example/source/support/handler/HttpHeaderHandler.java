package org.example.source.support.handler;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.inbound.support.context.ForwardedPortContext;
import org.example.source.core.props.ServiceProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class HttpHeaderHandler {
  private final ServiceProperties serviceProperties;

  public HttpHeaders createHeaders(HttpHeaders originalHeaders) {
    HttpHeaders headers = new HttpHeaders();
    headers.addAll(originalHeaders);
    addForwardedPortHeaders(headers);
    return headers;
  }

  // 여기서 다음 서버로 보낼 헤더를 HttpHeaders에 저장
  private void addForwardedPortHeaders(HttpHeaders headers) {
    // A 프로퍼티 헤더 key 값 조회
    String currentServiceHeaderKey = getCurrentServiceHeaderKey();

    ForwardedPortContext.getAttributes().entrySet().stream()
      .filter(entry -> !entry.getKey().equals(currentServiceHeaderKey))
      .forEach(entry -> {
        String key = entry.getKey();
        Integer port = entry.getValue();
        logPortForwarding(key, port);
        headers.add(key, String.valueOf(port));
      });
  }

  public String getCurrentServiceHeaderKey() {
    return serviceProperties.getA().getHeader().getKey();
  }

  public Optional<Integer> getForwardedPort() {
    String headerKey = getCurrentServiceHeaderKey();
    List<Integer> allowedPorts = serviceProperties.getA().getHeader().getPorts();

    return ForwardedPortContext.getAttribute(headerKey)
      .filter(port -> isValidPort(port, allowedPorts))
      .map(port -> {
        logPortForwarding(headerKey, port);
        return port;
      });
  }

  private boolean isValidPort(Integer port, List<Integer> allowedPorts) {
    return allowedPorts.contains(port);
  }

  private void logPortForwarding(String headerKey, Integer port) {
    log.info("Port forwarded - key: {}, port: {}", headerKey, port);
  }
} 