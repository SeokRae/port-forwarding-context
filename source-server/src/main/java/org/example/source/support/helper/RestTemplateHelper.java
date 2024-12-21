package org.example.source.support.helper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.source.support.builder.UrlTemplateBuilder;
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
public class RestTemplateHelper {

  private final RestTemplate restTemplate;
  private final UrlTemplateBuilder urlTemplateBuilder;

  public <R> ResponseEntity<R> postRequest(
    String domain,
    String path,
    Integer port,
    HttpHeaders httpHeaders,
    HttpMethod httpMethod,
    Map<String, Object> pathVariables,
    String requestBody,
    Class<R> responseType
  ) {
    HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, httpHeaders);
    String uriString = urlTemplateBuilder.buildUriComponents(domain, path, port, pathVariables).toUriString();

    log.info("[Request] URI: {}, Method: {}, Headers: {}, Body: {}", uriString, httpMethod, httpHeaders, requestBody);
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
