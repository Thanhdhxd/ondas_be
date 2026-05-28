## E2E (profile e2e)

> Chỉ khả dụng khi chạy với `SPRING_PROFILES_ACTIVE=e2e`.

### POST `/api/admin/e2e/reset`

Reset toàn bộ dữ liệu và seed lại dữ liệu mẫu phục vụ e2e.

**Auth:** ✅ `ADMIN`

**Response `200 OK`:**
```json
{ "success": true, "message": "OK", "data": null }
```

---

### POST `/api/admin/e2e/seed`

Seed dữ liệu nếu DB đang trống (không reset).

**Auth:** ✅ `ADMIN`

**Response `200 OK`:**
```json
{ "success": true, "message": "OK", "data": null }
```

**Tài khoản seed mặc định:**
- `admin@e2e.local` / `E2ePass123!`
- `user@e2e.local` / `E2ePass123!`

---

### GET `/api/e2e/storage/{bucket}/{objectName:.+}`

Tải object từ MinIO storage (dùng cho e2e test). Hỗ trợ Range request để stream audio/video.

**Auth:** Không yêu cầu (internal e2e)

**Path Params:**
| Param | Type | Mô tả |
|---|---|---|
| `bucket` | string | Tên bucket (vd: `songs`, `covers`, `avatars`) |
| `objectName` | string | Tên object (có thể chứa `/`) |

**Request Headers:**
| Header | Bắt buộc | Mô tả |
|---|---|---|
| `Range` | ❌ | Byte range, ví dụ: `bytes=0-65535` |

**Response `200 OK`:** Toàn bộ file (không có `Range` header).

**Response `206 Partial Content`:** Đoạn file theo Range request (có `Content-Range` header).

> **Lưu ý:** Chỉ khả dụng khi chạy với `SPRING_PROFILES_ACTIVE=e2e`.

---
