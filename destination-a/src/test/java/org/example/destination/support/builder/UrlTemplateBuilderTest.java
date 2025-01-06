package org.example.destination.support.builder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.util.UriComponentsBuilder;

class UrlTemplateBuilderTest {

  @DisplayName(value = "UriComponentsBuilder를 이용한 URI 생성 테스트")
  @Test
  void testCase1() {
    String uriString = UriComponentsBuilder
      .fromUriString("https://example.com")
      .path("/path/{id}")
      .buildAndExpand("1")
      .toUriString();
    System.out.println("uriString = " + uriString);
  }
}