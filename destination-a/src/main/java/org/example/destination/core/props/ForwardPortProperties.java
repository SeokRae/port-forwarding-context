package org.example.destination.core.props;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Slf4j
@Getter
@ConfigurationProperties(prefix = "service.forwarding")
public class ForwardPortProperties {
  private final ForwardPortHeader headers;

  public ForwardPortProperties(ForwardPortHeader headers) {
    this.headers = headers;
  }

}
