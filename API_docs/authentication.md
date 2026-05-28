## Authentication

### POST `/api/auth/register`

Đăng ký tài khoản mới.

**Auth:** Không yêu cầu

**Request Body:**
| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `email` | string | ✅ | Email hợp lệ, tối đa 255 ký tự |
| `password` | string | ✅ | Tối thiểu 6 ký tự |
| `displayName` | string | ✅ | 2–100 ký tự |

```json
{
  "email": "user@example.com",
  "password": "password123",
  "displayName": "Nguyen Van A"
}
```

**Response `201 Created`:**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "accessToken": "eyJhbGciOi...",
    "refreshToken": "dGhpcyBpcyBh...",
    "user": {
      "id": "uuid",
      "email": "user@example.com",
      "displayName": "Nguyen Van A",
      "role": "USER"
    }
  }
}
```

---

### POST `/api/auth/login`

Đăng nhập.

**Auth:** Không yêu cầu

**Request Body:**
| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `email` | string | ✅ | Email hợp lệ |
| `password` | string | ✅ | |

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "accessToken": "eyJhbGciOi...",
    "refreshToken": "dGhpcyBpcyBh...",
    "user": {
      "id": "uuid",
      "email": "user@example.com",
      "displayName": "Nguyen Van A",
      "role": "USER"
    }
  }
}
```

---

### POST `/api/auth/refresh`

Lấy access token mới bằng refresh token.

**Auth:** Không yêu cầu

**Request Body:**
| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `refreshToken` | string | ✅ | Refresh token hiện tại |

```json
{
  "refreshToken": "dGhpcyBpcyBh..."
}
```

**Response `200 OK`:** Tương tự login.

---

### DELETE `/api/auth/logout`

Đăng xuất, thu hồi refresh token.

**Auth:** Không yêu cầu (gửi kèm refreshToken trong body)

**Request Body:**
| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `refreshToken` | string | ✅ | |

```json
{
  "refreshToken": "dGhpcyBpcyBh..."
}
```

**Response `200 OK`:**
```json
{ "success": true, "message": "OK", "data": null }
```

---

### POST `/api/auth/forgot-password`

Gửi OTP về email để đặt lại mật khẩu.

**Auth:** Không yêu cầu

**Request Body:**
| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `email` | string | ✅ | Email đã đăng ký |

```json
{
  "email": "user@example.com"
}
```

**Response `200 OK`:**
```json
{ "success": true, "message": "OK", "data": null }
```

> **Lưu ý:** Luôn trả về thành công để tránh tiết lộ email tồn tại trong hệ thống.

---

### POST `/api/auth/reset-password`

Đặt lại mật khẩu bằng OTP.

**Auth:** Không yêu cầu

**Request Body:**
| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `email` | string | ✅ | Email đăng ký |
| `otp` | string | ✅ | 6 chữ số |
| `newPassword` | string | ✅ | Tối thiểu 8 ký tự |

```json
{
  "email": "user@example.com",
  "otp": "123456",
  "newPassword": "newpassword123"
}
```

**Response `200 OK`:**
```json
{ "success": true, "message": "OK", "data": null }
```

---
