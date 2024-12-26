package org.example.destination.core.props;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ForwardPortPropertiesTest {

  @Configuration
  @EnableConfigurationProperties(ForwardPortProperties.class)
  static class ForwardPortPropertiesTestConfig {
  }

  @Autowired
  private ForwardPortProperties forwardPortProperties;

  @Test
  @DisplayName("포워딩 포트 프로퍼티가 정상적으로 로드되는지 확인한다")
  void forwardPropertiesLoadTest() {
    assertThat(forwardPortProperties)
      .isNotNull()
      .satisfies(properties -> {
        assertThat(properties.getHeaders())
          .isNotNull()
          .satisfies(headers -> {
            assertThat(headers.getPatterns())
              .isNotNull()
              .satisfies(patterns -> {
                assertThat(patterns.getSuffix()).isEqualTo("forwarded-port");
                assertThat(patterns.getPattern()).isEqualTo("^[a-zA-Z0-9-]+-forwarded-port$");
              });

            assertThat(headers.getPorts())
              .isNotNull()
              .satisfies(ports -> {
                assertThat(ports.getPattern()).isEqualTo("^[0-9]+$");
                assertThat(ports.getRange())
                  .isNotNull()
                  .satisfies(range -> {
                    assertThat(range.getMin()).isEqualTo(8081);
                    assertThat(range.getMax()).isEqualTo(8082);
                  });
              });
          });
      });
  }
}