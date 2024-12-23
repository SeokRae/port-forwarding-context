package org.example.source.support.handler;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.source.support.context.ForwardedPortContext;
import org.example.source.support.validator.PortValidator;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ForwardedPortHandler {
  private static final int MIN_PORT = 1;
  private static final int MAX_PORT = 65535;
  private static final String PORT_HEADER_SUFFIX = "forwarded-port";
  private static final String PORT_HEADER_PATTERN = "^[a-zA-Z0-9-]+-" + PORT_HEADER_SUFFIX + "$";

  private final PortValidator portValidator;

  /**
   * 헤더 정보를 검증하고 Context에 저장
   */
  public void validateAndStore(String headerName, String headerValue) {
    if (!isValidHeaderPattern(headerName)) {
      log.warn("Invalid header name: {}, value: {}", headerName, headerValue);
      return;
    }
    if (!validatePort(headerName, headerValue)) {
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
  private boolean validatePort(String key, String portValue) {
    if (portValue == null || portValue.trim().isEmpty()) {
      log.warn("Port value is null or empty for key: '{}'", key);
      return false;
    }

    try {
      int port = Integer.parseInt(portValue.trim());
      if (!portValidator.isValidPort(port)) {
        log.warn("Port {} is not in the allowed ports list", port);
        return false;
      }
      return isValidPortRange(port);
    } catch (NumberFormatException e) {
      log.error("Invalid port value format - Key: '{}', Value: '{}'", key, portValue);
      return false;
    }
  }

  private boolean isValidHeaderPattern(String headerName) {
    return headerName != null && headerName.matches(PORT_HEADER_PATTERN);
  }

  private boolean isValidPortRange(int port) {
    return port >= MIN_PORT && port <= MAX_PORT;
  }
} 