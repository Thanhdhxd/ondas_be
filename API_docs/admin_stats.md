## Admin — Thong ke nang cao

> Tat ca endpoint trong section nay yeu cau role `ADMIN`.

### GET `/api/admin/stats/top-songs`

Lay top bai hat theo so luot phat trong khoang thoi gian.

**Auth:** ✅ `ADMIN`

**Query Params:**
| Param | Type | Bat buoc | Mac dinh | Mo ta |
|---|---|---|---|---|
| `from` | date | ❌ | `today - 29 days` | Ngay bat dau (yyyy-MM-dd) |
| `to` | date | ❌ | `today` | Ngay ket thuc (yyyy-MM-dd) |
| `limit` | integer | ❌ | `10` | So luong top tra ve (toi da 100) |

**Vi du:**
```
GET /api/admin/stats/top-songs?from=2026-05-01&to=2026-05-31&limit=10
```

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
      "playCount": 1234,
      "artists": [
        { "id": "uuid", "name": "Artist A", "avatarUrl": "https://..." }
      ]
    }
  ]
}
```

---

### GET `/api/admin/stats/top-artists`

Lay top nghe si theo tong luot phat (tong tu cac bai cua nghe si).

**Auth:** ✅ `ADMIN`

**Query Params:**
| Param | Type | Bat buoc | Mac dinh | Mo ta |
|---|---|---|---|---|
| `from` | date | ❌ | `today - 29 days` | Ngay bat dau (yyyy-MM-dd) |
| `to` | date | ❌ | `today` | Ngay ket thuc (yyyy-MM-dd) |
| `limit` | integer | ❌ | `10` | So luong top tra ve (toi da 100) |

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
      "playCount": 4567
    }
  ]
}
```

---

### GET `/api/admin/stats/plays-daily`

Lay luot phat theo ngay trong khoang thoi gian.

**Auth:** ✅ `ADMIN`

**Query Params:**
| Param | Type | Bat buoc | Mac dinh | Mo ta |
|---|---|---|---|---|
| `from` | date | ❌ | `today - 29 days` | Ngay bat dau (yyyy-MM-dd) |
| `to` | date | ❌ | `today` | Ngay ket thuc (yyyy-MM-dd) |

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "success.ok",
  "data": [
    { "date": "2026-05-01", "playCount": 120 },
    { "date": "2026-05-02", "playCount": 98 }
  ]
}
```

---

### GET `/api/admin/stats/dau-mau`

Lay DAU (nguoi dung hoat dong trong ngay) va MAU (30 ngay gan nhat tinh tu ngay chon).

**Auth:** ✅ `ADMIN`

**Query Params:**
| Param | Type | Bat buoc | Mac dinh | Mo ta |
|---|---|---|---|---|
| `date` | date | ❌ | `today` | Ngay tham chieu (yyyy-MM-dd) |

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "success.ok",
  "data": {
    "date": "2026-05-31",
    "dau": 120,
    "mau": 820,
    "mauWindowDays": 30
  }
}
```
