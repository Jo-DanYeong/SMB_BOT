# Discord 관리 봇 (SMB BOT)

JDA를 기반으로 한 Discord 서버 관리 봇입니다. 금지어 필터링, 경고 시스템, 자동 뮤트 기능 등 서버 관리를 위한 필수 기능을 제공합니다.
- JDA : https://github.com/discord-jda/JDA
## 🚀 주요 기능

### 💬 명령어 처리 시스템
- `>` 접두사를 사용한 간편한 명령어 시스템
- 관리자 권한 검증 기능
- 한글 및 영문 명령어 지원

### 🚫 금지어 필터링
- 실시간 메시지 필터링
- 특수문자 우회 방지 (정규화 기능)
- 서버별 독립적인 금지어 목록 관리
- MongoDB 기반 영구 저장

### ⚠️ 경고 및 자동 뮤트 시스템
- 사용자 경고 부여 및 누적 관리
- 5회 경고 시 자동 뮤트 (경고 1회당 2분)
- 뮤트 상태 데이터베이스 저장
- 관리자 권한이 있는 사용자만 경고 부여 가능

### 🧹 메시지 정리 기능
- 대량 메시지 삭제 (1-100개)
- 비동기 처리로 빠른 응답 속도
- 삭제 결과 임베드 메시지로 알림

## 📋 명령어 목록

| 명령어 | 설명 | 권한 |
|--------|------|------|
| `>help` 또는 `>도움말` | 도움말 메시지 표시 | 모든 사용자 |
| `>금지어 [추가/삭제/목록] [단어]` | 금지어 관리 | 관리자 |
| `>경고 [add/remove] @사용자` | 경고 부여/제거 | 관리자 |
| `>뮤트 [숫자]` | 메시지 대량 삭제 | 관리자 |

## 🛠️ 기술 스택

- **Java 17**
- **Spring Boot 3.2.0**
- **JDA (Java Discord API) 6.1.2**
- **MongoDB** (데이터 저장소)S
- **Gradle** (빌드 도구)
- **Lombok** (보일러플레이트 코드 감소)

## 📁 프로젝트 구조

```
src/main/java/
├── my/bot/                 # 메인 애플리케이션
│   ├── BotMain.java       # Spring Boot 진입점
│   ├── BotToken.java      # Discord 토큰 관리
│   └── GatwayIntents.java # Discord 게이트웨이 인텐트
├── response/              # 명령어 처리
│   ├── ListenCommend.java # 명령어 리스너
│   ├── EmbedUtil.java     # 임베드 메시지 유틸
│   └── Test.java          # 테스트 클래스
├── CurseWordDB/               # 금지어 필터링
│   ├── MessageFilter.java # 메시지 필터
│   └── database/          # 금지어 데이터베이스
│       ├── CurseWordDB.java
│       ├── CurseWordEntity.java
│       └── CurseWordRepo.java
└── WarnDB/                  # 경고 시스템
    ├── WarnCount.java     # 경고 카운트
    ├── WarnEntity.java    # 경고 엔티티
    └── WarnRepo.java      # 경고 저장소
```

## 🔄 시스템 흐름

### 1. 봇 시작 및 Discord 연결
1. Spring Boot 애플리케이션 시작
2. Discord 토큰 로드 (Token.properties)
3. JDA 빌더 설정 및 이벤트 리스너 등록
4. Discord 서버 연결

### 2. 명령어 처리
1. 메시지 수신 및 접두사 검증 (`>`)
2. 명령어 파싱 및 분기
3. 관리자 권한 검증
4. 적절한 핸들러 함수 호출

### 3. 금지어 필터링
1. 메시지 정규화 (특수문자 제거, 소문자 변환)
2. 서버별 금지어 목록 조회
3. 금지어 포함 여부 검사
4. 위반 시 메시지 삭제 및 알림

### 4. 경고 시스템
1. 경고 부여 명령어 처리
2. MongoDB에 경고 횟수 저장
3. 5회 누적 시 자동 뮤트 실행
4. 뮤트 상태 데이터베이스 저장

### 5. 메시지 정리
1. 삭제할 메시지 수 파싱
2. 수량 검증 (1-100)
3. 비동기 메시지 삭제
4. 결과 알림 전송

## 🚀 설치 및 실행

### 사전 요구사항
- Java 17 이상
- MongoDB 서버
- Discord 봇 토큰

### 설정
1. 리포지토리 클론
```bash
git clone [리포지토리 주소]
cd CMS_BOT
```

2. 토큰 설정
`src/main/resources/Token.properties` 파일 생성:
```properties
BotToken=YOUR_DISCORD_BOT_TOKEN
```

3. MongoDB 설정
`application.properties` 파일 생성:
```properties
spring.data.mongodb.uri=mongodb://localhost:27017/discord_bot
```

### 실행
```bash
# Gradle Wrapper 사용
./gradlew bootRun

# 또는 PowerShell 사용
./gradlew.bat bootRun
```

### 빌드
```bash
# 실행 가능 JAR 파일 생성
./gradlew build

# 빌드된 JAR 실행
java -jar build/libs/CMS_BOT-1.0-SNAPSHOT.jar
```

## 📊 데이터베이스 스키마

### CurseWordDB (금지어)
```java
- guildId: String (서버 ID)
- word: String (금지어)
- banned: Boolean (활성화 여부)
```

### WarnCount (경고 카운트)
```java
- guildId: String (서버 ID)
- userId: String (사용자 ID)
- warncnt: Integer (경고 횟수)
- mute: Boolean (뮤트 여부)
```

### 코드 스타일
- Lombok 사용으로 보일러플레이트 코드 최소화
- Spring Boot 의존성 주입 방식
- MongoDB Repository 패턴

### 테스트
```bash
./gradlew test
```

### 빌드 정리
```powershell
# Windows PowerShell 사용
./clean-build.ps1
```