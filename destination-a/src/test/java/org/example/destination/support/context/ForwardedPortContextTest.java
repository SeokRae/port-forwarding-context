package org.example.destination.support.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ForwardedPortContext 테스트")
class ForwardedPortContextTest {

  @AfterEach
  void tearDown() {
    ForwardedPortContext.clear();
  }

  @Test
  @DisplayName("포트 값을 저장하고 조회할 수 있다")
  void shouldStoreAndRetrievePortValue() {
    // given
    String key = "test-forwarded-port";
    int port = 8080;

    // when
    ForwardedPortContext.setAttribute(key, port);

    // then
    assertThat(ForwardedPortContext.getAttribute(key))
      .isPresent()
      .hasValue(port);
  }

  @Test
  @DisplayName("존재하지 않는 키로 조회시 빈 Optional을 반환한다")
  void shouldReturnEmptyOptionalForNonExistentKey() {
    assertThat(ForwardedPortContext.getAttribute("non-existent-key")).isEmpty();
  }

  @Test
  @DisplayName("모든 속성을 조회할 수 있다")
  void shouldRetrieveAllAttributes() {
    // given
    String key1 = "test-port-1";
    String key2 = "test-port-2";
    ForwardedPortContext.setAttribute(key1, 8081);
    ForwardedPortContext.setAttribute(key2, 8082);

    // when
    Map<String, Integer> attributes = ForwardedPortContext.getAttributes();

    // then
    assertThat(attributes)
      .hasSize(2)
      .containsEntry(key1, 8081)
      .containsEntry(key2, 8082);
  }

  @Test
  @DisplayName("getAttributes()는 수정 불가능한 Map을 반환한다")
  void shouldReturnUnmodifiableMap() {
    // given
    ForwardedPortContext.setAttribute("test-port", 8080);
    Map<String, Integer> attributes = ForwardedPortContext.getAttributes();

    // when & then
    assertThatThrownBy(() -> attributes.put("new-key", 8081))
      .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  @DisplayName("특정 속성을 제거할 수 있다")
  void shouldRemoveAttribute() {
    // given
    String key = "test-port";
    ForwardedPortContext.setAttribute(key, 8080);

    // when
    ForwardedPortContext.removeAttribute(key);

    // then
    assertThat(ForwardedPortContext.getAttribute(key)).isEmpty();
  }

  @Test
  @DisplayName("clear() 호출 시 모든 속성이 제거된다")
  void shouldClearAllAttributes() {
    // given
    ForwardedPortContext.setAttribute("test-port-1", 8081);
    ForwardedPortContext.setAttribute("test-port-2", 8082);

    // when
    ForwardedPortContext.clear();

    // then
    assertThat(ForwardedPortContext.getAttributes()).isEmpty();
  }

  @Test
  @DisplayName("여러 스레드에서 동시에 접근해도 ThreadLocal 격리가 유지된다")
  void shouldMaintainThreadLocalIsolationUnderHighConcurrency() throws InterruptedException {
    // given
    final int THREAD_COUNT = 100;
    final String TEST_KEY = "test-port";
    Set<Integer> collectedPorts = ConcurrentHashMap.newKeySet();
    ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    try {
      // when
      for (int i = 0; i < THREAD_COUNT; i++) {
        final int port = 8080 + i;
        executor.submit(() -> {
          try {
            ForwardedPortContext.setAttribute(TEST_KEY, port);
            Thread.sleep(ThreadLocalRandom.current().nextInt(10));
            ForwardedPortContext.getAttribute(TEST_KEY).ifPresent(collectedPorts::add);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          } finally {
            ForwardedPortContext.clear();
          }
        });
      }

      executor.shutdown();
      boolean completed = executor.awaitTermination(10, TimeUnit.SECONDS);

      // then
      assertThat(completed).isTrue();
      assertThat(collectedPorts)
        .hasSize(THREAD_COUNT)
        .allSatisfy(port ->
          assertThat(port).isBetween(8080, 8080 + THREAD_COUNT - 1)
        );
    } finally {
      executor.shutdownNow();
    }
  }

  @Test
  @DisplayName("수천 개의 가상 스레드에서 동시에 접근해도 ThreadLocal 격리가 유지된다")
  void shouldMaintainThreadLocalIsolationWithVirtualThreads() throws InterruptedException {
    // given
    final int VIRTUAL_THREAD_COUNT = 10_000;
    final String TEST_KEY = "test-port";
    Set<Integer> collectedPorts = ConcurrentHashMap.newKeySet();
    ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    try {
      // when
      for (int i = 0; i < VIRTUAL_THREAD_COUNT; i++) {
        final int port = 8080 + i;
        executor.submit(() -> {
          try {
            ForwardedPortContext.setAttribute(TEST_KEY, port);
            Thread.sleep(ThreadLocalRandom.current().nextInt(5));
            ForwardedPortContext.getAttribute(TEST_KEY).ifPresent(collectedPorts::add);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          } finally {
            ForwardedPortContext.clear();
          }
        });
      }

      executor.shutdown();
      boolean completed = executor.awaitTermination(15, TimeUnit.SECONDS);

      // then
      assertThat(completed).isTrue();
      assertThat(collectedPorts)
        .hasSize(VIRTUAL_THREAD_COUNT)
        .allSatisfy(port ->
          assertThat(port).isBetween(8080, 8080 + VIRTUAL_THREAD_COUNT - 1)
        );
    } finally {
      executor.shutdownNow();
    }
  }
}