package org.example.destination.support.handler;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.destination.core.props.ServiceProperties;
import org.example.inbound.infrastructure.context.ForwardedPortContext;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class HttpHeaderHandler {

  private final ServiceProperties serviceProperties;

  public HttpHeaders createHeaders(HttpHeaders originalHeaders) {
    try {
      HttpHeaders headers = new HttpHeaders();
      Optional.ofNullable(originalHeaders)
        .ifPresent(headers::putAll);
      addForwardedPortHeaders(headers);
      return headers;
    } catch (Exception e) {
      log.error("Error occurred while creating headers", e);
      return new HttpHeaders();
    }
  }

  // 여기서 다음 서버로 보낼 헤더를 HttpHeaders에 저장
  private void addForwardedPortHeaders(HttpHeaders headers) {
    // A 프로퍼티 헤더 key 값 조회
    String currentServiceHeaderKey = getCurrentServiceHeaderKey();

    try {
      ForwardedPortContext.getAttributes().entrySet().stream()
        .filter(entry -> !entry.getKey().equals(currentServiceHeaderKey))
        .forEach(entry -> {
          String key = entry.getKey();
          Integer port = entry.getValue();
          logPortForwarding(key, port);
          headers.add(key, String.valueOf(port));
        });
    } catch (Exception e) {
      log.error("Error occurred while adding forwarded port headers", e);
    }
  }

  public String getCurrentServiceHeaderKey() {
    return serviceProperties.getB().getHeader().getKey();
  }

  public Optional<Integer> getForwardedPort() {
    String headerKey = getCurrentServiceHeaderKey();
    List<Integer> allowedPorts = serviceProperties.getB().getHeader().getPorts();

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