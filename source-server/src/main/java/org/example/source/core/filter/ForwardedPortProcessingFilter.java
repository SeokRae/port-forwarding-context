package org.example.source.core.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.source.support.context.ForwardedPortContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ForwardedPortProcessingFilter implements Filter {

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    throws IOException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    ForwardedPortContext context = ForwardedPortContext.getContext();
    long startTime = System.currentTimeMillis();

    try {
      // 요청 시작 로깅
      logRequestStart(httpRequest);
      // 헤더 정보 저장 로직
      extractAndProcessPorts(httpRequest);
      chain.doFilter(request, response);
    } catch (Exception e) {
      log.error("Error occurred while processing request", e);
    } finally {
      logRequestEnd(startTime);
      /* 필수 ForwardedPort에 대한 컨테이너를 비워 메모리 누수 방지 */
      context.clear();
    }
  }

  /**
   * 서블릿 요청의 헤더 정보를 추출하여 포트 정보를 추출하고 처리한다.
   *
   * @param request
   */
  private void extractAndProcessPorts(HttpServletRequest request) {
    Map<String, String> forwardedPortHeaders = Collections.list(request.getHeaderNames()).stream()
      .filter(StringUtils::hasText)
      .collect(Collectors.toMap(headerName -> headerName, request::getHeader));

    ForwardedPortContext.getContext().extractAndProcessForwardedPorts(forwardedPortHeaders);
  }

  /**
   * 요청 헤더 정보 로깅
   *
   * @param request
   */
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