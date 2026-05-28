## Playlists

> Mỗi user quản lý playlist của chính mình. Playlist có thể là **public** (ai cũng xem được) hoặc **private** (chỉ chủ sở hữu).

### POST `/api/playlists`

Tạo playlist mới.

**Auth:** ✅ Yêu cầu (JWT)

**Content-Type:** `multipart/form-data`

**Parts:**
| Part | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `data` | JSON (`CreatePlaylistRequest`) | ✅ | Thông tin playlist |
| `cover` | File | ❌ | Ảnh bìa |

**`CreatePlaylistRequest`:**
| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `name` | string | ✅ | Tên playlist |
| `description` | string | ❌ | Mô tả |
| `isPublic` | boolean | ❌ | Công khai hay không (mặc định `false`) |

```json
{
  "name": "Nhạc hay 2026",
  "description": "Tuyển tập bài hát yêu thích",
  "isPublic": true
}
```

**Response `201 Created`:**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": "uuid",
    "userId": "uuid",
    "name": "Nhạc hay 2026",
    "description": "Tuyển tập bài hát yêu thích",
    "coverUrl": null,
    "isPublic": true,
    "totalSongs": 0,
    "createdAt": "2026-04-27T10:00:00",
    "updatedAt": "2026-04-27T10:00:00",
    "songs": []
  }
}
```

---

### PUT `/api/playlists/{id}`

Cập nhật playlist. Chỉ chủ sở hữu mới được cập nhật.

**Auth:** ✅ Yêu cầu (JWT)

**Content-Type:** `multipart/form-data`

**Path Params:**
| Param | Type | Mô tả |
|---|---|---|
| `id` | UUID | ID playlist |

**Parts:**
| Part | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `data` | JSON (`UpdatePlaylistRequest`) | ✅ | Partial update |
| `cover` | File | ❌ | Ảnh bìa mới |

**`UpdatePlaylistRequest` fields:** `name`, `description`, `isPublic` — tất cả đều optional.

**Response `200 OK`:** Trả về `PlaylistResponse` đã cập nhật.

---

### GET `/api/playlists/{id}`

Lấy chi tiết playlist kèm danh sách bài hát.

**Auth:** ✅ Yêu cầu (JWT) — playlist private chỉ chủ sở hữu mới xem được.

**Response `200 OK`:** Trả về `PlaylistResponse`.

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": "uuid",
    "userId": "uuid",
    "name": "Nhạc hay 2026",
    "description": "...",
    "coverUrl": "https://...",
    "isPublic": true,
    "totalSongs": 2,
    "createdAt": "2026-04-27T10:00:00",
    "updatedAt": "2026-04-27T10:00:00",
    "songs": [
      {
        "position": 1,
        "addedAt": "2026-04-27T10:05:00",
        "song": {
          "id": "uuid",
          "title": "Nơi Này Có Anh",
          "coverUrl": "https://...",
          "durationSeconds": 210,
          "audioUrl": "https://..."
        }
      }
    ]
  }
}
```

---

### GET `/api/playlists`

Lấy danh sách playlist có phân trang.

**Auth:** ✅ Yêu cầu (JWT)

**Query Params:**
| Param | Type | Bắt buộc | Mặc định | Mô tả |
|---|---|---|---|---|
| `query` | string | ❌ | — | Tìm kiếm theo tên playlist |
| `owner` | boolean | ❌ | `false` | `true` = chỉ lấy playlist của user hiện tại |
| `isPublic` | boolean | ❌ | — | Lọc theo trạng thái công khai |
| `songId` | UUID | ❌ | — | Nếu truyền, mỗi playlist trong kết quả sẽ có thêm field `containsSong` cho biết bài hát đó đã có trong playlist chưa |
| `page` | integer | ❌ | `0` | Trang (0-based) |
| `size` | integer | ❌ | `20` | Số phần tử mỗi trang |

**Ví dụ:**
```
GET /api/playlists?owner=true&page=0&size=20                        → playlist của tôi
GET /api/playlists?isPublic=true&page=0&size=20                     → tất cả playlist public
GET /api/playlists?query=nhac+hay&page=0&size=20                    → tìm theo tên
GET /api/playlists?owner=true&songId=<uuid>                         → playlist của tôi kèm trạng thái bài hát
```

**Response `200 OK`:** Trả về `PageResultDto<PlaylistResponse>`.

> **Lưu ý về `containsSong`:** Field này chỉ xuất hiện (non-null) khi truyền `songId`. Dùng để hiển thị trạng thái "đã thêm / chưa thêm" trong màn hình chọn playlist.

---

### DELETE `/api/playlists/{id}`

Xóa playlist. Chỉ chủ sở hữu mới được xóa.

**Auth:** ✅ Yêu cầu (JWT)

**Response `200 OK`:**
```json
{ "success": true, "message": "OK", "data": null }
```

---

### POST `/api/playlists/{id}/songs`

Thêm bài hát vào playlist.

**Auth:** ✅ Yêu cầu (JWT)

**Path Params:**
| Param | Type | Mô tả |
|---|---|---|
| `id` | UUID | ID playlist |

**Request Body:**
| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `songId` | UUID | ✅ | ID bài hát muốn thêm |

```json
{
  "songId": "uuid"
}
```

**Response `200 OK`:** Trả về `PlaylistResponse` đã cập nhật.

---

### DELETE `/api/playlists/{id}/songs/{songId}`

Xóa bài hát khỏi playlist.

**Auth:** ✅ Yêu cầu (JWT)

**Path Params:**
| Param | Type | Mô tả |
|---|---|---|
| `id` | UUID | ID playlist |
| `songId` | UUID | ID bài hát muốn xóa |

**Response `200 OK`:** Trả về `PlaylistResponse` đã cập nhật.

---

### PUT `/api/playlists/{id}/songs/reorder`

Sắp xếp lại thứ tự bài hát trong playlist. Danh sách `songIds` xác định thứ tự mới.

**Auth:** ✅ Yêu cầu (JWT)

**Request Body:**
| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `songIds` | UUID[] | ✅ | Danh sách ID bài hát theo thứ tự mới |

```json
{
  "songIds": ["uuid-b", "uuid-a", "uuid-c"]
}
```

**Response `200 OK`:** Trả về `PlaylistResponse` với thứ tự đã cập nhật.

---
