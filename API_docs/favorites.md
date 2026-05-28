## Favorites

> Cho phép user thêm/xóa bài hát yêu thích và lấy danh sách bài hát đã yêu thích.

### POST `/api/favorites/{songId}`

Thêm bài hát vào danh sách yêu thích.

**Auth:** ✅ Yêu cầu (JWT)

**Path Params:**
| Param | Type | Mô tả |
|---|---|---|
| `songId` | UUID | ID bài hát muốn thêm |

**Response `200 OK`:**
```json
{ "success": true, "message": "OK", "data": null }
```

**Response `404 Not Found`:** Bài hát không tồn tại hoặc đang bị ẩn.

**Response `409 Conflict`:** Bài hát đã có trong danh sách yêu thích.

---

### DELETE `/api/favorites/{songId}`

Xóa bài hát khỏi danh sách yêu thích.

**Auth:** ✅ Yêu cầu (JWT)

**Path Params:**
| Param | Type | Mô tả |
|---|---|---|
| `songId` | UUID | ID bài hát muốn xóa |

**Response `200 OK`:**
```json
{ "success": true, "message": "OK", "data": null }
```

**Response `404 Not Found`:** Bài hát không có trong danh sách yêu thích.

---

### GET `/api/favorites/{songId}/status`

Kiểm tra một bài hát có đang trong danh sách yêu thích của user hay không.

**Auth:** ✅ Yêu cầu (JWT)

**Path Params:**
| Param | Type | Mô tả |
|---|---|---|
| `songId` | UUID | ID bài hát cần kiểm tra |

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "OK",
  "data": true
}
```

> `data` là `true` nếu đã yêu thích, `false` nếu chưa.

---

### GET `/api/favorites`

Lấy danh sách bài hát yêu thích của user đang đăng nhập, sắp xếp theo thời gian thêm mới nhất.

**Auth:** ✅ Yêu cầu (JWT)

**Query Params:**
| Param | Type | Bắt buộc | Mặc định | Mô tả |
|---|---|---|---|---|
| `page` | integer | ❌ | `0` | Trang (0-based) |
| `size` | integer | ❌ | `20` | Số phần tử mỗi trang |

**Response `200 OK`:** Trả về `PageResultDto<FavoriteSongResponse>`.

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "items": [
      {
        "songId": "uuid",
        "title": "Nơi Này Có Anh",
        "slug": "noi-nay-co-anh",
        "durationSeconds": 210,
        "audioUrl": "https://...",
        "audioFormat": "mp3",
        "coverUrl": "https://...",
        "playCount": 12345,
        "favoritedAt": "2026-05-01T10:00:00",
        "artists": [
          { "id": "uuid", "name": "Sơn Tùng M-TP", "avatarUrl": "https://..." }
        ],
        "genres": [
          { "id": 1, "name": "V-Pop" }
        ]
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
