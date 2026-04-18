# Skill: Security — Ondas Backend

> Kéo file này vào khi: viết JWT filter, cấu hình Spring Security, phân quyền endpoint, xử lý auth.

---

## Tech Stack

| Thư viện | Phiên bản | Vai trò |
|---|---|---|
| `spring-boot-starter-security` | (Spring Boot managed) | Filter chain, phân quyền |
| `jjwt-api` | 0.12.6 | JWT API |
| `jjwt-impl` | 0.12.6 | JWT implementation (runtime) |
| `jjwt-jackson` | 0.12.6 | JWT JSON serialization (runtime) |

---

## Authentication — JWT Stateless

- Token đính kèm trong header: `Authorization: Bearer <token>`
- Server **không lưu session** — hoàn toàn stateless.
- Mỗi request đều phải qua filter xác thực JWT trước khi vào Controller.

---

## Authorization — Role-based (RBAC)

> 3 role được định nghĩa từ tuần 1 (xem `project_plan_9weeks.md` — Tuần 1, task 3).

| Role | Quyền |
|---|---|
| `USER` | Nghe nhạc, quản lý playlist cá nhân, yêu thích bài hát |
| `CONTENT_MANAGER` | CRUD bài hát / nghệ sĩ / album / thể loại, quản lý lyrics, tag/mood, cấu hình trang chủ app |
| `ADMIN` | Tất cả quyền CONTENT_MANAGER + quản lý user, xem thống kê, activity log, phân quyền |

### URL Prefix Convention

| Prefix | Dành cho | Ai gọi |
|---|---|---|
| `/api/auth/**` | Đăng ký, đăng nhập, refresh token | Public |
| `/api/**` | Tính năng Mobile App | `USER` (và tất cả role) |
| `/admin/**` | Tính năng Admin Web App | `CONTENT_MANAGER` + `ADMIN` |

> `/admin/**` có nghĩa là *"endpoint của admin web"*, không phải *"chỉ dành cho role ADMIN"*. `@PreAuthorize` sẽ phân biệt chi tiết bên trong.

### SecurityConfig — Skeleton

```java
http
    .authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/auth/**").permitAll()
        .requestMatchers("/admin/**").authenticated()   // CM + Admin, @PreAuthorize lo phần còn lại
        .anyRequest().authenticated()
    )
```

### Phân quyền chi tiết — `@PreAuthorize`

Dùng annotation trực tiếp trên method để kiểm soát trong `/admin/**`:

```java
// Cần đăng nhập (bất kỳ role — dùng cho /api/**)
@PreAuthorize("isAuthenticated()")

// Content Manager hoặc Admin (CRUD nội dung, lyrics, tag...)
@PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")

// Admin only (quản lý user, activity log, phân quyền)
@PreAuthorize("hasRole('ADMIN')")
```

---

## Quy tắc bảo mật bắt buộc

- **KHÔNG** log password, JWT secret, hay thông tin cá nhân.
- Message lỗi login dùng `"Invalid credentials"` — không phân biệt sai email hay sai password. **Ngoại lệ:** flow quên mật khẩu được phép trả `"Email not found"` để UX rõ ràng.
- JWT secret phải lấy từ environment variable, **không hardcode** trong code hay `application.yml`.
- Endpoint lỗi auth trả về HTTP `401 Unauthorized`, không expose stack trace hay chi tiết nội bộ.

---

## Vị trí đặt code Security

```
infrastructure/security/
├── JwtFilter.java              # OncePerRequestFilter — validate token
├── JwtService.java             # Generate / validate / extract claims
├── SecurityConfig.java         # SecurityFilterChain bean
└── UserDetailsServiceImpl.java # Load user từ DB cho Spring Security
```

---

## Environment Variables — Security

| Biến | Mô tả |
|---|---|
| `JWT_SECRET` | Secret key ký JWT (Base64 encoded, ≥ 256 bit) |
| `JWT_EXPIRATION` | Thời gian sống của access token (ms) — vd: `86400000` (24h) |
