package org.example.client.rest.infrastructures.factory;

import lombok.extern.slf4j.Slf4j;
import org.example.client.rest.infrastructures.AbstractRequestStrategy;
import org.example.client.rest.infrastructures.context.RequestContext;
import org.example.client.rest.infrastructures.support.UrlTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Component
public class JsonRequestStrategy extends AbstractRequestStrategy {

  private static final MediaType MEDIA_TYPE = MediaType.APPLICATION_JSON;

  public JsonRequestStrategy(RestTemplate restTemplate, UrlTemplateBuilder urlTemplateBuilder) {
    super(restTemplate, urlTemplateBuilder);
  }

  @Override
  protected <R> R doExecute(RequestContext<R> context) {
    log.info("Executing request: {} {}", context.getDomain(), context.getPath());
    HttpHeaders headers = new HttpHeaders();
    headers.addAll(context.getHttpHeaders());
    headers.setContentType(MEDIA_TYPE);

    String uriString = urlTemplateBuilder.buildUriComponents(
      context.getDomain(),
      context.getPath(),
      context.getPort(),
      context.getPathVariables()
    ).toUriString();


    log.info("[Request] URI: {}, Method: {}, Headers: {}, Body: {}", uriString, context.getHttpMethod(), headers, context.getRequestBody());
    try {
      ResponseEntity<R> responseEntity = restTemplate.exchange(
        uriString,
        context.getHttpMethod(),
        new HttpEntity<>(context.getRequestBody(), headers),
        context.getResponseType()
      );

      log.info("[Response] Status: {}", responseEntity.getStatusCode());
      return responseEntity.getBody();
    } catch (HttpClientErrorException e) {
      log.error("[Request] URI: {}, Method: {}, Headers: {}, Body: {}, Response: {}", uriString, context.getHttpMethod(), headers, context.getRequestBody(), e.getResponseBodyAsString());
    } catch (HttpServerErrorException e) {
      log.error("[Request] URI: {}, Method: {}, Headers: {}, Body: {}, Response: {}", uriString, context.getHttpMethod(), headers, context.getRequestBody(), e.getResponseBodyAsString());
    } catch (ResourceAccessException e) {
      log.error("[Request] URI: {}, Method: {}, Headers: {}, Body: {}, Response: {}", uriString, context.getHttpMethod(), headers, context.getRequestBody(), e.getMessage());
    } catch (Exception e) {
      log.error("[Request] URI: {}, Method: {}, Headers: {}, Body: {}, Response: {}", uriString, context.getHttpMethod(), headers, context.getRequestBody(), e.getMessage());
      throw e;
    }
    throw new RuntimeException("Failed to request");
  }

  @Override
  protected List<MediaType> getSupportedMediaTypes() {
    return List.of(MEDIA_TYPE);
  }
}
