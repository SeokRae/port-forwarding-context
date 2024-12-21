package org.example.source.core.props;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {ServicePropertiesTest.ServicePropertiesTestConfig.class})
class ServicePropertiesTest {

  @Configuration
  @EnableConfigurationProperties(ServiceProperties.class)
  static class ServicePropertiesTestConfig {
  }

  @Autowired
  private ServiceProperties serviceProperties;

  @Test
  @DisplayName("서비스 프로퍼티가 정상적으로 로드되는지 확인한다")
  void servicePropertiesLoadTest() {
    assertThat(serviceProperties)
      .isNotNull()
      .satisfies(properties -> {
        assertThat(properties.getA())
          .isNotNull()
          .satisfies(a -> {
            assertThat(a.getName()).isEqualTo("service-a");
            assertThat(a.getDomain()).isEqualTo("localhost");

            assertThat(a.getHeader())
              .isNotNull()
              .satisfies(header -> {
                assertThat(header.getKey()).isEqualTo("service-a-forwarded-port");
                assertThat(header.getPorts())
                  .containsExactly(8081, 8082);
              });
          });
      });
  }
}