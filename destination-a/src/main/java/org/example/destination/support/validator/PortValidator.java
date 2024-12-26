package org.example.destination.support.validator;

@FunctionalInterface
public interface PortValidator {
  /**
   * 포워딩된 포트 정보(헤더 키와 포트 값)의 유효성을 검증합니다.
   *
   * @param headerName 포트 정보가 포함된 헤더 키
   * @param headerValue       검증할 포트 번호
   * @return 유효한 포워딩 정보인 경우 true
   */
  boolean isValidForwardedPort(String headerName, String headerValue);
} 