package org.example.source.core.interceptor;


import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Slf4j
public class RestTemplateLoggingInterceptor implements ClientHttpRequestInterceptor {

  @Override
  public ClientHttpResponse intercept(@NonNull HttpRequest request, @NonNull byte[] body, @NonNull ClientHttpRequestExecution execution) throws IOException {
    long startTime = System.currentTimeMillis(); // 요청 전송 전 시간 측정
    logRequest(request, body);
    ClientHttpResponse response = execution.execute(request, body);
    logResponse(response);

    long endTime = System.currentTimeMillis(); // 응답 수신 후 시간 측정
    long duration = endTime - startTime; // 처리 시간 계산

    if (log.isInfoEnabled()) {
      log.info("Processing time: {} ms", duration); // 처리 시간 로깅
    }

    return response;
  }

  private void logRequest(HttpRequest request, byte[] body) {
    if (log.isInfoEnabled()) {
      log.info("================================================== Request Begin ==================================================");
      log.info("URI         : {}", request.getURI());
      log.info("Method      : {}", request.getMethod());
      log.info("Headers     : {}", request.getHeaders());
      log.info("Request body: {}", new String(body, StandardCharsets.UTF_8));
      log.info("================================================== Request End ==================================================");
    }
  }

  private void logResponse(ClientHttpResponse response) throws IOException {
    if (log.isInfoEnabled()) {
      log.info("================================================== Response Begin ==================================================");
      log.info("Status code  : {}", response.getStatusCode());
      log.info("Status text  : {}", response.getStatusText());
      log.info("Headers      : {}", response.getHeaders());
      log.info("Response body: {}", StreamUtils.copyToString(response.getBody(), Charset.defaultCharset()));
      log.info("================================================== Response End ==================================================");
    }
  }
}
