package org.example.source.support.validator;


import lombok.extern.slf4j.Slf4j;
import org.example.source.core.props.ServiceProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 해당 서비스에서 유효한 포트번호에 대한 검증을 수행하는 클래스
 */
@Slf4j
@Component
public class ForwardedPortValidator implements PortValidator {
  private final List<Integer> allowedPorts;

  public ForwardedPortValidator(ServiceProperties serviceProperties) {
    this.allowedPorts = serviceProperties.getA().getHeader().getPorts();
    log.info("{}", this);
  }

  @Override
  public boolean isValidPort(int port) {
    return allowedPorts.contains(port);
  }
} 