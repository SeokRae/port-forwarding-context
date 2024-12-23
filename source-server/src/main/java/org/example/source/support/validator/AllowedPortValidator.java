package org.example.source.support.validator;

import org.example.destination.core.props.ServiceProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AllowedPortValidator implements PortValidator {
  private final List<Integer> allowedPorts;

  public AllowedPortValidator(ServiceProperties serviceProperties) {
    this.allowedPorts = serviceProperties.getB().getHeader().getPorts();
  }

  @Override
  public boolean isValidPort(int port) {
    return allowedPorts.contains(port);
  }
} 