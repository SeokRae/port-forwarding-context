package org.example.source.core.props;


import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.List;

@Slf4j
@Getter
@ToString
@ConfigurationProperties(prefix = "service")
public class ServiceProperties {

  private final A a;

  @ConstructorBinding
  public ServiceProperties(A a) {
    this.a = a;
    log.info("{}", this);
  }

  @Getter
  @ToString
  public static class A {
    private final String name;
    private final String domain;
    private final Header header;

    public A(String name, String domain, Header header) {
      this.name = name;
      this.domain = domain;
      this.header = header;
    }

  }

  @Getter
  @ToString
  public static class Header {
    private final String key;
    private final List<Integer> ports;

    public Header(String key, List<Integer> ports) {
      this.key = key;
      this.ports = ports;
    }
  }
}