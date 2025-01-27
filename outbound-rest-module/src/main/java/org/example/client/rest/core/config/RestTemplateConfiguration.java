package org.example.client.rest.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.client.rest.core.interceptor.RestTemplateLoggingInterceptor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfiguration {

  @Bean
  public RestTemplateLoggingInterceptor loggingRequestInterceptor() {
    return new RestTemplateLoggingInterceptor();
  }

  @Bean
  public RestTemplate restTemplate(
    RestTemplateBuilder restTemplateBuilder, ObjectMapper objectMapper
  ) {
    return restTemplateBuilder
      .requestFactory(() -> new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()))
      // 5초 동안 응답이 없으면 예외 발생
      .setConnectTimeout(Duration.ofSeconds(5))
      // 15초 동안 데이터가 안오면 예외 발생
      .setReadTimeout(Duration.ofSeconds(10))
      .additionalInterceptors(loggingRequestInterceptor())
      .additionalMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
      .build();
  }


}
