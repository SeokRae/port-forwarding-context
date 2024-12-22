package org.example.destination.core.props;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Slf4j
@Getter
@ToString
@ConfigurationProperties(prefix = "uris")
public class UrisProperties {

  private final String destination;

  public UrisProperties(String destination) {
    this.destination = destination;
    log.info("{}", this);
  }
}
