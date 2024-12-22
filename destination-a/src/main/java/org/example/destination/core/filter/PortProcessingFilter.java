package org.example.destination.core.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.destination.support.context.RequestContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class PortProcessingFilter implements Filter {
  private static final String PORT_HEADER_SUFFIX = "forwarded-port";
  private static final int MIN_PORT = 1;
  private static final int MAX_PORT = 65535;

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    RequestContext context = RequestContext.getContext();
    long startTime = System.currentTimeMillis();

    try {
      preHandle(httpRequest);
      processHandle(httpRequest, request, response, chain);
    } catch (Exception e) {
      log.error("Error occurred during port processing: {}", e.getMessage());
      throw e;
    } finally {
      postHandle(startTime);
      context.clear();
    }
  }

  // 전처리: 요청 시작 로깅 및 초기 설정
  private void preHandle(HttpServletRequest request) {
    log.info("================================================== [Port PreHandle Start] ==================================================");
    logRequestStart(request);
    extractAndProcessForwardedPorts(request);
    log.info("================================================== [Port PreHandle End] ==================================================");
  }

  // 메인 처리: 실제 필터 체인 실행
  private void processHandle(HttpServletRequest httpRequest,
                             ServletRequest request,
                             ServletResponse response,
                             FilterChain chain) throws IOException, ServletException {
    chain.doFilter(request, response);
  }

  // 후처리: 요청 종료 로깅 및 정리
  private void postHandle(long startTime) {
    log.info("================================================== [Port PostHandle End] ==================================================");
    logRequestEnd(startTime);
    log.info("================================================== [Port PostHandle End] ==================================================");
  }

  private void extractAndProcessForwardedPorts(HttpServletRequest request) {
    Collections.list(request.getHeaderNames()).stream()
      .filter(StringUtils::hasText)
      .filter(this::isForwardedPortHeader)
      .forEach(headerName -> processPortHeader(request, headerName));
  }

  private boolean isForwardedPortHeader(String headerName) {
    return headerName.toLowerCase().endsWith(PORT_HEADER_SUFFIX);
  }

  private void processPortHeader(HttpServletRequest request, String headerName) {
    if (!StringUtils.hasText(headerName)) {
      log.warn("Header name is empty: {}", headerName);
      return;
    }

    String portValue = request.getHeader(headerName);
    if (!StringUtils.hasText(portValue)) {
      log.info("Port value is missing. Header: {}", headerName);
      return;
    }

    processPortValue(headerName, portValue.trim());
  }

  private void processPortValue(String headerName, String portValue) {
    try {
      int port = Integer.parseInt(portValue);
      if (isValidPort(port)) {
        RequestContext.getContext().setAttribute(headerName, port);
        log.info("Port configuration completed - Header: {}, Port: {}", headerName, port);
      } else {
        log.warn("Invalid port number - Header: {}, Port: {}", headerName, port);
      }
    } catch (NumberFormatException e) {
      // 유효하지 않은 포트 확인
      log.warn("Invalid port value - Header: {}, Value: {}", headerName, portValue);
    }
  }

  private boolean isValidPort(int port) {
    return port >= MIN_PORT && port <= MAX_PORT;
  }

  private void logRequestStart(HttpServletRequest request) {
    if (log.isInfoEnabled()) {
      log.info("Request URI    : {}", request.getRequestURI());
      log.info("Request Method : {}", request.getMethod());
      log.info("Remote Address : {}", request.getRemoteAddr());
    }
  }

  private void logRequestEnd(long startTime) {
    if (log.isInfoEnabled()) {
      long duration = System.currentTimeMillis() - startTime;
      log.info("[Port Processing End] Processing Time: {} ms", duration);
    }
  }
}
