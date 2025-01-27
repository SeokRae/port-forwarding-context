package org.example.client.rest.infrastructures.factory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.client.rest.infrastructures.RequestStrategy;
import org.example.client.rest.infrastructures.context.RequestContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequestStrategyFactory {

  private final List<RequestStrategy> requestStrategies;

  public <R> RequestStrategy findStrategy(RequestContext<R> context) {
    for (RequestStrategy requestStrategy : requestStrategies) {
      if (requestStrategy.supports(context)) {
        log.info("[Strategy] [{}]", requestStrategy.getClass().getSimpleName());
        return requestStrategy;
      }
    }

    throw new IllegalArgumentException("Unsupported request strategy");
  }
}
