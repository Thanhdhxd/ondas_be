## Admin — Quản lý User

> Tất cả endpoint trong section này yêu cầu role `ADMIN`.

### GET `/api/admin/users`

Lấy danh sách user có phân trang, hỗ trợ lọc theo từ khóa, role và trạng thái.

**Auth:** ✅ `ADMIN`

**Query Params:**
| Param | Type | Bắt buộc | Mặc định | Mô tả |
|---|---|---|---|---|
| `keyword` | string | ❌ | — | Tìm kiếm theo email hoặc displayName |
| `role` | string | ❌ | — | Lọc theo role: `USER`, `CONTENT_MANAGER`, `ADMIN` |
| `active` | boolean | ❌ | — | Lọc theo trạng thái tài khoản |
| `page` | integer | ❌ | `0` | Trang (0-based) |
| `size` | integer | ❌ | `20` | Số phần tử mỗi trang |

**Ví dụ:**
```
GET /api/admin/users?keyword=nguyen&role=USER&active=true&page=0&size=20
```

**Response `200 OK`:** Trả về `PageResultDto<AdminUserResponse>`.

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "items": [
      {
        "id": "uuid",
        "email": "user@example.com",
        "displayName": "Nguyen Van A",
        "avatarUrl": "https://...",
        "role": "USER",
        "active": true,
        "banReason": null,
        "bannedAt": null,
        "lastLoginAt": "2026-04-27T10:00:00",
        "createdAt": "2026-01-01T00:00:00",
        "updatedAt": "2026-04-27T10:00:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

---

### GET `/api/admin/users/{id}`

Xem chi tiết một user.

**Auth:** ✅ `ADMIN`

**Path Params:**
| Param | Type | Mô tả |
|---|---|---|
| `id` | UUID | ID user |

**Response `200 OK`:** Trả về `AdminUserResponse`.

---

### PATCH `/api/admin/users/{id}/ban`

Ban một user.

**Auth:** ✅ `ADMIN`

**Path Params:**
| Param | Type | Mô tả |
|---|---|---|
| `id` | UUID | ID user cần ban |

**Request Body:**
| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `banReason` | string | ✅ | Lý do ban |

```json
{
  "banReason": "Vi phạm điều khoản sử dụng"
}
```

**Response `200 OK`:** Trả về `AdminUserResponse` đã cập nhật (với `active: false`, `banReason` và `bannedAt`).

---

### PATCH `/api/admin/users/{id}/unban`

Unban một user.

**Auth:** ✅ `ADMIN`

**Path Params:**
| Param | Type | Mô tả |
|---|---|---|
| `id` | UUID | ID user cần unban |

**Response `200 OK`:** Trả về `AdminUserResponse` đã cập nhật (với `active: true`, `banReason: null`, `bannedAt: null`).

---
