package org.example.destination.support.helper;

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
public class RestTemplateHelper {

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

    ForwardedPortContext context = ForwardedPortContext.getContext();
    String headerKey = serviceProperties.getB().getHeader().getKey();
    Integer port = context.getAttribute(headerKey, Integer.class).orElse(null);

    context.getAttributes()
      .entrySet()
      .stream()
      .filter(entry -> !entry.getKey().equals(headerKey))
      .forEach(entry -> httpHeaders.add(entry.getKey(), String.valueOf(entry.getValue())));
    if (port == null) {
      log.error("Port information is missing in the context. Header Key: {}", headerKey);
      httpHeaders.set(headerKey, String.valueOf(port));
    }

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
