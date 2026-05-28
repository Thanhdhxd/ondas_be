## Home

### GET `/api/home`

Lấy dữ liệu trang chủ gồm bài hát nổi bật, nghệ sĩ và album mới nhất. Trả về 3 section trong 1 request duy nhất.

**Auth:** Không yêu cầu

**Query Params:**
| Param | Type | Bắt buộc | Mặc định | Mô tả |
|---|---|---|---|---|
| `trendingLimit` | integer | ❌ | `10` | Số bài hát nổi bật |
| `artistLimit` | integer | ❌ | `10` | Số nghệ sĩ |
| `albumLimit` | integer | ❌ | `10` | Số album mới nhất |

**Ví dụ:**
```
GET /api/home                                           → mặc định 10 mỗi section
GET /api/home?trendingLimit=5&artistLimit=6&albumLimit=8 → custom limit
```

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "trendingSongs": [
      {
        "id": "uuid",
        "title": "Nơi Này Có Anh",
        "slug": "noi-nay-co-anh",
        "durationSeconds": 210,
        "audioUrl": "https://...",
        "coverUrl": "https://...",
        "playCount": 99999,
        "active": true,
        "artists": [ { "id": "uuid", "name": "Sơn Tùng M-TP", "avatarUrl": "https://..." } ],
        "genres": [ { "id": 1, "name": "V-Pop" } ]
      }
    ],
    "featuredArtists": [
      {
        "id": "uuid",
        "name": "Sơn Tùng M-TP",
        "slug": "son-tung-m-tp",
        "bio": "...",
        "avatarUrl": "https://...",
        "country": "Vietnam"
      }
    ],
    "newReleases": [
      {
        "id": "uuid",
        "title": "Tâm 9",
        "slug": "tam-9",
        "coverUrl": "https://...",
        "releaseDate": "2026-04-01",
        "albumType": "ALBUM",
        "totalTracks": 9,
        "artistIds": ["uuid"],
        "tracklist": []
      }
    ]
  }
}
```

---
