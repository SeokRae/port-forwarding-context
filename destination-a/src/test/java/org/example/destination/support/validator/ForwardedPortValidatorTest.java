package org.example.destination.support.validator;

import org.example.destination.core.props.ForwardPortProperties;
import org.example.inbound.support.validator.ForwardedPortValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ForwardedPortValidatorTest {

  @Autowired
  private ForwardPortProperties properties;

  private ForwardedPortValidator validator;

  @BeforeEach
  void setUp() {
    validator = new ForwardedPortValidator(properties);
  }

  @Test
  @DisplayName("유효한 포워딩 정보 검증")
  void validForwardedPort() {
    assertThat(validator.isValidForwardedPort("service-a-forwarded-port", "8081"))
      .isTrue();
  }

  @ParameterizedTest
  @MethodSource("invalidPortTestCases")
  @DisplayName("유효하지 않은 포트 값 검증")
  void invalidPortValue(String headerName, String portValue, String testCase) {
    assertThat(validator.isValidForwardedPort(headerName, portValue))
      .as(testCase)
      .isFalse();
  }

  private static Stream<Arguments> invalidPortTestCases() {
    return Stream.of(
      Arguments.of("service-a-forwarded-port", "0", "범위 미만의 포트"),
      Arguments.of("service-a-forwarded-port", "65536", "범위 초과의 포트"),
      Arguments.of("service-a-forwarded-port", "abc", "숫자가 아닌 포트"),
      Arguments.of("service-a-forwarded-port", "", "빈 포트 값"),
      Arguments.of("service-a-forwarded-port", null, "null 포트 값"),
      Arguments.of("invalid-header", "8081", "잘못된 헤더 이름"),
      Arguments.of(null, "8081", "null 헤더 이름"),
      Arguments.of("", "8081", "빈 헤더 이름")
    );
  }

  @ParameterizedTest
  @MethodSource("validPortTestCases")
  @DisplayName("유효한 포트 값 검증")
  void validPortValue(String headerName, String portValue, String testCase) {
    assertThat(validator.isValidForwardedPort(headerName, portValue))
      .as(testCase)
      .isTrue();
  }

  private static Stream<Arguments> validPortTestCases() {
    return Stream.of(
      Arguments.of("service-a-forwarded-port", "8081", "최소 허용 포트"),
      Arguments.of("service-a-forwarded-port", "8082", "최대 허용 포트")
    );
  }
}