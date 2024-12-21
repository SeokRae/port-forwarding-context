package org.example.source.support.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
class RequestContextTest {

  @AfterEach
  void cleanup() {
    RequestContext.getContext().clear();
  }

  private static Stream<TestCase> portTestCases() {
    return Stream.of(
      new TestCase("x-forwarded-port", 8080, true),
      new TestCase("proxy-forwarded-port", 8081, true),
      new TestCase("normal-port", 8082, false)
    );
  }

  private static Stream<ThreadTestCase> threadTestCases() {
    return Stream.of(
      new ThreadTestCase("x-forwarded-port", 8080, 9090),
      new ThreadTestCase("proxy-forwarded-port", 8081, 9091)
    );
  }

  private static Stream<ContextTestCase> contextTestCases() {
    return Stream.of(
      new ContextTestCase("x-forwarded-port", 8080),
      new ContextTestCase("proxy-forwarded-port", 8081)
    );
  }

  private static class TestCase {
    String key;
    int port;
    boolean shouldStore;

    TestCase(String key, int port, boolean shouldStore) {
      this.key = key;
      this.port = port;
      this.shouldStore = shouldStore;
    }
  }

  private static class ThreadTestCase {
    String key;
    int mainPort;
    int threadPort;

    ThreadTestCase(String key, int mainPort, int threadPort) {
      this.key = key;
      this.mainPort = mainPort;
      this.threadPort = threadPort;
    }
  }

  private static class ContextTestCase {
    String key;
    int port;

    ContextTestCase(String key, int port) {
      this.key = key;
      this.port = port;
    }
  }

  @ParameterizedTest
  @MethodSource("portTestCases")
  void 포트_키_저장_테스트(TestCase testCase) {
    // given
    RequestContext context = RequestContext.getContext();

    // when
    context.setAttribute(testCase.key, testCase.port);

    // then
    if (testCase.shouldStore) {
      context
        .getAttribute(testCase.key, Integer.class)
        .ifPresent(port -> {
          assertThat(port).isEqualTo(testCase.port);
        });
    } else {
      context
        .getAttribute(testCase.key, Integer.class)
        .ifPresent(port -> {
          assertThat(port).isNotEqualTo(testCase.port);
        });
    }
  }

  @ParameterizedTest
  @MethodSource("threadTestCases")
  void 스레드별로_독립적인_컨텍스트를_가진다(ThreadTestCase testCase) throws InterruptedException {
    // given
    RequestContext context1 = RequestContext.getContext();
    context1.setAttribute(testCase.key, testCase.mainPort);

    // when
    Thread thread = new Thread(() -> {
      RequestContext context2 = RequestContext.getContext();
      context2.setAttribute(testCase.key, testCase.threadPort);

      // then
      RequestContext.getContext()
        .getAttribute(testCase.key, Integer.class)
        .ifPresent(port -> assertThat(port).isEqualTo(testCase.threadPort));
    });

    thread.start();
    thread.join();

    // then
    RequestContext.getContext()
      .getAttribute(testCase.key, Integer.class)
      .ifPresent(port -> assertThat(port).isEqualTo(testCase.mainPort));
  }

  @ParameterizedTest
  @MethodSource("contextTestCases")
  void clear_호출시_컨텍스트가_초기화된다(ContextTestCase testCase) {
    // given
    RequestContext context = RequestContext.getContext();
    context.setAttribute(testCase.key, testCase.port);

    // when
    context.clear();

    // then
    context.getAttribute(testCase.key, Integer.class)
      .ifPresent(port -> assertThat(port).isEqualTo(testCase.port));
  }

  @ParameterizedTest
  @MethodSource("contextTestCases")
  void getCurrentContext로_현재_스레드의_컨텍스트를_가져온다(ContextTestCase testCase) {
    // given
    RequestContext context = RequestContext.getContext();
    context.setAttribute(testCase.key, testCase.port);

    // when
    RequestContext currentContext = RequestContext.getContext();

    // then
    context.getAttribute(testCase.key, Integer.class)
      .ifPresent(port -> assertThat(port).isEqualTo(testCase.port));
  }
}