# 로컬 개발 환경 가이드

## 권장 방식: 하이브리드
- **인프라**(Postgres/Redis)는 docker로
- **Spring Boot**는 IntelliJ에서 직접 실행
- 핫 리로드 + 디버거 활용 가능

---

## 1. 사전 준비

### 필수 설치
- Docker Desktop
- IntelliJ IDEA
- IntelliJ **EnvFile 플러그인** (Settings → Plugins → "EnvFile" 검색 후 설치)

### Windows 사용자 주의
- 클론 후 `git config --global core.autocrlf input` 설정 권장
- 레포에 `.gitattributes` 있어서 자동으로 LF 강제되지만, 안전장치로 권장

---

## 2. 초기 셋업

```
# 1) .env 파일 생성
cp .env.example .env

# 2) .env 열어서 JWT_SECRET 수정 (반드시 32자 이상)

# 3) 인프라(postgres + redis) 백그라운드 기동
docker compose up -d

# 4) 헬스체크
docker compose ps   # postgres, redis 모두 healthy 확인
```

---

## 3. IntelliJ에서 backend 실행

1. **Run/Debug Configurations** → New → **Spring Boot**
2. Main class: `com.soongongjandi.SoongongjandiApplication` (또는 실제 메인 클래스)
3. **EnvFile 탭**:
    - Enable EnvFile
    - `+` 버튼 → 프로젝트 루트 `.env` 추가
4. Active profile: `local`
5. Run / Debug

### 검증
- 헬스체크: http://localhost:8080/actuator/health → `{"status":"UP"}`
- Swagger: http://localhost:8080/swagger-ui/index.html
- 로그에 ` 환경변수 검증 통과` 보이면 정상

---

## 4. DB 완전 초기화 (Flyway 꼬였을 때)

```bash
docker compose down -v          # ← -v 가 핵심: 볼륨까지 삭제
docker compose up -d postgres   # 새 DB 기동
# IntelliJ에서 backend 재실행 → Flyway가 처음부터 마이그레이션
```

---

## 자주 쓰는 명령어

| 목적 | 명령 |
|---|---|
| 인프라 시작 | `docker compose up -d` |
| 인프라 중지 | `docker compose down` |
| **인프라 + DB 데이터 삭제** | `docker compose down -v` |
| 로그 확인 | `docker compose logs -f postgres` |
| psql 접속 | `docker exec -it jandi-postgres psql -U soongongjandi -d soongongjandi` |
| redis-cli 접속 | `docker exec -it jandi-redis redis-cli` |

---

## 자주 발생하는 에러

### `Could not resolve placeholder 'DB_HOST'`
→ EnvFile 플러그인이 `.env`를 못 읽었거나, `.env` 파일 자체가 없음. 위 2번 단계 재확인.

### `JWT_SECRET은 최소 32자여야 합니다`
→ `.env`의 `JWT_SECRET` 값이 짧음. 32자 이상으로 변경.

### `./gradlew: /bin/sh^M: bad interpreter` (도커 빌드 시)
→ Windows에서 클론하면서 CRLF 변환됨. 다음 실행:
\`\`\`bash
git rm --cached -r .
git reset --hard
\`\`\`

### `port is already allocated` (5432 또는 6379)
→ 로컬에 다른 postgres/redis 떠있음. 끄거나 `docker-compose.yml`에서 포트 변경.