## Songs

### POST `/api/songs`

Tạo bài hát mới (kèm upload file).

**Auth:** ✅ `ADMIN` hoặc `CONTENT_MANAGER`

**Content-Type:** `multipart/form-data`

**Parts:**
| Part | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `data` | JSON (`CreateSongRequest`) | ✅ | Metadata bài hát |
| `audio` | File | ✅ | File audio |
| `cover` | File | ❌ | Ảnh bìa |

**`CreateSongRequest` (JSON part `data`):**
| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `title` | string | ✅ | Tên bài hát |
| `albumId` | UUID | ❌ | ID album (nếu là single thì bỏ qua) |
| `trackNumber` | integer | ❌ | Số thứ tự trong album |
| `releaseDate` | date (`yyyy-MM-dd`) | ❌ | Ngày phát hành |
| `artistIds` | UUID[] | ✅ | Danh sách ID nghệ sĩ |
| `genreIds` | Long[] | ✅ | Danh sách ID thể loại |

**Response `201 Created`:**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": "uuid",
    "title": "Nơi Này Có Anh",
    "slug": "noi-nay-co-anh",
    "durationSeconds": 210,
    "audioUrl": "https://...",
    "audioFormat": "mp3",
    "audioSizeBytes": 5242880,
    "coverUrl": "https://...",
    "albumId": "uuid",
    "trackNumber": 1,
    "releaseDate": "2026-01-01",
    "playCount": 0,
    "active": true,
    "artists": [
      { "id": "uuid", "name": "Sơn Tùng M-TP", "avatarUrl": "https://..." }
    ],
    "genres": [
      { "id": 1, "name": "V-Pop" }
    ]
  }
}
```

---

### PUT `/api/songs/{id}`

Cập nhật bài hát.

**Auth:** ✅ `ADMIN` hoặc `CONTENT_MANAGER`

**Content-Type:** `multipart/form-data`

**Path Params:**
| Param | Type | Mô tả |
|---|---|---|
| `id` | UUID | ID bài hát |

**Parts:**
| Part | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `data` | JSON (`UpdateSongRequest`) | ✅ | Các field muốn cập nhật (partial) |
| `audio` | File | ❌ | File audio mới |
| `cover` | File | ❌ | Ảnh bìa mới |

**`UpdateSongRequest` fields:** `title`, `albumId`, `trackNumber`, `releaseDate`, `artistIds`, `genreIds`, `active` — tất cả đều optional.

**Response `200 OK`:** Trả về `SongResponse` đã cập nhật.

---

### GET `/api/songs/{id}`

Lấy chi tiết bài hát theo ID.

**Auth:** ✅ Yêu cầu (JWT)

**Response `200 OK`:** Trả về `SongResponse`.

---

### GET `/api/songs`

Lấy danh sách bài hát có phân trang, hỗ trợ lọc theo nhiều tiêu chí. Các filter loại trừ nhau theo thứ tự ưu tiên: `query` → `artistId` → `albumId` → `genreId` → tất cả.

**Auth:** ✅ Yêu cầu (JWT)

**Query Params:**
| Param | Type | Bắt buộc | Mặc định | Mô tả |
|---|---|---|---|---|
| `query` | string | ❌ | — | Tìm kiếm theo tên bài hát |
| `mode` | string | ❌ | `contains` | Chế độ tìm kiếm: `contains`, `fulltext` |
| `artistId` | UUID | ❌ | — | Lọc theo nghệ sĩ |
| `albumId` | UUID | ❌ | — | Lọc theo album |
| `genreId` | Long | ❌ | — | Lọc theo thể loại |
| `page` | integer | ❌ | `0` | Trang (0-based) |
| `size` | integer | ❌ | `20` | Số phần tử mỗi trang |

**Ví dụ:**
```
GET /api/songs?page=0&size=20                          → tất cả (paginated)
GET /api/songs?query=noi+nay&mode=contains&page=0&size=20 → tìm theo tên
GET /api/songs?artistId=<uuid>&page=0&size=20          → theo nghệ sĩ
GET /api/songs?albumId=<uuid>&page=0&size=20           → theo album
GET /api/songs?genreId=1&page=0&size=20                → theo thể loại
```

**Response `200 OK`:** Trả về `PageResultDto<SongResponse>`.

---

### DELETE `/api/songs/{id}`

Xóa bài hát.

**Auth:** ✅ `ADMIN` hoặc `CONTENT_MANAGER`

**Response `200 OK`:**
```json
{ "success": true, "message": "OK", "data": null }
```

---

### GET `/api/songs/{id}/stream`

Stream audio bài hát. Chỉ trả về dữ liệu audio, **không** tự động ghi lịch sử hay tăng play count. Client cần gọi riêng `POST /api/play-history` khi bắt đầu phát.

**Auth:** ✅ Yêu cầu (JWT)

**Path Params:**
| Param | Type | Mô tả |
|---|---|---|
| `id` | UUID | ID bài hát |

**Request Headers:**
| Header | Bắt buộc | Mô tả |
|---|---|---|
| `Range` | ❌ | Byte range, ví dụ: `bytes=0-65535` |

**Response `200 OK`:** Toàn bộ file audio (không có `Range` header).

**Response `206 Partial Content`:** Đoạn audio theo Range request (có `Content-Range` header).

---
