package org.example.source.support.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("포트 처리 예외 테스트")
class ForwardedPortContextExceptionTest {
  private ForwardedPortContext context;

  @BeforeEach
  void setUp() {
    context = ForwardedPortContext.getContext();
  }

  @AfterEach
  void tearDown() {
    context.clear();
  }

  @Test
  @DisplayName("포트 값이 숫자가 아닌 경우 처리되지 않아야 한다")
  void whenPortValueIsNotNumeric_thenShouldNotProcess() {
    // given
    String headerKey = "service-a-forwarded-port";
    String invalidPort = "abc";

    // when
    context.extractAndProcessForwardedPorts(Map.of(headerKey, invalidPort));

    // then
    assertThat(context.getAttribute(headerKey)).isEmpty();
  }

  @Test
  @DisplayName("포트 범위를 벗어난 경우 처리되지 않아야 한다")
  void whenPortValueIsOutOfRange_thenShouldNotProcess() {
    // given
    String headerKey = "service-a-forwarded-port";
    String[] invalidPorts = {"0", "65536", "-1", "999999"};

    // when & then
    Arrays.stream(invalidPorts).forEach(port -> {
      context.extractAndProcessForwardedPorts(Map.of(headerKey, port));
      assertThat(context.getAttribute(headerKey))
        .as("Port %s should not be processed", port)
        .isEmpty();
    });
  }

  @Test
  @DisplayName("헤더 키가 올바르지 않은 경우 처리되지 않아야 한다")
  void whenHeaderKeyIsInvalid_thenShouldNotProcess() {
    // given
    Map<String, String> invalidHeaders = Map.of(
      "invalid-header", "8080",
      "service-a-wrong-suffix", "8080",
      "!invalid-forwarded-port", "8080"
    );

    // when
    context.extractAndProcessForwardedPorts(invalidHeaders);

    // then
    assertThat(context.getAttributes()).isEmpty();
  }

  @Test
  @DisplayName("null 또는 빈 값이 입력된 경우 처리되지 않아야 한다")
  void whenNullOrEmptyValues_thenShouldNotProcess() {
    // given
    String validKey = "service-a-forwarded-port";

    // when & then
    HashMap<String, String> headers = new HashMap<>();
    headers.put(validKey, null);
    context.extractAndProcessForwardedPorts(headers);
    assertThat(context.getAttribute(validKey)).isEmpty();

    context.extractAndProcessForwardedPorts(Map.of(validKey, ""));
    assertThat(context.getAttribute(validKey)).isEmpty();

    context.extractAndProcessForwardedPorts(Map.of(validKey, "  "));
    assertThat(context.getAttribute(validKey)).isEmpty();
  }

  @Test
  @DisplayName("여러 예외 케이스가 혼합된 경우 유효한 포트만 처리되어야 한다")
  void whenMixedValidAndInvalidPorts_thenShouldOnlyProcessValidPorts() {
    // given
    Map<String, String> mixedHeaders = new HashMap<>();
    mixedHeaders.put("service-a-forwarded-port", "8081");  // valid
    mixedHeaders.put("service-b-forwarded-port", "abc");   // invalid format
    mixedHeaders.put("invalid-header", "8082");            // invalid key
    mixedHeaders.put("service-c-forwarded-port", "0");     // invalid range
    mixedHeaders.put("service-d-forwarded-port", "8084");  // valid

    // when
    context.extractAndProcessForwardedPorts(mixedHeaders);

    // then
    assertThat(context.getAttributes())
      .hasSize(2)
      .containsEntry("service-a-forwarded-port", 8081)
      .containsEntry("service-d-forwarded-port", 8084);
  }
}