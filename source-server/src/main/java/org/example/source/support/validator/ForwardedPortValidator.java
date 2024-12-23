package org.example.source.support.validator;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.source.core.props.ServiceProperties;
import org.example.source.support.context.ForwardedPortContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ForwardedPortValidator {
  private static final int MIN_PORT = 1;
  private static final int MAX_PORT = 65535;
  private static final String PORT_HEADER_SUFFIX = "forwarded-port";
  private static final String PORT_HEADER_PATTERN = "^[a-zA-Z0-9-]+-" + PORT_HEADER_SUFFIX + "$";

  private final ServiceProperties serviceProperties;

  /**
   * 헤더 정보를 검증하고 Context에 저장
   */
  public void validateAndStore(String headerName, String headerValue) {
    if (!isValidHeaderPattern(headerName)) {
      log.warn("Invalid header name: {}, value: {}", headerName, headerValue);
      return;
    }
    if (!validateKeyAndPort(headerName, headerValue)) {
      log.warn("Invalid key: null or empty");
      return;
    }

    int port = Integer.parseInt(headerValue.trim());
    ForwardedPortContext.setAttribute(headerName, port);
    log.info("Port stored in context - Header: {}, Port: {}", headerName, port);
  }

  /**
   * 헤더 키와 포트값의 유효성 검증
   */
  private boolean validateKeyAndPort(String key, String portValue) {
    if (key == null || key.trim().isEmpty()) {
      return false;
    }

    String configuredKey = serviceProperties.getA().getHeader().getKey();
    if (!key.equals(configuredKey)) {
      log.warn("Invalid header key: '{}'. Expected: '{}'", key, configuredKey);
      return false;
    }

    if (portValue == null || portValue.trim().isEmpty()) {
      log.warn("Port value is null or empty for key: '{}'", key);
      return false;
    }

    try {
      int port = Integer.parseInt(portValue.trim());
      List<Integer> allowedPorts = serviceProperties.getA().getHeader().getPorts();
      if (!allowedPorts.contains(port)) {
        log.warn("Port {} is not in the allowed ports list: {}", port, allowedPorts);
        return false;
      }
      return isValidPort(port);
    } catch (NumberFormatException e) {
      log.error("Invalid port value format - Key: '{}', Value: '{}'", key, portValue);
      return false;
    }
  }

  private boolean isValidHeaderPattern(String headerName) {
    return headerName != null && headerName.matches(PORT_HEADER_PATTERN);
  }

  private boolean isValidPort(int port) {
    return port >= MIN_PORT && port <= MAX_PORT;
  }
} 