# SAIT API

SAIT 서비스의 백엔드 API 서버입니다.

## Tech Stack

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL
- Redis
- JWT
- Maven

## Authentication

- Local Login
- Kakao Login
- Apple Login
- Email Verification

## Environment Variables

애플리케이션 실행을 위해 다음 환경변수가 필요합니다.

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `JWT_SECRET`
- `APPLE_CLIENT_ID`
- `KAKAO_CLIENT_ID`
- `KAKAO_CLIENT_SECRET`

환경변수의 실제 값은 Git 저장소에 포함하지 않습니다.

## Run

Windows:

```bash
mvnw.cmd spring-boot:run