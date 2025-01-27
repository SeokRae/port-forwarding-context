package org.example.client.rest.infrastructures;


import org.example.client.rest.infrastructures.context.RequestContext;

public interface RequestStrategy {

  boolean supports(RequestContext<?> context);

  <R> R execute(RequestContext<R> context);
}
