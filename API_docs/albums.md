## Albums

### POST `/api/albums`

Tạo album mới.

**Auth:** ✅ `ADMIN` hoặc `CONTENT_MANAGER`

**Content-Type:** `multipart/form-data`

**Parts:**
| Part | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `data` | JSON (`CreateAlbumRequest`) | ✅ | Metadata album |
| `cover` | File | ❌ | Ảnh bìa album |

**`CreateAlbumRequest`:**
| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `title` | string | ✅ | Tên album |
| `slug` | string | ❌ | Slug URL |
| `releaseDate` | date (`yyyy-MM-dd`) | ❌ | Ngày phát hành |
| `albumType` | string | ❌ | Loại album (vd: `ALBUM`, `EP`, `SINGLE`) |
| `description` | string | ❌ | Mô tả album |
| `artistIds` | UUID[] | ✅ | Danh sách ID nghệ sĩ |

**Response `201 Created`:**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": "uuid",
    "title": "Tâm 9",
    "slug": "tam-9",
    "coverUrl": "https://...",
    "releaseDate": "2020-01-01",
    "albumType": "ALBUM",
    "description": "...",
    "totalTracks": 0,
    "artistIds": ["uuid"],
    "tracklist": []
  }
}
```

---

### PUT `/api/albums/{id}`

Cập nhật album.

**Auth:** ✅ `ADMIN` hoặc `CONTENT_MANAGER`

**Content-Type:** `multipart/form-data`

**Parts:**
| Part | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `data` | JSON (`UpdateAlbumRequest`) | ✅ | Partial update |
| `cover` | File | ❌ | Ảnh bìa mới |

**`UpdateAlbumRequest` fields:** `title`, `slug`, `releaseDate`, `albumType`, `description`, `artistIds` — tất cả đều optional.

**Response `200 OK`:** Trả về `AlbumResponse` đã cập nhật.

---

### GET `/api/albums/{id}`

Lấy chi tiết album.

**Auth:** ✅ Yêu cầu (JWT)

**Response `200 OK`:** Trả về `AlbumResponse` (bao gồm `tracklist`).

---

### GET `/api/albums`

Lấy danh sách album có phân trang, hỗ trợ tìm kiếm theo tên.

**Auth:** ✅ Yêu cầu (JWT)

**Query Params:**
| Param | Type | Bắt buộc | Mặc định | Mô tả |
|---|---|---|---|---|
| `query` | string | ❌ | — | Tìm kiếm theo tên album |
| `mode` | string | ❌ | `contains` | Chế độ tìm kiếm: `contains`, `fulltext` |
| `page` | integer | ❌ | `0` | Trang (0-based) |
| `size` | integer | ❌ | `20` | Số phần tử mỗi trang |

**Ví dụ:**
```
GET /api/albums?page=0&size=20              → tất cả (paginated)
GET /api/albums?query=tam+9&page=0&size=20  → tìm theo tên
```

**Response `200 OK`:** Trả về `PageResultDto<AlbumResponse>`.

---

### DELETE `/api/albums/{id}`

Xóa album.

**Auth:** ✅ `ADMIN` hoặc `CONTENT_MANAGER`

**Response `200 OK`:**
```json
{ "success": true, "message": "OK", "data": null }
```

---
