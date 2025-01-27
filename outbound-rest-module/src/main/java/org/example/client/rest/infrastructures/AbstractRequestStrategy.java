package org.example.client.rest.infrastructures;

import lombok.extern.slf4j.Slf4j;
import org.example.client.rest.infrastructures.context.RequestContext;
import org.example.client.rest.infrastructures.support.UrlTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Slf4j
public abstract class AbstractRequestStrategy implements RequestStrategy {

  protected final RestTemplate restTemplate;
  protected final UrlTemplateBuilder urlTemplateBuilder;

  protected AbstractRequestStrategy(RestTemplate restTemplate, UrlTemplateBuilder urlTemplateBuilder) {
    Optional.ofNullable(restTemplate)
      .orElseThrow(() -> new IllegalArgumentException("RestTemplate must not be null"));
    Optional.ofNullable(urlTemplateBuilder)
      .orElseThrow(() -> new IllegalArgumentException("UrlTemplateBuilder must not be null"));
    this.restTemplate = restTemplate;
    this.urlTemplateBuilder = urlTemplateBuilder;
  }

  @Override
  public boolean supports(RequestContext<?> context) {
    MediaType mediaType = context.getMediaType();
    return mediaType != null && getSupportedMediaTypes().contains(mediaType);
  }

  @Override
  public <R> R execute(RequestContext<R> context) {
    log.info("Executing strategy: {}", this.getClass().getSimpleName());
    preProcess(context);
    R result = doExecute(context);
    postProcess(result);
    return result;
  }

  protected abstract List<MediaType> getSupportedMediaTypes();

  protected abstract <R> R doExecute(RequestContext<R> context);

  // 선택적으로 오버라이드할 수 있는 후처리/전처리 메서드
  protected <R> void preProcess(RequestContext<R> context) {
    log.info("Preprocessing request: {} {}", context.getDomain(), context.getPath());
  }

  protected <R> void postProcess(R result) {
    log.info("Postprocessing result: {}", result);
  }
}
