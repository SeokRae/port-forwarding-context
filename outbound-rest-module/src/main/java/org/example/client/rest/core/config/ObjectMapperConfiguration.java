package org.example.client.rest.core.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class ObjectMapperConfiguration {

  @Bean
  public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
    ObjectMapper objectMapper = builder.createXmlMapper(false).build();

    /* 빈 객체 직렬화 실패 방지 */
    objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    /* 알 수 없는 속성 무시 */
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    /* null 값 필드 제외 */
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

    /* camelCase 설정 (기본값이지만 명시적으로 설정) */
    objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);

    return objectMapper;
  }
}