package org.example.inbound.support.validator;


import lombok.extern.slf4j.Slf4j;
import org.example.inbound.core.props.ForwardedPortConfigProperties;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 해당 서비스에서 유효한 포트번호에 대한 검증을 수행하는 클래스
 */
@Slf4j
@Component
public class ForwardedPortValidator implements PortValidator {
  // 헤더
  private final Pattern headerPattern;
  // 포트
  private final Pattern portPattern;
  private final Integer minPort;
  private final Integer maxPort;

  public ForwardedPortValidator(ForwardedPortConfigProperties forwardedPortConfigProperties) {

    this.headerPattern = Pattern.compile(forwardedPortConfigProperties.getHeaderPattern());
    this.portPattern = Pattern.compile(forwardedPortConfigProperties.getPortPattern());
    this.minPort = forwardedPortConfigProperties.getMinPort();
    this.maxPort = forwardedPortConfigProperties.getMaxPort();

    log.info("Initialized with port range: {}-{}, port pattern: {}, header pattern: {}",
      minPort, maxPort, portPattern, headerPattern);
  }

  @Override
  public boolean isValidForwardedPort(String headerName, String portValue) {
    // 헤더(키) 검증
    if (!validateHeader(headerName)) {
      return false;
    }

    // 헤더(값) 검증
    if (!validatePort(portValue)) {
      return false;
    }

    log.debug("[Validation] Success - Header: {}, Port: {}", headerName, portValue);
    return true;
  }

  private boolean validateHeader(String headerName) {
    if (headerName == null || headerName.trim().isEmpty()) {
      log.debug("[Validation] Header name is null or empty");
      return false;
    }

    if (!headerPattern.matcher(headerName).matches()) {
      log.debug("[Validation] Invalid header name: '{}', pattern: {}",
        headerName, headerPattern);
      return false;
    }

    return true;
  }

  // null, empty, 숫자 포맷
  private boolean validatePort(String portValue) {
    if (!validatePortFormat(portValue)) {
      return false;
    }

    int port = Integer.parseInt(portValue.trim());
    return validatePortRange(port);
  }

  private boolean validatePortFormat(String portValue) {
    if (portValue == null || portValue.trim().isEmpty()) {
      log.debug("[Validation] Port value is null or empty");
      return false;
    }

    if (!portPattern.matcher(portValue.trim()).matches()) {
      log.debug("[Validation] Port value '{}' does not match pattern: {}",
        portValue, portPattern);
      return false;
    }

    try {
      Integer.parseInt(portValue.trim());
      return true;
    } catch (NumberFormatException e) {
      log.debug("[Validation] Port value '{}' is not a valid integer", portValue);
      return false;
    }
  }

  // 포트 범위
  private boolean validatePortRange(int port) {
    if (port < minPort || port > maxPort) {
      log.debug("[Validation] Port {} is outside allowed range [{}-{}]",
        port, minPort, maxPort);
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return String.format("ForwardedPortValidator(portRange=%d-%d, portPattern=%s, headerPattern=%s)",
      minPort, maxPort, portPattern, headerPattern);
  }
} 