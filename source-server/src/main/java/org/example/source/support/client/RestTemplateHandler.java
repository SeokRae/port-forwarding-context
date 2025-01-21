package org.example.source.support.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.source.support.builder.UrlTemplateBuilder;
import org.example.source.support.handler.HttpHeaderHandler;
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
  // 헤더 만들기, 포트가져오기 -> Context
  private final HttpHeaderHandler httpHeaderHandler;

  public <R> ResponseEntity<R> postRequest(
    String domain,
    String path,
    HttpHeaders httpHeaders,
    HttpMethod httpMethod,
    Map<String, Object> pathVariables,
    String requestBody,
    Class<R> responseType
  ) {

    HttpHeaders headers = httpHeaderHandler.createHeaders(httpHeaders);
    Integer forwardedPort = httpHeaderHandler.getForwardedPort().orElse(null);

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

}
