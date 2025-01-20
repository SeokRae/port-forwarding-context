package org.example.source.core.props;

import org.example.inbound.core.props.AbstractForwardedPortProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "service.forwarding")
public class ForwardPortProperties extends AbstractForwardedPortProperties {

  public ForwardPortProperties(PortHeader headers) {
    super(headers);
  }
}
