package org.example.destination.core.interceptor;


import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
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
      // 파일 다운로드 여부 판단
      boolean isFileDownload = isFileDownloadResponse(response);
      if (isFileDownload) {
        // 파일 다운로드 로깅
        logFileDownloadResponse(response);
      } else {
        // 일반 API 응답 로깅
        logApiResponse(response);
      }
      log.info("================================================== Response End ==================================================");
    }
  }

  // 파일 다운로드 여부 판단
  private boolean isFileDownloadResponse(ClientHttpResponse response) {
    HttpHeaders headers = response.getHeaders();
    String contentType = headers.getContentType() != null ? headers.getContentType().toString() : "";
    String contentDisposition = headers.getFirst("Content-Disposition");

    // 파일 다운로드 조건: Content-Type 또는 Content-Disposition에 따라 판단
    return contentType.contains("application/octet-stream") ||
      (contentDisposition != null && contentDisposition.toLowerCase().contains("attachment"));
  }

  // 파일 다운로드 로깅
  private void logFileDownloadResponse(ClientHttpResponse response) throws IOException {
    log.info("This response contains a file download.");
    InputStream inputStream = response.getBody();

    // 파일 크기 확인 (가능한 경우)
    int byteCount = 0;
    byte[] buffer = new byte[1024];
    while (inputStream.read(buffer) != -1) {
      byteCount += buffer.length;
    }

    log.info("Downloaded file size (approx): {} bytes", byteCount);

  }

  // 일반 API 응답 로깅
  private void logApiResponse(ClientHttpResponse response) throws IOException {
    String responseBody = StreamUtils.copyToString(response.getBody(), Charset.defaultCharset());
    log.info("Response body: {}", responseBody);
  }
}
