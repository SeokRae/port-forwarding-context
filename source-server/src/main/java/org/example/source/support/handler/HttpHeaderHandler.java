package org.example.source.support.handler;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.source.core.props.ServiceProperties;
import org.example.source.support.context.ForwardedPortContext;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

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

  private void addForwardedPortHeaders(HttpHeaders headers) {
    String currentServiceHeaderKey = getCurrentServiceHeaderKey();
    ForwardedPortContext.getAttributes().entrySet().stream()
      .filter(entry -> !entry.getKey().equals(currentServiceHeaderKey))
      .forEach(entry -> {
        String key = entry.getKey();
        Integer port = entry.getValue();
        log.info("Port forwarded - key: {}, port: {}", key, port);
        headers.add(key, String.valueOf(port));
      });
  }

  public String getCurrentServiceHeaderKey() {
    return serviceProperties.getA().getHeader().getKey();
  }

  public Optional<Integer> getForwardedPort() {
    return ForwardedPortContext.getAttribute(getCurrentServiceHeaderKey());
  }
} 