## Play History

> **Lưu ý:** `play_count` và lịch sử nghe **không** được ghi tự động qua stream. Client phải gọi `POST /api/play-history` một lần khi người dùng thực sự bắt đầu nghe bài.

### POST `/api/play-history`

Ghi một lượt nghe: lưu lịch sử và tăng `play_count` của bài hát.

**Auth:** ✅ Yêu cầu (JWT)

**Request Body:**
| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `songId` | UUID | ✅ | ID bài hát |
| `source` | string | ❌ | Nguồn nghe: `search`, `album`, `playlist`, `home`, `artist`, `favorites`, `history` |

```json
{
  "songId": "uuid",
  "source": "home"
}
```

**Response `200 OK`:**
```json
{ "success": true, "message": "OK", "data": null }
```

**Response `404 Not Found`:** Nếu bài hát không tồn tại hoặc đang bị ẩn.

---

### GET `/api/play-history`

Lấy lịch sử nghe nhạc của user đang đăng nhập, sắp xếp theo thời gian mới nhất.

**Auth:** ✅ Yêu cầu (JWT)

**Query Params:**
| Param | Type | Bắt buộc | Mặc định | Mô tả |
|---|---|---|---|---|
| `page` | integer | ❌ | `0` | Trang (0-based) |
| `size` | integer | ❌ | `20` | Số phần tử mỗi trang |

**Response `200 OK`:** Trả về `PageResultDto<PlayHistoryResponse>`.

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "items": [
      {
        "id": 1,
        "song": {
          "id": "uuid",
          "title": "Nơi Này Có Anh",
          "coverUrl": "https://...",
          "durationSeconds": 210,
          "audioUrl": "https://..."
        },
        "playedAt": "2026-04-27T15:30:00",
        "source": "home"
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

### DELETE `/api/play-history`

Xóa toàn bộ lịch sử nghe nhạc của user đang đăng nhập.

**Auth:** ✅ Yêu cầu (JWT)

**Response `200 OK`:**
```json
{ "success": true, "message": "OK", "data": null }
```

---

### DELETE `/api/play-history/{id}`

Xóa một mục lịch sử cụ thể. Chỉ xóa được mục thuộc về user đang đăng nhập.

**Auth:** ✅ Yêu cầu (JWT)

**Path Params:**
| Param | Type | Mô tả |
|---|---|---|
| `id` | Long | ID của mục lịch sử |

**Response `200 OK`:**
```json
{ "success": true, "message": "OK", "data": null }
```

**Response `404 Not Found`:** Nếu mục không tồn tại hoặc không thuộc user hiện tại.

---
