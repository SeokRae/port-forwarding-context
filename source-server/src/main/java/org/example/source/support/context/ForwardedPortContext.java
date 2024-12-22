package org.example.source.support.context;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ForwardedPortContext {
  private static final ThreadLocal<ForwardedPortContext> CONTEXT = new ThreadLocal<>();
  private static final String PORT_HEADER_SUFFIX = "forwarded-port";
  private static final int MIN_PORT = 1;
  private static final int MAX_PORT = 65535;

  private final Map<String, Object> contextData = new ConcurrentHashMap<>();

  public static ForwardedPortContext getContext() {
    return Optional.ofNullable(CONTEXT.get()).orElseGet(() -> {
      ForwardedPortContext context = new ForwardedPortContext();
      CONTEXT.set(context);
      return context;
    });
  }

  public void setAttribute(String key, Object value) {
    if (value != null) {
      contextData.put(key, value);
    }
  }

  public <T> Optional<T> getAttribute(String key, Class<T> type) {
    return Optional.ofNullable(contextData.get(key)).filter(type::isInstance).map(type::cast);
  }

  public Map<String, Object> getAttributes() {
    return Collections.unmodifiableMap(contextData);
  }

  public void removeAttribute(String key) {
    contextData.remove(key);
  }

  public void clear() {
    contextData.clear();
    CONTEXT.remove();
  }

  public void extractAndProcessForwardedPorts(Map<String, String> headers) {
    headers.entrySet().stream()
      .filter(entry -> entry.getKey() != null && !entry.getKey().isEmpty())
      .filter(entry -> entry.getKey().toLowerCase().endsWith(PORT_HEADER_SUFFIX))
      .forEach(entry -> processPortHeader(entry.getKey(), entry.getValue()));
  }

  private void processPortHeader(String headerName, String portValue) {
    if (portValue != null && !portValue.trim().isEmpty()) {
      try {
        int port = Integer.parseInt(portValue.trim());
        if (port >= MIN_PORT && port <= MAX_PORT) {
          setAttribute(headerName, port);
          log.info("Port configuration completed - Header: {}, Port: {}", headerName, port);
        } else {
          log.warn("Invalid port number - Header: {}, Port: {}", headerName, port);
        }
      } catch (NumberFormatException e) {
        log.warn("Invalid port value - Header: {}, Value: {}", headerName, portValue);
      }
    } else {
      log.info("Port value is missing - Header: {}", headerName);
    }
  }
}