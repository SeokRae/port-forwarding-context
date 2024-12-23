package org.example.destination.support.context;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ForwardedPortContext {
  private static final int MIN_PORT = 1;
  private static final int MAX_PORT = 65535;
  private static final String PORT_HEADER_SUFFIX = "forwarded-port";
  private static final String PORT_HEADER_PATTERN = "^[a-zA-Z0-9-]+-" + PORT_HEADER_SUFFIX + "$";
  private static final String PORT_HEADER_SUFFIX_LOWER = PORT_HEADER_SUFFIX.toLowerCase();

  private static final ThreadLocal<ForwardedPortContext> CONTEXT = ThreadLocal.withInitial(ForwardedPortContext::new);

  private final Map<String, Integer> contextData = new ConcurrentHashMap<>();

  public static ForwardedPortContext getContext() {
    return Optional.ofNullable(CONTEXT.get()).orElseGet(() -> {
      ForwardedPortContext context = new ForwardedPortContext();
      CONTEXT.set(context);
      return context;
    });
  }

  public void setAttribute(String key, int port) {
    if (!validateKeyAndPort(key, port)) {
      return;
    }
    contextData.put(key, port);
    log.debug("Port value set - Key: {}, Port: {}", key, port);
  }

  public Optional<Integer> getAttribute(String key) {
    return Optional.ofNullable(contextData.get(key));
  }

  public Map<String, Integer> getAttributes() {
    return Collections.unmodifiableMap(contextData);
  }

  public void removeAttribute(String key) {
    contextData.remove(key);
  }

  public void clear() {
    contextData.clear();
    CONTEXT.remove();
  }

  /**
   * 서블릿 요청 단계의 헤더 정보를 받아 포트 정보만을 추출하여 저장
   *
   * @param headers
   */
  public void extractAndProcessForwardedPorts(Map<String, String> headers) {
    headers.entrySet().stream()
      .filter(entry -> entry.getKey() != null && !entry.getKey().isEmpty())
      .filter(entry -> isValidHeaderPattern(entry.getKey()))
      .forEach(entry -> processPortHeader(entry.getKey(), entry.getValue()));
  }

  /**
   * 포트 정보의 유효성 검사 및 저장
   *
   * @param headerName
   * @param portValue
   */
  private void processPortHeader(String headerName, String portValue) {
    if (!validateKeyAndPort(headerName, portValue)) {
      return;
    }
    int port = Integer.parseInt(portValue.trim());
    contextData.put(headerName, port);
    log.info("Port value set - Key: {}, Port: {}", headerName, port);
  }

  private boolean validateKeyAndPort(String key, int port) {
    return validateKeyAndPort(key, String.valueOf(port));
  }

  /**
   * 헤더 정보의 키와 값(포트)의 유효성 검사
   *
   * @param key
   * @param portValue
   * @return
   */
  private boolean validateKeyAndPort(String key, String portValue) {
    if (key == null || key.trim().isEmpty()) {
      log.warn("Invalid key: null or empty");
      return false;
    }
    if (!isValidHeaderPattern(key)) {
      log.warn("Invalid header pattern for key: '{}'", key);
      return false;
    }

    if (portValue == null || portValue.trim().isEmpty()) {
      log.warn("Port value is null or empty for key: '{}'", key);
      return false;
    }

    try {
      int port = Integer.parseInt(portValue.trim());
      if (!isValidPort(port)) {
        log.warn("Invalid port number - Key: '{}', Port: {}", key, port);
        return false;
      }
      return true;
    } catch (NumberFormatException e) {
      log.error("Invalid port value format - Key: '{}', Value: '{}'", key, portValue);
      return false;
    }
  }

  /**
   * 헤더 이름의 유효성 검사 <br/>
   * - 포트 정보를 담은 헤더 이름은 특정 패턴을 따라야 함 <br/>
   * - 패턴: [a-zA-Z0-9-]+-forwarded-port
   *
   * @param headerName
   * @return
   */
  private boolean isValidHeaderPattern(String headerName) {
    if (headerName == null) {
      log.warn("Header name is null");
      return false;
    }
    String lowerHeaderName = headerName.toLowerCase();
    // 헤더 이름이 forwarded-port로 끝나지 않는 경우
    if (!lowerHeaderName.endsWith(PORT_HEADER_SUFFIX_LOWER)) {
      log.warn("Header '{}' must end with '{}'", headerName, PORT_HEADER_SUFFIX);
      return false;
    }
    // 헤더 이름이 특정 패턴을 따르지 않는 경우
    if (!headerName.matches(PORT_HEADER_PATTERN)) {
      log.warn("Invalid header pattern: '{}', expected pattern: '{}'", headerName, PORT_HEADER_PATTERN);
      return false;
    }
    return true;
  }

  /**
   * 포트 범위(MIN_PORT ~ MAX_PORT) 검사 <br/>
   * - 1 ~ 65535
   *
   * @param port
   * @return
   */
  private boolean isValidPort(int port) {
    return port >= MIN_PORT && port <= MAX_PORT;
  }
}