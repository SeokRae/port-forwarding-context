package org.example.destination.core.filter;


import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.destination.support.context.ForwardedPortContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ForwardedPortProcessingFilter implements Filter {
  private static final int RESERVED_PORT_END = 1023;
  private static final int MIN_PORT = 1;
  private static final int MAX_PORT = 65535;
  //  알파벳 대소문자(a-z, A-Z), 숫자(0-9), 밑줄(_), 하이픈(-) 중 하나의 문자를 허용.
  private static final Pattern FORWARDED_PORT_HEADER_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+-forwarded-port$");

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    throws IOException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    ForwardedPortContext context = ForwardedPortContext.getContext();
    long startTime = System.currentTimeMillis();

    try {
      // 요청 시작 로깅
      logRequestStart(httpRequest);
      // 헤더 정보 저장 로직
      extractAndProcessPorts(httpRequest);
      chain.doFilter(request, response);
    } catch (Exception e) {
      handleException(httpResponse, e);
    } finally {
      logRequestEnd(startTime);
      context.clear();
    }
  }

  private void extractAndProcessPorts(HttpServletRequest request) {
    Collections.list(request.getHeaderNames()).stream()
      .filter(StringUtils::hasText)
      .filter(this::isValidPortHeader)
      .forEach(headerName -> processPortHeader(request, headerName));
  }

  private boolean isValidPortHeader(String headerName) {
    return FORWARDED_PORT_HEADER_PATTERN.matcher(headerName).matches();
  }

  private void processPortHeader(HttpServletRequest request, String headerName) {
    String portValue = request.getHeader(headerName);
    if (!StringUtils.hasText(portValue)) {
      log.warn("Port value missing for header: {}", headerName);
      return;
    }
    try {
      int port = Integer.parseInt(portValue.trim());
      if (isValidPort(port)) {
        ForwardedPortContext.getContext().setAttribute(headerName, port);
        log.info("Port set - Header: {}, Port: {}", headerName, port);
      } else {
        log.warn("Invalid port range - Header: {}, Port: {}", headerName, port);
      }
    } catch (NumberFormatException e) {
      log.warn("Invalid port value - Header: {}, Value: {}", headerName, portValue);
    }
  }

  private boolean isValidPort(int port) {
    // 예약 포트를 포함한 범위 검증
    if (port < MIN_PORT || port > MAX_PORT) {
      return false;
    }
    // 예약 포트 제한
    if (port <= RESERVED_PORT_END) {
      log.warn("Port is within the reserved range: {}", port);
      return false;
    }
    return true;
  }

  private void handleException(HttpServletResponse response, Exception e) throws IOException {
    log.error("Error processing port headers: {}", e.getMessage(), e);
    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    response.getWriter().write("Invalid forwarded-port header format");
  }

  private void logRequestStart(HttpServletRequest request) {
    String headers = Collections.list(request.getHeaderNames()).stream()
      .collect(Collectors.toMap(
        headerName -> headerName,
        request::getHeader
      ))
      .toString();

    log.info("Request Start - URI: {}, Method: {}, Remote: {}, Headers: {}",
      request.getRequestURI(), request.getMethod(), request.getRemoteAddr(), headers);
  }

  private void logRequestEnd(long startTime) {
    log.info("Request End - Processing Time: {} ms", System.currentTimeMillis() - startTime);
  }
}