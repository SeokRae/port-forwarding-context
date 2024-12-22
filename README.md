# Port Forwarding Service

## 개요

이 프로젝트는 Spring Boot 기반의 포트 포워딩 서비스입니다. RFC 표준을 준수하는 사용자 정의 HTTP 헤더를 통해 포트 포워딩 정보를 전달하고 관리하며, 멀티 홉(Multi-hop) 시나리오를
지원합니다.

## 프로젝트 구조

프로젝트는 다음과 같이 3개의 모듈로 구성되어 있습니다:

- `source-server`: 포트 포워딩을 처리하는 메인 서버 (기본 포트: 8080)
- `destination-a`: 포트 8081에서 실행되는 대상 서버 A
- `destination-b`: 포트 8082에서 실행되는 대상 서버 B

## RFC 표준 준수

본 서비스는 다음 RFC 표준들을 준수합니다:

1. RFC 6648: "Deprecating the 'X-' Prefix and Similar Constructs in Application Protocols"
    - 사용자 정의 헤더에서 "X-" 접두사 사용 제외
    - 표준화된 헤더 명명 규칙 적용

2. RFC 7230/7231: "HTTP/1.1"
    - HTTP 헤더 필드 문법 준수
    - 메시지 구문과 라우팅 규칙 준수

## 헤더 정의

다음과 같은 사용자 정의 헤더를 사용합니다:

- `Forwarded-Port`: 초기 요청의 포트 정보 (기본 헤더)
- `service-a-forwarded-port`: Destination A 서버로의 포워딩 시 사용될 포트
- `service-b-forwarded-port`: Destination B 서버로의 포워딩 시 사용될 포트

## 주요 기능

### 1. 포트 포워딩 체인

Source Server → Destination A → Destination B 간 포워딩 시:

1. 클라이언트가 Source Server로 초기 요청 시 포워딩 포트 정보 전달
2. Source Server가 Destination으로 요청 전달 시 해당 포트 정보 사용
3. 포트 정보가 전체 체인에 걸쳐 유지됨

### 2. 멀티 홉 시나리오 지원

- 단일 홉: Source → Destination A
- 다중 홉: Source → Destination A → Destination B
- 각 홉별 독립적인 포트 설정 가능

### 3. 컨텍스트 관리

- ThreadLocal 기반 포트 정보 관리
- 요청별 독립적인 컨텍스트 유지
- 요청 완료 시 자동 정리

## API 사용 예시

### 1. 단일 홉 포워딩

```bash
# Destination A로 포워딩
curl -H "service-a-forwarded-port: 8081" \
     http://localhost:8080/port/forward
```

### 2. 다중 홉 포워딩

```bash
# Destination A를 거쳐 B로 포워딩
curl -H "service-a-forwarded-port: 8081" \
     -H "service-b-forwarded-port: 8082" \
     http://localhost:8080/port/forward
```

## 기술 스택

- Java 21
- Spring Boot 3.2.3
- Gradle 8.11.1

### source-server

- Request

```bash
GET /port/forward HTTP/1.1
Host: example.com
service-a-forwarded-port: 8081
service-b-forwarded-port: 8082
```

- Response

```bash
HTTP/1.1 200 OK
Content-Type: text/plain;charset=UTF-8

"Destination B"
```

### destination-a

- Request

```bash
GET /target/path HTTP/1.1
Host: localhost:8081
service-b-forwarded-port: 8082
```

- Response

```bash
HTTP/1.1 200 OK
Content-Type: text/plain;charset=UTF-8

Destination B
```

### destination-b

- Request

```bash
GET /target/path HTTP/1.1
Host: localhost:8082
```

- Response

```bash
HTTP/1.1 200 OK
Content-Type: text/plain;charset=UTF-8

Destination B

```
`
## 실행 방법

### 1. 프로젝트 빌드

```bash
./gradlew clean build
```

### 2. 각 서버 실행

```bash
# Source Server 실행
./gradlew :source-server:bootRun

# Destination A 실행
./gradlew :destination-a:bootRun

# Destination B 실행
./gradlew :destination-b:bootRun
```

### 3. 실행 로그

- source-server

```text

2024-12-22T18:14:58.159+09:00  INFO 44482 --- [source-server] [    Test worker] o.e.s.c.f.ForwardedPortProcessingFilter  : Request Start - URI: /port/forward, Method: GET, Remote: 127.0.0.1, Headers: {service-a-forwarded-port=8081, service-b-forwarded-port=8082}
2024-12-22T18:14:58.159+09:00  INFO 44482 --- [source-server] [    Test worker] o.e.s.c.f.ForwardedPortProcessingFilter  : Port set - Header: service-a-forwarded-port, Port: 8081
2024-12-22T18:14:58.159+09:00  INFO 44482 --- [source-server] [    Test worker] o.e.s.c.f.ForwardedPortProcessingFilter  : Port set - Header: service-b-forwarded-port, Port: 8082
2024-12-22T18:14:58.171+09:00  INFO 44482 --- [source-server] [    Test worker] o.e.s.a.c.PortForwardController          : Forwarding request to service: service-a-forwarded-port, port: 8081
2024-12-22T18:14:58.171+09:00  INFO 44482 --- [source-server] [    Test worker] o.e.s.s.builder.UrlTemplateBuilder       : Building URI components for domain: localhost, path: /target/path, port: 8081
2024-12-22T18:14:58.171+09:00  INFO 44482 --- [source-server] [    Test worker] o.e.s.s.builder.UrlTemplateBuilder       : Using port: 8081
2024-12-22T18:14:58.171+09:00  INFO 44482 --- [source-server] [    Test worker] o.e.s.support.helper.RestTemplateHelper  : [Request] URI: http://localhost:8081/target/path, Method: GET, Headers: [service-b-forwarded-port:"8082"], Body: 
2024-12-22T18:14:58.179+09:00  INFO 44482 --- [source-server] [    Test worker] o.e.s.c.i.RestTemplateLoggingInterceptor : ================================================== Request Begin ==================================================
2024-12-22T18:14:58.179+09:00  INFO 44482 --- [source-server] [    Test worker] o.e.s.c.i.RestTemplateLoggingInterceptor : URI         : http://localhost:8081/target/path
2024-12-22T18:14:58.179+09:00  INFO 44482 --- [source-server] [    Test worker] o.e.s.c.i.RestTemplateLoggingInterceptor : Method      : GET
2024-12-22T18:14:58.179+09:00  INFO 44482 --- [source-server] [    Test worker] o.e.s.c.i.RestTemplateLoggingInterceptor : Headers     : [Accept:"text/plain, application/json, application/*+json, */*", service-b-forwarded-port:"8082", Content-Type:"text/plain;charset=UTF-8", Content-Length:"0"]
2024-12-22T18:14:58.179+09:00  INFO 44482 --- [source-server] [    Test worker] o.e.s.c.i.RestTemplateLoggingInterceptor : Request body: 
2024-12-22T18:14:58.179+09:00  INFO 44482 --- [source-server] [    Test worker] o.e.s.c.i.RestTemplateLoggingInterceptor : ================================================== Request End ==================================================
2024-12-22T18:14:58.304+09:00  INFO 44482 --- [source-server] [    Test worker] o.e.s.c.i.RestTemplateLoggingInterceptor : ================================================== Response Begin ==================================================
2024-12-22T18:14:58.305+09:00  INFO 44482 --- [source-server] [    Test worker] o.e.s.c.i.RestTemplateLoggingInterceptor : Status code  : 200 OK
2024-12-22T18:14:58.306+09:00  INFO 44482 --- [source-server] [    Test worker] o.e.s.c.i.RestTemplateLoggingInterceptor : Status text  : 
2024-12-22T18:14:58.306+09:00  INFO 44482 --- [source-server] [    Test worker] o.e.s.c.i.RestTemplateLoggingInterceptor : Headers      : [Date:"Sun, 22 Dec 2024 09:14:58 GMT", Keep-Alive:"timeout=60", Connection:"keep-alive, keep-alive", Content-Type:"text/plain;charset=UTF-8", Content-Length:"13"]
2024-12-22T18:14:58.307+09:00  INFO 44482 --- [source-server] [    Test worker] o.e.s.c.i.RestTemplateLoggingInterceptor : Response body: Destination B
2024-12-22T18:14:58.307+09:00  INFO 44482 --- [source-server] [    Test worker] o.e.s.c.i.RestTemplateLoggingInterceptor : ================================================== Response End ==================================================
2024-12-22T18:14:58.307+09:00  INFO 44482 --- [source-server] [    Test worker] o.e.s.c.i.RestTemplateLoggingInterceptor : Processing time: 128 ms
2024-12-22T18:14:58.309+09:00  INFO 44482 --- [source-server] [    Test worker] o.e.s.support.helper.RestTemplateHelper  : [Response] Status: 200 OK, Body: Destination B
2024-12-22T18:14:58.315+09:00  INFO 44482 --- [source-server] [    Test worker] o.e.s.c.f.ForwardedPortProcessingFilter  : Request End - Processing Time: 157 ms
```

- destination-a

```text
2024-12-22T18:14:58.221+09:00  INFO 44460 --- [service-a] [nio-8081-exec-1] o.e.d.core.filter.PortProcessingFilter   : ================================================== [Port PreHandle Start] ==================================================
2024-12-22T18:14:58.221+09:00  INFO 44460 --- [service-a] [nio-8081-exec-1] o.e.d.core.filter.PortProcessingFilter   : Request URI    : /target/path
2024-12-22T18:14:58.221+09:00  INFO 44460 --- [service-a] [nio-8081-exec-1] o.e.d.core.filter.PortProcessingFilter   : Request Method : GET
2024-12-22T18:14:58.221+09:00  INFO 44460 --- [service-a] [nio-8081-exec-1] o.e.d.core.filter.PortProcessingFilter   : Remote Address : 127.0.0.1
2024-12-22T18:14:58.222+09:00  INFO 44460 --- [service-a] [nio-8081-exec-1] o.e.d.core.filter.PortProcessingFilter   : Port configuration completed - Header: service-b-forwarded-port, Port: 8082
2024-12-22T18:14:58.222+09:00  INFO 44460 --- [service-a] [nio-8081-exec-1] o.e.d.core.filter.PortProcessingFilter   : ================================================== [Port PreHandle End] ==================================================
2024-12-22T18:14:58.232+09:00  INFO 44460 --- [service-a] [nio-8081-exec-1] o.e.d.a.DestinationAController           : Forwarding request to service: service-b-forwarded-port, port: 8082
2024-12-22T18:14:58.232+09:00  INFO 44460 --- [service-a] [nio-8081-exec-1] o.e.d.s.builder.UrlTemplateBuilder       : Building URI components for domain: localhost, path: /target/path, port: 8082
2024-12-22T18:14:58.234+09:00  INFO 44460 --- [service-a] [nio-8081-exec-1] o.e.d.s.builder.UrlTemplateBuilder       : Using port: 8082
2024-12-22T18:14:58.234+09:00  INFO 44460 --- [service-a] [nio-8081-exec-1] o.e.d.support.helper.RestTemplateHelper  : [Request] URI: http://localhost:8082/target/path, Method: GET, Headers: [], Body: 
2024-12-22T18:14:58.241+09:00  INFO 44460 --- [service-a] [nio-8081-exec-1] o.e.d.c.i.RestTemplateLoggingInterceptor : ================================================== Request Begin ==================================================
2024-12-22T18:14:58.241+09:00  INFO 44460 --- [service-a] [nio-8081-exec-1] o.e.d.c.i.RestTemplateLoggingInterceptor : URI         : http://localhost:8082/target/path
2024-12-22T18:14:58.241+09:00  INFO 44460 --- [service-a] [nio-8081-exec-1] o.e.d.c.i.RestTemplateLoggingInterceptor : Method      : GET
2024-12-22T18:14:58.241+09:00  INFO 44460 --- [service-a] [nio-8081-exec-1] o.e.d.c.i.RestTemplateLoggingInterceptor : Headers     : [Accept:"text/plain, application/json, application/*+json, */*", Content-Type:"text/plain;charset=UTF-8", Content-Length:"0"]
2024-12-22T18:14:58.241+09:00  INFO 44460 --- [service-a] [nio-8081-exec-1] o.e.d.c.i.RestTemplateLoggingInterceptor : Request body: 
2024-12-22T18:14:58.241+09:00  INFO 44460 --- [service-a] [nio-8081-exec-1] o.e.d.c.i.RestTemplateLoggingInterceptor : ================================================== Request End ==================================================
2024-12-22T18:14:58.296+09:00  INFO 44460 --- [service-a] [nio-8081-exec-1] o.e.d.c.i.RestTemplateLoggingInterceptor : ================================================== Response Begin ==================================================
2024-12-22T18:14:58.296+09:00  INFO 44460 --- [service-a] [nio-8081-exec-1] o.e.d.c.i.RestTemplateLoggingInterceptor : Status code  : 200 OK
2024-12-22T18:14:58.296+09:00  INFO 44460 --- [service-a] [nio-8081-exec-1] o.e.d.c.i.RestTemplateLoggingInterceptor : Status text  : 
2024-12-22T18:14:58.296+09:00  INFO 44460 --- [service-a] [nio-8081-exec-1] o.e.d.c.i.RestTemplateLoggingInterceptor : Headers      : [Content-Type:"text/plain;charset=UTF-8", Content-Length:"13", Date:"Sun, 22 Dec 2024 09:14:58 GMT", Keep-Alive:"timeout=60", Connection:"keep-alive"]
2024-12-22T18:14:58.297+09:00  INFO 44460 --- [service-a] [nio-8081-exec-1] o.e.d.c.i.RestTemplateLoggingInterceptor : Response body: Destination B
2024-12-22T18:14:58.297+09:00  INFO 44460 --- [service-a] [nio-8081-exec-1] o.e.d.c.i.RestTemplateLoggingInterceptor : ================================================== Response End ==================================================
2024-12-22T18:14:58.297+09:00  INFO 44460 --- [service-a] [nio-8081-exec-1] o.e.d.c.i.RestTemplateLoggingInterceptor : Processing time: 56 ms
2024-12-22T18:14:58.299+09:00  INFO 44460 --- [service-a] [nio-8081-exec-1] o.e.d.support.helper.RestTemplateHelper  : [Response] Status: 200 OK, Body: Destination B
2024-12-22T18:14:58.304+09:00  INFO 44460 --- [service-a] [nio-8081-exec-1] o.e.d.core.filter.PortProcessingFilter   : ================================================== [Port PostHandle End] ==================================================
2024-12-22T18:14:58.304+09:00  INFO 44460 --- [service-a] [nio-8081-exec-1] o.e.d.core.filter.PortProcessingFilter   : [Port Processing End] Processing Time: 83 ms
2024-12-22T18:14:58.304+09:00  INFO 44460 --- [service-a] [nio-8081-exec-1] o.e.d.core.filter.PortProcessingFilter   : ================================================== [Port PostHandle End] ==================================================
```

- destination-b

```text
2024-12-22T18:14:58.285+09:00  INFO 44471 --- [service-b] [nio-8082-exec-1] o.e.d.a.DestinationBController           : Destination B
```

## 설정

### source-server/application.yml

```yaml

service:
  a:
    name: service-a
    domain: localhost
    header:
      key: ${service.a.name}-forwarded-port
      ports: [ 8081, 8082 ]

```

### destination-a/application.yml

```yaml

service:
  b:
    name: service-b
    domain: localhost
    header:
      key: ${service.b.name}-forwarded-port
      ports: [ 8081, 8082 ]

```

## 에러 처리

- 잘못된 포트 번호 (400 Bad Request)
- 서버 연결 실패 (503 Service Unavailable)
- 포워딩 체인 실패 (500 Internal Server Error)

## 보안 고려사항

1. 포트 유효성 검증
    - 범위: 1-65535
    - 숫자 형식 검증
    - 시스템 예약 포트 제한

2. 요청 격리
    - ThreadLocal 기반 컨텍스트 관리
    - 요청간 데이터 독립성 보장

3. 헤더 보안
    - RFC 표준 준수 검증
    - 잘못된 헤더 값 필터링

## 모니터링

- 요청/응답 상세 로깅
- 포워딩 체인 추적
- 성능 메트릭 수집
