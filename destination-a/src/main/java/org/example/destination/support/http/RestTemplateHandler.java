package org.example.destination.support.http;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.destination.core.props.ServiceProperties;
import org.example.destination.support.builder.UrlTemplateBuilder;
import org.example.destination.support.context.ForwardedPortContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestTemplateHandler {

  private final RestTemplate restTemplate;
  private final UrlTemplateBuilder urlTemplateBuilder;
  private final ServiceProperties serviceProperties;

  public <R> ResponseEntity<R> postRequest(
    String domain,
    String path,
    HttpHeaders originalHeaders,
    HttpMethod httpMethod,
    Map<String, Object> pathVariables,
    String requestBody,
    Class<R> responseType
  ) {

    String headerKey = serviceProperties.getB().getHeader().getKey();

    HttpHeaders headers = createHeaders(originalHeaders, headerKey);
    Integer forwardedPort = getForwardedPort(headerKey);

    HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

    String uriString = urlTemplateBuilder.buildUriComponents(domain, path, forwardedPort, pathVariables).toUriString();

    log.info("[Request] URI: {}, Method: {}, Headers: {}, Body: {}",
      uriString, httpMethod, headers, requestBody);

    try {
      ResponseEntity<R> responseEntity = restTemplate.exchange(
        uriString,
        httpMethod,
        requestEntity,
        responseType
      );

      logResponse(responseEntity);
      return responseEntity.getBody() != null ? responseEntity : null;

    } catch (HttpClientErrorException e) {
      logClientError(uriString, httpMethod, headers, requestBody, e);
    } catch (HttpServerErrorException e) {
      logServerError(uriString, httpMethod, headers, requestBody, e);
    } catch (ResourceAccessException e) {
      logResourceAccessError(uriString, httpMethod, headers, requestBody, e);
    } catch (Exception e) {
      logUnexpectedError(uriString, httpMethod, headers, requestBody, e);
      throw e;
    }

    throw new RuntimeException("Failed to request");
  }

  private HttpHeaders createHeaders(HttpHeaders originalHeaders, String headerKey) {
    HttpHeaders headers = new HttpHeaders();
    headers.addAll(originalHeaders);
    addForwardedPortHeaders(headers, headerKey);
    return headers;
  }

  private void addForwardedPortHeaders(HttpHeaders headers, String headerKey) {
    Integer port = getForwardedPort(headerKey);

    // 포트 관련 헤더 추가
    ForwardedPortContext.getAttributes()
      .entrySet()
      .stream()
      .filter(entry -> !entry.getKey().equals(headerKey))
      .forEach(entry -> headers.add(entry.getKey(), String.valueOf(entry.getValue())));

    if (port != null) {
      log.info("Port forwarded from header key: {}, port: {}", headerKey, port);
      headers.add(headerKey, String.valueOf(port));
    }
  }

  private Integer getForwardedPort(String headerKey) {
    return ForwardedPortContext.getAttribute(headerKey).orElse(null);
  }

  private <R> void logResponse(ResponseEntity<R> responseEntity) {
    if (responseEntity.getBody() != null) {
      log.info("[Response] Status: {}, Body: {}",
        responseEntity.getStatusCode(), responseEntity.getBody());
    } else {
      log.info("[Response] Status: {}", responseEntity.getStatusCode());
    }
  }

  private void logClientError(String uri, HttpMethod method, HttpHeaders headers,
                              String body, HttpClientErrorException e) {
    log.warn("[Request] URI: {}, Method: {}, Headers: {}, Body: {}, Response: {}",
      uri, method, headers, body, e.getResponseBodyAsString());
  }

  private void logServerError(String uri, HttpMethod method, HttpHeaders headers,
                              String body, HttpServerErrorException e) {
    log.error("[Request] URI: {}, Method: {}, Headers: {}, Body: {}, Response: {}",
      uri, method, headers, body, e.getResponseBodyAsString());
  }

  private void logResourceAccessError(String uri, HttpMethod method, HttpHeaders headers,
                                      String body, ResourceAccessException e) {
    log.error("[Request] URI: {}, Method: {}, Headers: {}, Body: {}, Response: {}",
      uri, method, headers, body, e.getMessage());
  }

  private void logUnexpectedError(String uri, HttpMethod method, HttpHeaders headers,
                                  String body, Exception e) {
    log.error("[Request] URI: {}, Method: {}, Headers: {}, Body: {}, Response: {}",
      uri, method, headers, body, e.getMessage());
  }
}
