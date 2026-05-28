## Artists

### POST `/api/artists`

Tạo nghệ sĩ mới.

**Auth:** ✅ `ADMIN` hoặc `CONTENT_MANAGER`

**Content-Type:** `multipart/form-data`

**Parts:**
| Part | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `data` | JSON (`CreateArtistRequest`) | ✅ | Thông tin nghệ sĩ |
| `avatar` | File | ❌ | Ảnh đại diện |

**`CreateArtistRequest`:**
| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `name` | string | ✅ | Tên nghệ sĩ |
| `slug` | string | ❌ | Slug URL |
| `bio` | string | ❌ | Tiểu sử |
| `country` | string | ❌ | Quốc gia |

**Response `201 Created`:**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": "uuid",
    "name": "Sơn Tùng M-TP",
    "slug": "son-tung-m-tp",
    "bio": "...",
    "avatarUrl": "https://...",
    "country": "Vietnam"
  }
}
```

---

### PUT `/api/artists/{id}`

Cập nhật nghệ sĩ.

**Auth:** ✅ `ADMIN` hoặc `CONTENT_MANAGER`

**Content-Type:** `multipart/form-data`

**Parts:**
| Part | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `data` | JSON (`UpdateArtistRequest`) | ✅ | Partial update |
| `avatar` | File | ❌ | Ảnh mới |

**`UpdateArtistRequest` fields:** `name`, `slug`, `bio`, `country` — tất cả optional.

**Response `200 OK`:** Trả về `ArtistResponse` đã cập nhật.

---

### GET `/api/artists/{id}`

Lấy chi tiết nghệ sĩ.

**Auth:** ✅ Yêu cầu (JWT)

**Response `200 OK`:** Trả về `ArtistResponse`.

---

### GET `/api/artists`

Lấy danh sách nghệ sĩ có phân trang, hỗ trợ tìm kiếm theo tên.

**Auth:** ✅ Yêu cầu (JWT)

**Query Params:**
| Param | Type | Bắt buộc | Mặc định | Mô tả |
|---|---|---|---|---|
| `query` | string | ❌ | — | Tìm kiếm theo tên nghệ sĩ |
| `mode` | string | ❌ | `contains` | Chế độ tìm kiếm: `contains`, `fulltext` |
| `page` | integer | ❌ | `0` | Trang (0-based) |
| `size` | integer | ❌ | `20` | Số phần tử mỗi trang |

**Ví dụ:**
```
GET /api/artists?page=0&size=20               → tất cả (paginated)
GET /api/artists?query=son+tung&page=0&size=20 → tìm theo tên
```

**Response `200 OK`:** Trả về `PageResultDto<ArtistResponse>`.

---

### DELETE `/api/artists/{id}`

Xóa nghệ sĩ.

**Auth:** ✅ `ADMIN` hoặc `CONTENT_MANAGER`

**Response `200 OK`:**
```json
{ "success": true, "message": "OK", "data": null }
```

---
