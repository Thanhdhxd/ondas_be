## Lyrics

### GET `/api/songs/{songId}/lyrics`

Lấy lyrics của một bài hát (plain text + synced nếu có).

**Auth:** ✅ Yêu cầu (JWT)

**Path Params:**
| Param | Type | Mô tả |
|---|---|---|
| `songId` | UUID | ID bài hát |

**Response `200 OK`:** Trả về `LyricsResponse`.
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": "uuid",
    "songId": "uuid",
    "plainText": "Lời bài hát...",
    "hasSynced": true,
    "language": "vi",
    "createdAt": "2026-05-08T10:00:00",
    "updatedAt": "2026-05-08T10:00:00",
    "syncedLines": [
      { "id": 1, "startMs": 0, "endMs": 1200, "lineText": "Xin chao", "lineIndex": 0 },
      { "id": 2, "startMs": 1200, "endMs": 2400, "lineText": "The gioi", "lineIndex": 1 }
    ]
  }
}
```

**Response `404 Not Found`:** Nếu lyrics chưa tồn tại cho bài hát.

---

### POST `/api/songs/{songId}/lyrics`

Tạo mới lyrics cho bài hát (plain text + synced nếu có). **Cả `plainText` và `syncedLines` đều có thể không có, nhưng phải có ít nhất một trong hai.**

**Auth:** ✅ `ADMIN` hoặc `CONTENT_MANAGER`

**Path Params:**
| Param | Type | Mô tả |
|---|---|---|
| `songId` | UUID | ID bài hát |

**Request Body (`CreateLyricsRequest`):**
| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `language` | string | ❌ | Ngôn ngữ (tối đa 10 ký tự) |
| `plainText` | string | ❌ | Lời bài hát dạng plain text |
| `syncedLines` | SyncedLyricsLineDto[] | ❌ | Danh sách dòng synced |

**`SyncedLyricsLineDto`:**
| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `startMs` | integer | ✅ | Thời gian bắt đầu (>= 0) |
| `endMs` | integer | ❌ | Thời gian kết thúc (nếu có phải > startMs) |
| `lineText` | string | ✅ | Nội dung dòng |
| `lineIndex` | short | ✅ | Thứ tự dòng (0-based, tuần tự) |

```json
{
  "language": "vi",
  "plainText": "Lời bài hát...",
  "syncedLines": [
    { "startMs": 0, "endMs": 1200, "lineText": "Xin chao", "lineIndex": 0 },
    { "startMs": 1200, "endMs": 2400, "lineText": "The gioi", "lineIndex": 1 }
  ]
}
```

**Response `201 Created`:** Trả về `LyricsResponse`.

**Response `400 Bad Request`:** Nếu `lineIndex` không tuần tự, `endMs <= startMs`, hoặc các dòng bị chồng thời gian.

**Response `404 Not Found`:** Nếu bài hát không tồn tại.

**Response `409 Conflict`:** Nếu lyrics đã tồn tại cho bài hát.

---

### PATCH `/api/songs/{songId}/lyrics`

Cập nhật một phần lyrics. Chỉ field nào được gửi mới được áp dụng.

**Auth:** ✅ `ADMIN` hoặc `CONTENT_MANAGER`

**Request Body (`PatchLyricsRequest`):**
| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `language` | string | ❌ | Ngôn ngữ (tối đa 10 ký tự) |
| `plainText` | string | ❌ | Lời bài hát dạng plain text |
| `syncedLines` | SyncedLyricsLineDto[] | ❌ | `null` = không đổi, `[]` = xoá synced, danh sách = thay thế toàn bộ |

```json
{
  "plainText": "Lời bài hát cập nhật...",
  "syncedLines": []
}
```

**Response `200 OK`:** Trả về `LyricsResponse`.

**Response `400 Bad Request`:** Nếu `lineIndex` không tuần tự, `endMs <= startMs`, hoặc các dòng bị chồng thời gian.

**Response `404 Not Found`:** Nếu lyrics chưa tồn tại cho bài hát.

---

### DELETE `/api/songs/{songId}/lyrics`

Xoá toàn bộ lyrics của bài hát.

**Auth:** ✅ `ADMIN` hoặc `CONTENT_MANAGER`

**Response `200 OK`:**
```json
{ "success": true, "message": "OK", "data": null }
```

**Response `404 Not Found`:** Nếu lyrics không tồn tại.

---
