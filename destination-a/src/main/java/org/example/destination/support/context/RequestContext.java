package org.example.destination.support.context;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestContext {
  private static final ThreadLocal<RequestContext> CONTEXT = new ThreadLocal<>();

  private final Map<String, Object> contextData = new ConcurrentHashMap<>();

  public static RequestContext getContext() {
    RequestContext context = CONTEXT.get();
    if (context == null) {
      context = new RequestContext();
      CONTEXT.set(context);
    }
    return context;
  }

  // 데이터 저장
  public void setAttribute(String key, Object value) {
    if (value != null) {
      contextData.put(key, value);
    }
  }

  // 데이터 조회
  @SuppressWarnings("unchecked")
  public <T> Optional<T> getAttribute(String key, Class<T> type) {
    Object value = contextData.get(key);
    if (value != null && type.isInstance(value)) {
      return Optional.of((T) value);
    }
    return Optional.empty();
  }

  // 모든 데이터 조회
  public Map<String, Object> getAttributes() {
    return Collections.unmodifiableMap(contextData);
  }

  // 특정 키의 데이터 삭제 (요청 Scope의 헤더 정보를 삭제 해야 할 필요가 있을까?)
  public void removeAttribute(String key) {
    contextData.remove(key);
  }

  public void clear() {
    contextData.clear();
    CONTEXT.remove();
  }

  @Override
  public String toString() {
    return "RequestContext{contextData=" + contextData + "}";
  }

  public Map<String, Object> getAttributesExcluding(String... excludeKeys) {
    return contextData.entrySet().stream()
      .filter(entry -> !Arrays.asList(excludeKeys).contains(entry.getKey()))
      .collect(Collectors.toMap(
        Map.Entry::getKey,
        Map.Entry::getValue,
        (e1, e2) -> e1,
        HashMap::new
      ));
  }
}

