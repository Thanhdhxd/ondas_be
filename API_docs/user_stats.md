## User — Thong ke ca nhan

> Tat ca endpoint trong section nay yeu cau dang nhap (JWT).

### GET `/api/stats/me/listening-time`

Lay tong thoi gian va so luong bai hat da nghe cua user dang dang nhap.

**Auth:** ✅ Yeu cau (JWT)

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "success.ok",
  "data": {
    "totalListeningSeconds": 480,
    "totalListeningMinutes": 8.0,
    "totalListeningHours": 0.13,
    "totalSongsPlayed": 24
  }
}
```

---

### GET `/api/stats/me/top-songs`

Lay danh sach cac bai hat duoc nghe nhieu nhat cua user dang dang nhap.

**Auth:** ✅ Yeu cau (JWT)

**Query Params:**
| Param | Type | Bat buoc | Mac dinh | Mo ta |
|---|---|---|---|---|
| `limit` | integer | ❌ | `10` | So luong top tra ve |

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "success.ok",
  "data": [
    {
      "id": "uuid",
      "title": "Song A",
      "coverUrl": "https://...",
      "playCount": 42,
      "artists": [
        { "id": "uuid", "name": "Artist A", "avatarUrl": "https://..." }
      ]
    }
  ]
}
```

---

### GET `/api/stats/me/top-artists`

Lay danh sach cac ca si duoc nghe nhieu nhat cua user dang dang nhap.

**Auth:** ✅ Yeu cau (JWT)

**Query Params:**
| Param | Type | Bat buoc | Mac dinh | Mo ta |
|---|---|---|---|---|
| `limit` | integer | ❌ | `10` | So luong top tra ve |

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "success.ok",
  "data": [
    {
      "id": "uuid",
      "name": "Artist A",
      "avatarUrl": "https://...",
      "playCount": 77
    }
  ]
}
```
