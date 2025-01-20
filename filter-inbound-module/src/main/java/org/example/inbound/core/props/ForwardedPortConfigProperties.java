package org.example.inbound.core.props;

public interface ForwardedPortConfigProperties {    // 헤더 패턴 관련
  String getHeaderSuffix();

  String getHeaderPattern();

  // 포트 관련
  String getPortPattern();

  Integer getMinPort();

  Integer getMaxPort();
}
