package org.example.destination.support.validator;


import lombok.extern.slf4j.Slf4j;
import org.example.destination.core.props.ForwardPort;
import org.example.destination.core.props.ForwardPortHeader;
import org.example.destination.core.props.ForwardPortProperties;
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

  public ForwardedPortValidator(ForwardPortProperties forwardPortProperties) {
    ForwardPortHeader headers = forwardPortProperties.getHeaders();
    this.headerPattern = Pattern.compile(headers.getPatterns().getPattern());

    ForwardPort ports = headers.getPorts();
    this.portPattern = Pattern.compile(ports.getPattern());
    this.minPort = ports.getRange().getMin();
    this.maxPort = ports.getRange().getMax();

    log.info("Initialized with port range: {}-{}, port pattern: {}, header pattern: {}",
      minPort, maxPort, portPattern, headerPattern);
  }

  @Override
  public boolean isValidForwardedPort(String headerName, String portValue) {
    if (!validateHeader(headerName)) {
      return false;
    }

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