package org.example.source.support.http;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.source.core.props.ServiceProperties;
import org.example.source.support.builder.UrlTemplateBuilder;
import org.example.source.support.context.ForwardedPortContext;
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
    HttpHeaders httpHeaders,
    HttpMethod httpMethod,
    Map<String, Object> pathVariables,
    String requestBody,
    Class<R> responseType
  ) {

    String headerKey = serviceProperties.getA().getHeader().getKey();

    HttpHeaders headers = createHeaders(httpHeaders, headerKey);
    Integer forwardedPort = getForwardedPort(headerKey);

    HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

    String uriString = urlTemplateBuilder.buildUriComponents(domain, path, forwardedPort, pathVariables).toUriString();

    log.info("[Request] URI: {}, Method: {}, Headers: {}, Body: {}", uriString, httpMethod, headers, requestBody);
    try {

      ResponseEntity<R> responseEntity = restTemplate.exchange(
        uriString,
        httpMethod,
        requestEntity,
        responseType
      );

      if (responseEntity.getBody() != null) {
        log.info("[Response] Status: {}, Body: {}", responseEntity.getStatusCode(), responseEntity.getBody());
      } else {
        log.info("[Response] Status: {}", responseEntity.getStatusCode());
      }

      return responseEntity.getBody() != null ? responseEntity : null;

    } catch (HttpClientErrorException e) {
      log.warn("[Request] URI: {}, Method: {}, Headers: {}, Body: {}, Response: {}", uriString, httpMethod, httpHeaders, requestBody, e.getResponseBodyAsString());
    } catch (HttpServerErrorException e) {
      log.error("[Request] URI: {}, Method: {}, Headers: {}, Body: {}, Response: {}", uriString, httpMethod, httpHeaders, requestBody, e.getResponseBodyAsString());
    } catch (ResourceAccessException e) {
      log.error("[Request] URI: {}, Method: {}, Headers: {}, Body: {}, Response: {}", uriString, httpMethod, httpHeaders, requestBody, e.getMessage());
    } catch (Exception e) {
      log.error("[Request] URI: {}, Method: {}, Headers: {}, Body: {}, Response: {}", uriString, httpMethod, httpHeaders, requestBody, e.getMessage());
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

}
