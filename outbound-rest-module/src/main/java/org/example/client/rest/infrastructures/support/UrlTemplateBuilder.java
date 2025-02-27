package org.example.client.rest.infrastructures.support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UrlTemplateBuilder {

  public UriComponents buildUriComponents(String domain, String path, Integer port, Map<String, Object> pathVariables) {
    log.info("Building URI components for domain: {}, path: {}, port: {}", domain, path, port);
    UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
      .scheme("http")
      .host(domain)
      .path(path);

    Optional.ofNullable(port)
      .ifPresent(uriBuilder::port);

    return pathVariables != null && !pathVariables.isEmpty()
      ? uriBuilder.buildAndExpand(pathVariables)
      : uriBuilder.build();
  }
}