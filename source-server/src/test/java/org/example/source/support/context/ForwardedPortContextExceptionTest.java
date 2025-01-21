package org.example.source.support.context;

import org.example.inbound.infrastructure.context.ForwardedPortContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("포트 처리 예외 테스트")
class ForwardedPortContextExceptionTest {

  @AfterEach
  void tearDown() {
    ForwardedPortContext.clear();
  }

  private static Stream<InvalidPortTestCase> invalidPortTestCases() {
    return Stream.of(
      new InvalidPortTestCase("service-a-forwarded-port", "abc", "숫자가 아닌 포트값"),
      new InvalidPortTestCase("service-a-forwarded-port", "12.34", "소수점이 있는 포트값"),
      new InvalidPortTestCase("service-a-forwarded-port", "", "빈 포트값"),
      new InvalidPortTestCase("service-a-forwarded-port", "포트", "한글 포트값"),
      new InvalidPortTestCase("service-a-forwarded-port", "!@#$", "특수문자 포트값")
    );
  }

  private static Stream<OutOfRangePortTestCase> outOfRangePortTestCases() {
    return Stream.of(
      new OutOfRangePortTestCase("service-a-forwarded-port", -1, "음수 포트"),
      new OutOfRangePortTestCase("service-a-forwarded-port", 0, "0번 포트"),
      new OutOfRangePortTestCase("service-a-forwarded-port", 65536, "최대값 초과 포트"),
      new OutOfRangePortTestCase("service-a-forwarded-port", 99999, "큰 범위 초과 포트")
    );
  }

  private static Stream<InvalidKeyTestCase> invalidKeyTestCases() {
    return Stream.of(
      new InvalidKeyTestCase(null, 8080, "null 키"),
      new InvalidKeyTestCase("", 8080, "빈 문자열 키"),
      new InvalidKeyTestCase("   ", 8080, "공백 문자열 키"),
      new InvalidKeyTestCase("invalid-key", 8080, "잘못된 형식의 키"),
      new InvalidKeyTestCase("service-wrong-suffix", 8080, "잘못된 접미사의 키")
    );
  }

  @ParameterizedTest(name = "{2}")
  @MethodSource("invalidPortTestCases")
  @DisplayName("유효하지 않은 포트값 테스트")
  void whenInvalidPortValue_thenShouldNotProcess(InvalidPortTestCase testCase) {
    // when
    try {
      ForwardedPortContext.setAttribute(testCase.key, Integer.parseInt(testCase.port));
    } catch (NumberFormatException ignored) {
      // 예외 발생 예상됨
    }

    // then
    assertThat(ForwardedPortContext.getAttribute(testCase.key))
      .as("%s '%s'가 처리되지 않아야 함", testCase.description, testCase.port)
      .isEmpty();
  }

  @ParameterizedTest(name = "{2}")
  @MethodSource("outOfRangePortTestCases")
  @DisplayName("범위를 벗어난 포트값 테스트")
  void whenPortValueOutOfRange_thenShouldNotProcess(OutOfRangePortTestCase testCase) {
    // when
    ForwardedPortContext.setAttribute(testCase.key, testCase.port);

    // then
    assertThat(ForwardedPortContext.getAttribute(testCase.key))
      .as("%s %d가 처리되지 않아야 함", testCase.description, testCase.port)
      .isEmpty();
  }

  @ParameterizedTest(name = "{2}")
  @MethodSource("invalidKeyTestCases")
  @DisplayName("유효하지 않은 키 테스트")
  void whenInvalidKey_thenShouldNotProcess(InvalidKeyTestCase testCase) {
    // when
    try {
      ForwardedPortContext.setAttribute(testCase.key, testCase.port);
    } catch (Exception ignored) {
      // null 키의 경우 예외 발생 가능
    }

    // then
    assertThat(ForwardedPortContext.getAttribute(testCase.key))
      .as("%s가 처리되지 않아야 함", testCase.description)
      .isEmpty();
  }

  private static class InvalidPortTestCase {
    String key;
    String port;
    String description;

    InvalidPortTestCase(String key, String port, String description) {
      this.key = key;
      this.port = port;
      this.description = description;
    }
  }

  private static class OutOfRangePortTestCase {
    String key;
    int port;
    String description;

    OutOfRangePortTestCase(String key, int port, String description) {
      this.key = key;
      this.port = port;
      this.description = description;
    }
  }

  private static class InvalidKeyTestCase {
    String key;
    int port;
    String description;

    InvalidKeyTestCase(String key, int port, String description) {
      this.key = key;
      this.port = port;
      this.description = description;
    }
  }
}