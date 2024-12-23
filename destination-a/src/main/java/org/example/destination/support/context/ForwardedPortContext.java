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
  private static final ThreadLocal<ContextHolder> CONTEXT = ThreadLocal.withInitial(ContextHolder::new);

  public static void setAttribute(String key, int port) {
    if (key == null) {
      log.warn("Attempted to set attribute with null key");
      return;
    }
    CONTEXT.get().setAttribute(key, port);
  }

  public static Optional<Integer> getAttribute(String key) {
    if (key == null) {
      log.warn("Attempted to get attribute with null key");
      return Optional.empty();
    }
    return CONTEXT.get().getAttribute(key);
  }

  public static Map<String, Integer> getAttributes() {
    return CONTEXT.get().getAttributes();
  }

  public static void removeAttribute(String key) {
    if (key == null) {
      log.warn("Attempted to remove attribute with null key");
      return;
    }
    CONTEXT.get().removeAttribute(key);
  }

  public static void clear() {
    CONTEXT.remove();
  }

  private static class ContextHolder {
    private final Map<String, Integer> contextData = new ConcurrentHashMap<>();

    private void setAttribute(String key, int port) {
      contextData.put(key, port);
      log.debug("Port value set - Key: {}, Port: {}", key, port);
    }

    private Optional<Integer> getAttribute(String key) {
      return Optional.ofNullable(contextData.get(key));
    }

    private Map<String, Integer> getAttributes() {
      return Collections.unmodifiableMap(contextData);
    }

    private void removeAttribute(String key) {
      contextData.remove(key);
    }
  }
}