## Profile

### GET `/api/profile`

Lấy thông tin profile của user đang đăng nhập.

**Auth:** ✅ Yêu cầu (JWT)

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": "uuid",
    "email": "user@example.com",
    "displayName": "Nguyen Van A",
    "avatarUrl": "https://...",
    "role": "USER",
    "lastLoginAt": "2026-04-21T10:00:00",
    "createdAt": "2026-01-01T00:00:00"
  }
}
```

---

### PUT `/api/profile`

Cập nhật thông tin profile.

**Auth:** ✅ Yêu cầu (JWT)

**Request Body:**
| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `displayName` | string | ✅ | 1–50 ký tự |

```json
{
  "displayName": "Nguyen Van B"
}
```

**Response `200 OK`:** Trả về `UserProfileResponse` đã cập nhật.

---

### PATCH `/api/profile/avatar`

Cập nhật ảnh đại diện.

**Auth:** ✅ Yêu cầu (JWT)

**Content-Type:** `multipart/form-data`

**Parts:**
| Part | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `avatar` | File | ✅ | File ảnh đại diện mới |

**Response `200 OK`:** Trả về `UserProfileResponse` đã cập nhật.

---

### PUT `/api/profile/password`

Đổi mật khẩu.

**Auth:** ✅ Yêu cầu (JWT)

**Request Body:**
| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `currentPassword` | string | ✅ | Mật khẩu hiện tại |
| `newPassword` | string | ✅ | Mật khẩu mới, tối thiểu 8 ký tự |

```json
{
  "currentPassword": "oldpassword",
  "newPassword": "newpassword123"
}
```

**Response `200 OK`:**
```json
{ "success": true, "message": "OK", "data": null }
```

---
