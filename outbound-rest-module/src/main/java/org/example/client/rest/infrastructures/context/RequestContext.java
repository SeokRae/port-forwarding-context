package org.example.client.rest.infrastructures.context;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.Optional;

@Getter
@ToString
public class RequestContext<R> {
  private final MediaType mediaType;
  private final String domain;
  private final String path;
  private final Integer port;
  private final HttpHeaders httpHeaders;
  private final HttpMethod httpMethod;
  private final Map<String, Object> pathVariables;
  private final Object requestBody;
  private final Class<R> responseType;

  @Builder
  public RequestContext(MediaType mediaType, String domain, String path, Integer port, HttpHeaders httpHeaders, HttpMethod httpMethod, Map<String, Object> pathVariables, Object requestBody, Class<R> responseType) {
    Optional.ofNullable(mediaType)
      .orElseThrow(() -> new IllegalArgumentException("mediaType must not be null"));
    Optional.ofNullable(domain)
      .orElseThrow(() -> new IllegalArgumentException("domain must not be null"));
    Optional.ofNullable(path)
      .orElseThrow(() -> new IllegalArgumentException("path must not be null"));
    Optional.ofNullable(httpMethod)
      .orElseThrow(() -> new IllegalArgumentException("httpMethod must not be null"));
    Optional.ofNullable(responseType)
      .orElseThrow(() -> new IllegalArgumentException("responseType must not be null"));
    this.mediaType = mediaType;
    this.domain = domain;
    this.path = path;
    this.port = port;
    this.httpHeaders = httpHeaders;
    this.httpMethod = httpMethod;
    this.pathVariables = pathVariables;
    this.requestBody = requestBody;
    this.responseType = responseType;
  }
}
