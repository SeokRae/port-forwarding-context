package org.example.source.core.filter;


import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.source.support.context.ForwardedPortContext;
import org.example.source.support.validator.ForwardedPortValidator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ForwardedPortProcessingFilter implements Filter {
  private final ForwardedPortValidator validator;

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    long startTime = System.currentTimeMillis();

    try {
      logRequestStart(httpRequest);
      processForwardedPorts(httpRequest);
      chain.doFilter(request, response);
    } catch (Exception e) {
      log.error("Error occurred while processing request", e);
    } finally {
      logRequestEnd(startTime);
      ForwardedPortContext.clear();
    }
  }

  private void processForwardedPorts(HttpServletRequest request) {
    Collections.list(request.getHeaderNames()).stream()
      .filter(StringUtils::hasText)
      .forEach(headerName ->
        validator.validateAndStore(headerName, request.getHeader(headerName))
      );
  }

  private void logRequestStart(HttpServletRequest request) {
    if (log.isInfoEnabled()) {
      String headers = Collections.list(request.getHeaderNames()).stream()
        .collect(Collectors.toMap(
          headerName -> headerName,
          request::getHeader
        ))
        .toString();
      log.info("Request started - Headers: {}", headers);
    }
  }

  private void logRequestEnd(long startTime) {
    if (log.isInfoEnabled()) {
      long duration = System.currentTimeMillis() - startTime;
      log.info("Request completed in {} ms", duration);
    }
  }
}