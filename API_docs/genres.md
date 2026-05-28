## Genres

### POST `/api/genres`

Tạo thể loại mới.

**Auth:** ✅ `ADMIN` hoặc `CONTENT_MANAGER`

**Content-Type:** `multipart/form-data`

**Parts:**
| Part | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `data` | JSON (`CreateGenreRequest`) | ✅ | Thông tin thể loại |
| `cover` | File | ❌ | Ảnh bìa |

**`CreateGenreRequest`:**
| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `name` | string | ✅ | Tên thể loại |
| `slug` | string | ❌ | Slug URL |
| `description` | string | ❌ | Mô tả |
| `coverUrl` | string | ❌ | URL ảnh bìa (nếu không upload file) |

**Response `201 Created`:**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": 1,
    "name": "V-Pop",
    "slug": "v-pop",
    "description": "...",
    "coverUrl": "https://..."
  }
}
```

---

### PUT `/api/genres/{id}`

Cập nhật thể loại.

**Auth:** ✅ `ADMIN` hoặc `CONTENT_MANAGER`

**Content-Type:** `multipart/form-data`

**Parts:**
| Part | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `data` | JSON (`UpdateGenreRequest`) | ✅ | Partial update |
| `cover` | File | ❌ | Ảnh mới |

**`UpdateGenreRequest` fields:** `name`, `slug`, `description`, `coverUrl` — tất cả optional.

**Response `200 OK`:** Trả về `GenreResponse` đã cập nhật.

---

### GET `/api/genres/{id}`

Lấy chi tiết thể loại.

**Auth:** ✅ Yêu cầu (JWT)

**Response `200 OK`:** Trả về `GenreResponse`.

---

### GET `/api/genres`

Lấy tất cả thể loại.

**Auth:** ✅ Yêu cầu (JWT)

**Response `200 OK`:** Trả về `List<GenreResponse>`.

---

### GET `/api/genres/search`

Tìm kiếm thể loại theo tên.

**Auth:** ✅ Yêu cầu (JWT)

**Query Params:**
| Param | Type | Bắt buộc | Mặc định | Mô tả |
|---|---|---|---|---|
| `query` | string | ✅ | — | Từ khóa |
| `mode` | string | ❌ | `contains` | `contains`, `startsWith`, `exact` |
| `page` | integer | ❌ | `0` | |
| `size` | integer | ❌ | `20` | |

**Response `200 OK`:** Trả về `PageResultDto<GenreResponse>`.

---

### DELETE `/api/genres/{id}`

Xóa thể loại.

**Auth:** ✅ `ADMIN` hoặc `CONTENT_MANAGER`

**Response `200 OK`:**
```json
{ "success": true, "message": "OK", "data": null }
```

---
