## System Playlists

> System playlists (danh sách phát hệ thống) do `ADMIN` hoặc `CONTENT_MANAGER` quản lý. Người dùng chỉ có quyền xem các playlist đang active.

### GET `/api/system-playlists`

Lấy danh sách playlist hệ thống đang active.

**Auth:** ✅ Yêu cầu (JWT)

**Query Params:**
| Param | Type | Bắt buộc | Mặc định | Mô tả |
|---|---|---|---|---|
| `query` | string | ❌ | — | Tìm kiếm theo tên |
| `isActive` | boolean | ❌ | `true` | Lọc theo trạng thái active |
| `page` | integer | ❌ | `0` | Trang (0-based) |
| `size` | integer | ❌ | `20` | Số phần tử mỗi trang |

**Response `200 OK`:** Trả về `PageResultDto<SystemPlaylistResponse>`.

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "items": [
      {
        "id": "uuid",
        "name": "Top 100 Việt Nam",
        "description": "Top bài hát Việt Nam được yêu thích nhất",
        "coverUrl": "https://...",
        "colorHex": "#FF6B6B",
        "isActive": true,
        "totalSongs": 100,
        "createdAt": "...",
        "updatedAt": "...",
        "createdBy": "admin@example.com",
        "songs": []
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 5,
    "totalPages": 1
  }
}
```

---

### GET `/api/system-playlists/{id}`

Lấy chi tiết playlist hệ thống (kèm danh sách bài hát).

**Auth:** ✅ Yêu cầu (JWT)

**Response `200 OK`:** Trả về `SystemPlaylistResponse` (bao gồm `songs`).

**Response `404 Not Found`:** Nếu playlist không tồn tại hoặc không active.

---

## Admin System Playlists

> Base URL: `/api/admin/system-playlists`
> Tất cả endpoint yêu cầu `ADMIN` hoặc `CONTENT_MANAGER`.

### POST `/api/admin/system-playlists`

Tạo playlist hệ thống mới.

**Content-Type:** `multipart/form-data`

**Parts:**
| Part | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `data` | JSON (`CreateSystemPlaylistRequest`) | ✅ | Thông tin playlist |
| `cover` | File | ❌ | Ảnh bìa |

**`CreateSystemPlaylistRequest`:**
| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `name` | string | ✅ | Tên playlist |
| `description` | string | ❌ | Mô tả |
| `colorHex` | string | ❌ | Màu sắc (`#RRGGBB`) |
| `isActive` | boolean | ❌ | Mặc định `true` |

**Response `201 Created`:** Trả về `SystemPlaylistResponse`.

---

### PUT `/api/admin/system-playlists/{id}`

Cập nhật playlist hệ thống.

**Content-Type:** `multipart/form-data`

**Parts:**
| Part | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `data` | JSON (`UpdateSystemPlaylistRequest`) | ✅ | Partial update |
| `cover` | File | ❌ | Ảnh bìa mới |

**`UpdateSystemPlaylistRequest` fields:** `name`, `description`, `colorHex`, `isActive` — tất cả optional.

**Response `200 OK`:** Trả về `SystemPlaylistResponse` đã cập nhật.

---

### GET `/api/admin/system-playlists`

Lấy danh sách tất cả playlist hệ thống (cả active và inactive).

**Query Params:**
| Param | Type | Bắt buộc | Mặc định | Mô tả |
|---|---|---|---|---|
| `query` | string | ❌ | — | Tìm kiếm theo tên |
| `isActive` | boolean | ❌ | — | Lọc theo trạng thái |
| `page` | integer | ❌ | `0` | |
| `size` | integer | ❌ | `20` | |

**Response `200 OK`:** Trả về `PageResultDto<SystemPlaylistResponse>`.

---

### GET `/api/admin/system-playlists/{id}`

Lấy chi tiết playlist hệ thống (kể cả inactive).

**Response `200 OK`:** Trả về `SystemPlaylistResponse`.

---

### DELETE `/api/admin/system-playlists/{id}`

Xóa playlist hệ thống.

**Response `200 OK`:**
```json
{ "success": true, "message": "OK", "data": null }
```

---

### POST `/api/admin/system-playlists/{id}/songs`

Thêm bài hát vào playlist hệ thống.

**Request Body:**
| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `songId` | UUID | ✅ | ID bài hát |

```json
{ "songId": "uuid" }
```

**Response `200 OK`:** Trả về `SystemPlaylistResponse` đã cập nhật.

**Response `409 Conflict`:** Nếu bài hát đã có trong playlist.

---

### DELETE `/api/admin/system-playlists/{id}/songs/{songId}`

Xóa bài hát khỏi playlist hệ thống.

**Response `200 OK`:** Trả về `SystemPlaylistResponse` đã cập nhật.

**Response `404 Not Found`:** Nếu bài hát không có trong playlist.

---

### PUT `/api/admin/system-playlists/{id}/songs/reorder`

Sắp xếp lại thứ tự bài hát trong playlist hệ thống.

**Request Body:**
| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `songIds` | UUID[] | ✅ | Danh sách ID bài hát theo thứ tự mới |

```json
{
  "songIds": ["uuid-b", "uuid-a", "uuid-c"]
}
```

**Response `200 OK`:** Trả về `SystemPlaylistResponse` với thứ tự đã cập nhật.

---
