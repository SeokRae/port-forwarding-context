# Port Forwarding Service

## 개요

이 프로젝트는 Spring Boot 기반의 포트 포워딩 서비스입니다. RFC 표준을 준수하는 사용자 정의 HTTP 헤더를 통해 포트 포워딩 정보를 전달하고 관리하며, 멀티 홉(Multi-hop) 시나리오를 지원합니다.

## 주요 기능

### 1. 포트 포워딩 체인
- Source Server → Destination A → Destination B로 이어지는 포워딩 체인 구성
- 각 서버 간 독립적인 포트 설정 지원
- ThreadLocal 기반의 컨텍스트를 통한 요청별 포트 정보 관리

### 2. 헤더 기반 포트 관리
- RFC 표준 준수 헤더 사용
  - `service-a-forwarded-port`: Destination A 서버용 포트
  - `service-b-forwarded-port`: Destination B 서버용 포트
- 정규식 기반 헤더 검증: `^[a-zA-Z0-9-]+-forwarded-port$`

### 3. 포트 검증
- 숫자 형식 검증
- 허용 범위 검증 (8081-8082)
- 시스템 예약 포트 제한
- 설정된 포트 목록 기반 유효성 검증

## 아키텍처

### 1. source-server (포트: 8080)
- 진입점 서버 역할
- 포트 포워딩 요청의 최초 검증
- 포워딩 체인 시작점
- ThreadLocal 기반 컨텍스트 관리

### 2. destination-a (포트: 8081)
- 중간 경유 서버 역할
- Destination B로의 포워딩 처리
- 독립적인 포트 정보 유효성 검증

### 3. destination-b (포트: 8082)
- 최종 목적지 서버
- 최종 응답 생성 및 반환

## 보안

### 1. 입력 검증
- 헤더 이름 검증
  - 정규식 패턴 적용
  - 특수문자 필터링
  - null/빈 값 체크
- 포트 번호 검증
  - 숫자 형식 검증
  - 범위 검증
  - 허용 포트 목록 검증

## 동시성 제어

### 1. 요청 격리
- ThreadLocal 기반 컨텍스트로 요청별 독립성 보장
- 요청 종료 시 자동 컨텍스트 정리
- 멀티스레드 환경에서의 데이터 격리

## 에러 처리

### 1. 입력값 검증
- 잘못된 포트 번호 (400 Bad Request)
  - 포트 정보 누락
  - 유효하지 않은 포트 값
- 잘못된 헤더 형식
  - 헤더 키 형식 불일치
  - 허용되지 않은 포트 값

### 2. HTTP 통신
- 클라이언트 에러 (4xx)
  - 상세 에러 로깅
- 서버 에러 (5xx)
  - 상세 에러 로깅
- 리소스 접근 실패
  - Connection Timeout (5초)
  - Read Timeout (10초)

### 3. 로깅
- 에러 발생 시 상세 정보 로깅
  - URI
  - HTTP 메서드
  - 요청 헤더
  - 요청 본문
  - 에러 메시지

## 모니터링

### 1. 요청 추적
- URI 정보
- HTTP 메서드
- 요청/응답 헤더
- 요청/응답 본문
- 처리 시간 측정

### 2. 성능 지표
- 요청별 처리 시간
- 포워딩 체인 성공/실패
- 타임아웃 발생

### 3. 로깅
- 요청/응답 전문 로깅
- 에러 상황 상세 로깅
- 처리 시간 로깅

## 설정

### application.yml
```yaml
service:
  forwarding:
    headers:
      patterns:
        suffix: "forwarded-port"
        pattern: "^[a-zA-Z0-9-]+-forwarded-port$"
      ports:
        pattern: "^[0-9]+$"
        range:
          min: 8081
          max: 8082
```

## API 사용 예시

### 1. 단일 홉 포워딩
```bash
curl -H "service-a-forwarded-port: 8081" \
     http://localhost:8080/port/forward
```

### 2. 멀티 홉 포워딩
```bash
curl -H "service-a-forwarded-port: 8081" \
     -H "service-b-forwarded-port: 8082" \
     http://localhost:8080/port/forward
```

## 시작하기

### 요구사항
- Java 17+
- Gradle 8.5+

### 프로젝트 실행 방법

#### 1. 프로젝트 클론
```bash
git clone https://github.com/your-username/port-forwarding-service.git
cd port-forwarding-service
```

#### 2. 프로젝트 빌드
```bash
./gradlew clean build
```

#### 3. 각 서버 실행
서버는 다음 순서로 실행합니다:

```bash
# Destination B 실행 (포트: 8082)
./gradlew :destination-b:bootRun

# Destination A 실행 (포트: 8081)
./gradlew :destination-a:bootRun

# Source Server 실행 (포트: 8080)
./gradlew :source-server:bootRun
```

### 테스트 방법

#### 1. 단일 홉 테스트
```bash
# Source Server → Destination A
curl -v -H "service-a-forwarded-port: 8081" \
     http://localhost:8080/port/forward
```

#### 2. 멀티 홉 테스트
```bash
# Source Server → Destination A → Destination B
curl -v -H "service-a-forwarded-port: 8081" \
     -H "service-b-forwarded-port: 8082" \
     http://localhost:8080/port/forward
```

### 단위 테스트 실행
```bash
# 전체 테스트 실행
./gradlew test

# 개별 모듈 테스트
./gradlew :source-server:test
./gradlew :destination-a:test
./gradlew :destination-b:test
```

### 주의사항
1. 서버는 반드시 Destination B → A → Source Server 순서로 실행해야 합니다.
2. 각 서버는 지정된 포트(8080, 8081, 8082)를 사용하므로, 해당 포트들이 사용 가능한지 확인해주세요.
3. 모든 서버가 정상적으로 실행된 후에 테스트를 진행해주세요.