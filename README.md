# soongong-jandi (순공잔디)

집중/공부 추적 애플리케이션입니다. 사용자의 집중 세션을 기록하고 일별 요약 및 마크다운 커밋을 생성합니다.

## 톱레벨 디렉토리 구조
| 디렉토리 | 역할 |
| :--- | :--- |
| `backend/` | Spring Boot 기반 API 서버 |
| `web/` | Next.js 프론트엔드 (예정) |
| `notify-server/` | Node.js 기반 알림 서버 (예정) |
| `infra/` | CI/CD 및 인프라 설정 (Jenkins, Nginx) |
| `docs/` | API 명세서 및 프로젝트 컨벤션 |
| `scripts/` | 운영 및 유틸리티 스크립트 |

## 🛠 기술 스택 (Tech Stack)

### 💻 Development
| 분류 | 기술 스택 |
| :--- | :--- |
| **Frontend** | **Next.js**, React |
| **Backend** | **Spring Boot 3.x**, Java 21, JPA |
| **Database** | **MySQL**, Redis |

### 🚀 Infrastructure & DevOps
| 분류 | 기술 스택 | 상세 역할 |
| :--- | :--- | :--- |
| **CI/CD** | **GitHub Actions** | 파이프라인 자동화 |
| **Container** | **Docker**, Docker Compose | 환경 표준화 및 관리 |
| **Web Server** | **Nginx** | 무중단 배포 (Blue-Green), 리버스 프록시 |
| **Cloud** | **AWS EC2**, **AWS S3** | 컴퓨팅 서버 및 백업 저장소 |
| **Security** | **Let's Encrypt** | SSL/HTTPS 보안 적용 |
| **Monitoring** | **Prometheus**, **Grafana** | 리소스 시각화 및 로그 관리 |
| **Notification** | **Mattermost** | 실시간 배포/장애 알림 |