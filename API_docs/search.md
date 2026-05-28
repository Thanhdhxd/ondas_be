## Search

### GET `/api/search/suggestions`

Lấy dữ liệu gợi ý cho màn hình tìm kiếm khi user chưa gõ từ khóa. Trả về lịch sử tìm kiếm cá nhân, trending toàn hệ thống, top bài hát và tất cả thể loại.

**Auth:** ✅ Yêu cầu (JWT)

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "recentSearches": ["sơn tùng", "bích phương", "erik"],
    "trendingSearches": ["binz", "hoàng thuỳ linh", "tlinh"],
    "trendingSongs": [
      {
        "id": "uuid",
        "title": "Nơi Này Có Anh",
        "coverUrl": "https://...",
        "playCount": 99999,
        "artists": [ { "id": "uuid", "name": "Sơn Tùng M-TP", "avatarUrl": "https://..." } ],
        "genres": [ { "id": 1, "name": "V-Pop" } ]
      }
    ],
    "genres": [
      { "id": 1, "name": "V-Pop", "slug": "v-pop", "description": "...", "coverUrl": "https://..." }
    ]
  }
}
```

> **Ghi chú:**
> - `recentSearches`: tối đa 10 từ khóa gần nhất của user hiện tại (đã dedup).
> - `trendingSearches`: tối đa 10 từ khóa được tìm nhiều nhất toàn hệ thống trong 7 ngày qua.
> - `trendingSongs`: top 10 bài hát theo `play_count`.
> - `genres`: toàn bộ thể loại — dùng để hiển thị dạng Browse by Genre.

---

### DELETE `/api/search/history`

Xóa toàn bộ lịch sử tìm kiếm của user đang đăng nhập.

**Auth:** ✅ Yêu cầu (JWT)

**Response `200 OK`:**
```json
{ "success": true, "message": "OK", "data": null }
```

---

### GET `/api/search`

Tìm kiếm tổng hợp bài hát, nghệ sĩ và album theo từ khóa trong một request duy nhất. Query sẽ được **tự động lưu vào lịch sử tìm kiếm** của user.

**Auth:** ✅ Yêu cầu (JWT)

**Query Params:**
| Param | Type | Bắt buộc | Mặc định | Mô tả |
|---|---|---|---|---|
| `query` | string | ✅ | — | Từ khóa tìm kiếm (bắt buộc, không được để trống) |
| `page` | integer | ❌ | `0` | Trang (0-based) |
| `size` | integer | ❌ | `10` | Số phần tử mỗi loại mỗi trang (tối đa 50) |

**Ví dụ:**
```
GET /api/search?query=son+tung&page=0&size=10
```

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "query": "son tung",
    "page": 0,
    "size": 10,
    "totalSongs": 5,
    "totalArtists": 1,
    "totalAlbums": 2,
    "songs": [ { ... } ],
    "artists": [ { ... } ],
    "albums": [ { ... } ]
  }
}
```

**Response `400 Bad Request`:** Khi `query` để trống.

---
