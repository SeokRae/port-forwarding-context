package org.example.destination.application;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.example.destination.core.props.ServiceProperties;
import org.example.destination.core.props.UrisProperties;
import org.example.destination.support.context.ForwardedPortContext;
import org.example.destination.support.helper.RestTemplateHelper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DestinationAController {

  private final RestTemplateHelper restTemplateHelper;
  private final ServiceProperties serviceProperties;
  private final UrisProperties urisProperties;

  @GetMapping("/target/path/a")
  public void destinationA(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    ForwardedPortContext context = ForwardedPortContext.getContext();
    String headerKey = serviceProperties.getB().getHeader().getKey();
    Integer headerValue = context.getAttribute(headerKey, Integer.class).orElse(null);

    if (headerValue != null) {
      log.info("Header found: {} -> Forwarding to /internal/forward/b", headerKey);
      log.info("Initiating forward to service: {}, port: {}", headerKey, headerValue);

      request.getRequestDispatcher("/internal/forward/b").forward(request, response);

      log.info("Forwarding to /internal/forward/b completed successfully.");
    } else {
      log.info("Header not found: {} -> Responding with 'Destination A'", headerKey);
      response.getWriter().write("Destination A");
    }
  }


  @GetMapping("/internal/forward/b")
  public ResponseEntity<String> destinationB() {
    ForwardedPortContext context = ForwardedPortContext.getContext();
    String headerKey = serviceProperties.getB().getHeader().getKey();

    Integer port = context.getAttribute(headerKey, Integer.class).orElse(null);

    if (port == null) {
      log.error("Port information is missing in the context. Header Key: {}", headerKey);
      return ResponseEntity.badRequest().body("Invalid or missing port information.");
    }

    HttpHeaders headers = createHeaders(context, headerKey);
    log.info("Forwarding request to service: {}, port: {}", headerKey, port);

    return restTemplateHelper.postRequest(
      serviceProperties.getB().getDomain(),
      urisProperties.getDestination(),
      port,
      headers,
      HttpMethod.GET,
      new HashMap<>(),
      Strings.EMPTY,
      String.class
    );
  }

  private HttpHeaders createHeaders(ForwardedPortContext context, String excludeKey) {
    HttpHeaders headers = new HttpHeaders();
    context.getAttributes()
      .entrySet()
      .stream()
      .filter(entry -> !entry.getKey().equals(excludeKey))
      .forEach(entry -> headers.add(entry.getKey(), String.valueOf(entry.getValue())));
    return headers;
  }
}
