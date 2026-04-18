# Skill: DevOps & CI/CD — Ondas Backend

> Kéo file này vào khi: cấu hình Docker, viết pipeline Jenkins, xử lý deploy, quản lý env.

---

## Tech Stack

| Công nghệ | Vai trò |
|---|---|
| **Docker** | Đóng gói app thành container image |
| **Docker Compose** | Orchestrate nhiều service (app + db + minio) |
| **Jenkins** | CI/CD server tự host trên VPS |
| **Docker Hub** | Container registry lưu image |

---

## Docker

### Build image

```bash
docker build -t dinhtrieuxtnd/ondas-be:latest .
```

### Các file Docker Compose

| File | Môi trường | Dùng khi |
|---|---|---|
| `docker-compose.dev.yml` | Development | Chạy local với hot-reload |
| `docker-compose.prod.yml` | Production | Deploy lên VPS |

---

## CI/CD Pipeline (Jenkins)

### Luồng pipeline

```
Checkout → Build JAR → Run Tests → Build Docker Image → Push Docker Hub → Deploy VPS
```

### Chi tiết từng stage

| Stage | Lệnh chính |
|---|---|
| Build | `mvn clean package -DskipTests -B` |
| Test | `mvn test -B` |
| Build Image | `docker build -t <image>:<tag> .` |
| Push Hub | `docker push <image>:<tag>` |
| Deploy | `docker compose ... up -d --force-recreate` |

### Credentials cần cấu hình trong Jenkins

| Credential ID | Loại | Nội dung |
|---|---|---|
| `dockerhub-credentials` | Username/Password | Docker Hub login |
| `prod-env-file` | Secret File | File `.env` production |

---

## Thông tin server

| Service | URL |
|---|---|
| App | `http://103.245.237.251:8080` |
| Jenkins | `http://103.245.237.251:9090` |

---

## Build & Run Commands

```bash
# Chạy development
./mvnw spring-boot:run

# Chạy với profile cụ thể
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Build JAR (bỏ qua test)
./mvnw clean package -DskipTests

# Chạy toàn bộ test
./mvnw test
```

---

## Environment Variables — Tổng hợp

Xem file `.env.example` để biết danh sách đầy đủ.

| Biến | Mô tả |
|---|---|
| `DB_URL` | JDBC URL PostgreSQL |
| `DB_USERNAME` / `DB_PASSWORD` | Thông tin xác thực DB |
| `JWT_SECRET` | Secret key ký JWT |
| `JWT_EXPIRATION_MS` | Thời gian sống của token (ms) |
| `MINIO_ENDPOINT` / `MINIO_ACCESS_KEY` / `MINIO_SECRET_KEY` | Kết nối MinIO |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | SMTP credentials |

> Không bao giờ commit file `.env` lên Git. Chỉ commit `.env.example` (không có giá trị thật).
