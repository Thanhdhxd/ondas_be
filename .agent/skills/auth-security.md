# Skill: Auth & Security

## Mô tả
Skill này hướng dẫn cách làm việc với hệ thống xác thực và phân quyền trong dự án Ondas Backend.
Hệ thống dùng **JWT stateless** (access token + refresh token), **BCrypt** để hash password, và **Role-based access control** với 3 role: `USER`, `CONTENT_MANAGER`, `ADMIN`.

---

## Kiến trúc hiện tại

```
domain/entity/
    User.java              — domain entity, có field role, active, bannedAt
    RefreshToken.java      — lưu token hash + expiresAt + revoked
    OtpCode.java           — OTP 6 chữ số cho forgot-password
    Role.java              — enum: USER | CONTENT_MANAGER | ADMIN

infrastructure/security/
    JwtUtil.java                   — sinh/validate JWT (access + refresh)
    JwtAuthenticationFilter.java   — OncePerRequestFilter, đọc Bearer token
    SecurityConfig.java            — Spring Security filterChain
    UserDetailsServiceImpl.java    — load user by email cho Spring Security

application/service/port/
    AuthServicePort.java           — contract của auth service
    ProfileServicePort.java        — contract của profile service

application/service/impl/
    AuthService.java               — implements AuthServicePort
    ProfileService.java            — implements ProfileServicePort
```

---

## Skill 1 — Thêm endpoint mới vào AuthController

### Khi nào dùng
Cần thêm 1 flow auth mới (ví dụ: verify email, resend OTP, Google OAuth...).

### Các bước

**Bước 1: Tạo Request DTO** trong `application/dto/request/`
```java
// application/dto/request/ResendOtpRequest.java
@Data
public class ResendOtpRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email is invalid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;
}
```

**Bước 2: Thêm method vào `AuthServicePort`**
```java
// application/service/port/AuthServicePort.java
void resendOtp(ResendOtpRequest request);
```

**Bước 3: Implement trong `AuthService`**
```java
// application/service/impl/AuthService.java
@Transactional
public void resendOtp(ResendOtpRequest request) {
    String email = normalizeEmail(request.getEmail());
    User user = userRepoPort.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
    // business logic...
    emailPort.sendOtpEmail(email, otp, toExpirationMinutes(passwordResetExpirationMs));
}
```

**Bước 4: Thêm endpoint vào `AuthController`**
```java
// presentation/controller/AuthController.java
@PostMapping("/resend-otp")
public ResponseEntity<ApiResponse<Void>> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
    authServicePort.resendOtp(request);
    return ResponseEntity.ok(ApiResponse.success(null));
}
```

**Lưu ý:**
- Endpoint `/api/auth/**` đã được `SecurityConfig` cấu hình `permitAll()` — không cần thêm gì.
- KHÔNG xử lý exception bằng `try-catch` trong Controller — để `GlobalExceptionHandler` xử lý.
- KHÔNG tiết lộ email có tồn tại hay không trong message lỗi.

---

## Skill 2 — Thêm phân quyền Role cho endpoint

### Khi nào dùng
Cần giới hạn 1 endpoint chỉ cho role cụ thể (ADMIN, CONTENT_MANAGER, hoặc USER).

### Cách dùng `@PreAuthorize`

```java
// Chỉ ADMIN
@PreAuthorize("hasRole('ADMIN')")
@DeleteMapping("/users/{id}")
public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) { ... }

// ADMIN hoặc CONTENT_MANAGER
@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
@PostMapping
public ResponseEntity<ApiResponse<SongResponse>> createSong(...) { ... }

// User đã đăng nhập (bất kỳ role)
// → KHÔNG cần annotation, SecurityConfig đã require authenticated cho anyRequest()
```

### Cấu hình SecurityConfig hiện tại

```java
// infrastructure/security/SecurityConfig.java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/**").permitAll()
    .requestMatchers("/api/admin/**").hasRole("ADMIN")
    .anyRequest().authenticated()
)
```

Nếu muốn thêm rule mới (ví dụ `/api/content/**` chỉ cho CONTENT_MANAGER), thêm **trước** `.anyRequest()`:
```java
.requestMatchers("/api/content/**").hasAnyRole("ADMIN", "CONTENT_MANAGER")
```

**Quan trọng:** `Role.getAuthority()` trả về `"ROLE_" + name()`. Spring Security's `hasRole("ADMIN")` tự động thêm prefix `ROLE_`, nên KHÔNG cần viết `hasRole("ROLE_ADMIN")`.

---

## Skill 3 — Lấy thông tin user đang đăng nhập trong Service

### Cách 1: Qua `@AuthenticationPrincipal` (trong Controller)

```java
@GetMapping
public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
        @AuthenticationPrincipal UserDetails userDetails) {
    return ResponseEntity.ok(ApiResponse.success(
            profileServicePort.getMyProfile(userDetails.getUsername()))); // username = email
}
```

### Cách 2: Qua `SecurityContextHolder` (trong Service nếu cần)

```java
String email = SecurityContextHolder.getContext()
        .getAuthentication()
        .getName(); // trả về email (username)
```

**Quy tắc:** Ưu tiên truyền `email` từ Controller vào Service thay vì gọi `SecurityContextHolder` trong Service.

---

## Skill 4 — Thêm custom exception liên quan đến auth

### Các exception auth hiện có

| Exception | HTTP Status | Khi nào throw |
|---|---|---|
| `InvalidCredentialsException` | 401 | Sai email/password, token không hợp lệ |
| `UserNotFoundException` | 404 | Không tìm thấy user |
| `EmailAlreadyExistsException` | 409 | Email đã tồn tại khi register |
| `InvalidCurrentPasswordException` | 400 | Sai mật khẩu hiện tại khi đổi password |
| `InvalidTokenException` | 401 | Refresh token không hợp lệ/hết hạn/bị revoke |

### Thêm exception mới

**Bước 1:** Tạo exception class
```java
// application/exception/ (hoặc domain/exception/)
public class AccountBannedException extends RuntimeException {
    public AccountBannedException(String message) {
        super(message);
    }
}
```

**Bước 2:** Đăng ký trong `GlobalExceptionHandler`
```java
// presentation/advice/GlobalExceptionHandler.java
@ExceptionHandler(AccountBannedException.class)
public ResponseEntity<ApiResponse<Void>> handleAccountBanned(AccountBannedException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(ex.getMessage()));
}
```

**Bước 3:** Throw trong Service khi cần
```java
if (!user.isActive() || user.isBanned()) {
    throw new AccountBannedException("Your account has been suspended");
}
```

---

## Skill 5 — Hiểu luồng JWT

### Access Token
- Thời hạn: cấu hình qua `jwt.expiration` (mặc định 1 ngày)
- Chứa: `sub` (email), `role`, `type: "access"`
- Được validate bởi `JwtAuthenticationFilter` ở mọi request

### Refresh Token
- Thời hạn: cấu hình qua `jwt.refresh-expiration` (mặc định 7 ngày)
- Raw token gửi cho client, **hash SHA-256** lưu vào DB (`refresh_tokens` table)
- Validate: check hash trong DB + `revoked = false` + `expiresAt > now`
- Bị revoke khi logout

### Cấu hình trong `application.yml`
```yaml
jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000         # 1 ngày (ms)
  refresh-expiration: 604800000 # 7 ngày (ms)
  password-reset-expiration: 60000 # 1 phút (ms) cho OTP
```

---

## Checklist khi làm việc với auth

- [ ] Endpoint auth mới → thêm vào `/api/auth/**` path (đã `permitAll`)
- [ ] Endpoint cần role cụ thể → dùng `@PreAuthorize` hoặc thêm rule vào `SecurityConfig`
- [ ] Mọi request DTO → có `@Valid` ở Controller
- [ ] Exception lỗi credentials → KHÔNG tiết lộ thông tin thừa (dùng message chung)
- [ ] KHÔNG log password, token raw, hay thông tin cá nhân
- [ ] Refresh token → lưu dạng hash, KHÔNG lưu raw
