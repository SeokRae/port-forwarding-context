package org.example.destination.support.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.destination.support.context.ForwardedPortContext;
import org.example.destination.support.validator.PortValidator;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ForwardedPortHandler {

  private final PortValidator portValidator;

  /**
   * 헤더 정보를 검증하고 Context에 저장
   */
  public void validateAndStore(String headerName, String headerValue) {
    if (!portValidator.isValidForwardedPort(headerName, headerValue)) {
      log.warn("Invalid forwarded port - Header: {}, Value: {}", headerName, headerValue);
      return;
    }
    int port = Integer.parseInt(headerValue.trim());
    ForwardedPortContext.setAttribute(headerName, port);
    log.info("Port stored in context - Header: {}, Port: {}", headerName, port);
  }

}