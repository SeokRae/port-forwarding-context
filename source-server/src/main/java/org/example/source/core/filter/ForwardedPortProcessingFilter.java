package org.example.source.core.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.source.support.context.ForwardedPortContext;
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
  private static final int MIN_PORT = 1;
  private static final int MAX_PORT = 65535;
  private static final Pattern FORWARDED_PORT_HEADER_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+-forwarded-port$");

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    ForwardedPortContext context = ForwardedPortContext.getContext();
    long startTime = System.currentTimeMillis();

    try {
      logRequestStart(httpRequest);
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
    return port >= MIN_PORT && port <= MAX_PORT;
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