package org.example.source.support.validator;

import org.example.source.core.props.ServiceProperties;
import org.example.source.support.context.ForwardedPortContext;
import org.junit.jupiter.api.AfterEach;
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
@DisplayName("ForwardedPortValidator 테스트")
class ForwardedPortValidatorTest {

    @Autowired
    private ServiceProperties serviceProperties;

    @Autowired
    private ForwardedPortValidator validator;

    @AfterEach
    void tearDown() {
        ForwardedPortContext.clear();
    }

    @Test
    @DisplayName("유효한 헤더와 포트값이 주어지면 컨텍스트에 저장된다")
    void whenValidHeaderAndPort_thenStoresInContext() {
        // given
        String headerName = "service-a-forwarded-port";
        String headerValue = "8081";

        // when
        validator.validateAndStore(headerName, headerValue);

        // then
        assertThat(ForwardedPortContext.getAttribute(headerName))
            .isPresent()
            .hasValue(8081);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidHeaderPatterns")
    @DisplayName("유효하지 않은 헤더 패턴이 주어지면 컨텍스트에 저장되지 않는다")
    void whenInvalidHeaderPattern_thenDoesNotStore(String testName, String headerName, String headerValue) {
        // when
        validator.validateAndStore(headerName, headerValue);

        // then
        assertThat(ForwardedPortContext.getAttribute(headerName)).isEmpty();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidPortValues")
    @DisplayName("유효하지 않은 포트값이 주어지면 컨텍스트에 저장되지 않는다")
    void whenInvalidPortValue_thenDoesNotStore(String testName, String headerName, String headerValue) {
        // when
        validator.validateAndStore(headerName, headerValue);

        // then
        assertThat(ForwardedPortContext.getAttribute(headerName)).isEmpty();
    }

    @Test
    @DisplayName("허용되지 않은 포트가 주어지면 컨텍스트에 저장되지 않는다")
    void whenNotAllowedPort_thenDoesNotStore() {
        // given
        String headerName = "service-a-forwarded-port";
        String headerValue = "8083"; // 허용되지 않은 포트

        // when
        validator.validateAndStore(headerName, headerValue);

        // then
        assertThat(ForwardedPortContext.getAttribute(headerName)).isEmpty();
    }

    private static Stream<Arguments> invalidHeaderPatterns() {
        return Stream.of(
            Arguments.of("null 헤더", null, "8081"),
            Arguments.of("빈 헤더", "", "8081"),
            Arguments.of("잘못된 접미사", "service-a-wrong-suffix", "8081"),
            Arguments.of("잘못된 형식", "invalid-header", "8081"),
            Arguments.of("특수문자 포함", "service@a-forwarded-port", "8081")
        );
    }

    private static Stream<Arguments> invalidPortValues() {
        return Stream.of(
            Arguments.of("범위 미만 포트", "service-a-forwarded-port", "0"),
            Arguments.of("범위 초과 포트", "service-a-forwarded-port", "65536"),
            Arguments.of("음수 포트", "service-a-forwarded-port", "-1"),
            Arguments.of("문자열 포트", "service-a-forwarded-port", "abc"),
            Arguments.of("빈 포트값", "service-a-forwarded-port", ""),
            Arguments.of("null 포트값", "service-a-forwarded-port", null),
            Arguments.of("소수점 포트값", "service-a-forwarded-port", "8081.5"),
            Arguments.of("특수문자 포트값", "service-a-forwarded-port", "8081!")
        );
    }
}