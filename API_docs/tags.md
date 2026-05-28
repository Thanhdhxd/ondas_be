# Tag/Mood API (Standalone)

Base URL: `/api`

All responses are wrapped by:
```
{
  "success": true,
  "message": "OK",
  "data": { ... }
}
```

## 1) Tag CRUD (system tags)
System tags are managed by Admin/Content Manager only.

### Create tag
`POST /api/tags`

**Auth**: ✅ `ADMIN`, `CONTENT_MANAGER`

**Request body**
```json
{
  "name": "Chill",
  "type": "mood",
  "colorHex": "#7AC9FF"
}
```

**Validation**
- `name` is required
- `type` should be one of: `mood`, `theme`, `activity`, `era`
- `colorHex` must match `#RRGGBB` when provided

**Response 201**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": 12,
    "name": "Chill",
    "type": "mood",
    "colorHex": "#7AC9FF"
  }
}
```

### Update tag
`PUT /api/tags/{id}`

**Auth**: ✅ `ADMIN`, `CONTENT_MANAGER`

**Request body**
```json
{
  "name": "Late Night",
  "type": "theme",
  "colorHex": "#E59F71"
}
```

**Response 200**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": 12,
    "name": "Late Night",
    "type": "theme",
    "colorHex": "#E59F71"
  }
}
```

### Get tag by id
`GET /api/tags/{id}`

**Auth**: ✅ Yêu cầu (JWT)

**Response 200**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": 12,
    "name": "Chill",
    "type": "mood",
    "colorHex": "#7AC9FF"
  }
}
```

### Get all tags
`GET /api/tags`

**Auth**: ✅ Yêu cầu (JWT)

**Query params**
- `type` (optional): `mood | theme | activity | era`

**Response 200**
```json
{
  "success": true,
  "message": "OK",
  "data": [
    { "id": 1, "name": "Chill", "type": "mood", "colorHex": "#7AC9FF" },
    { "id": 2, "name": "Workout", "type": "activity", "colorHex": "#FF8A65" }
  ]
}
```

### Search tags by name
`GET /api/tags/search`

**Auth**: ✅ Yêu cầu (JWT)

**Query params**
| Param | Type | Bắt buộc | Mặc định | Mô tả |
|---|---|---|---|---|
| `query` | string | ✅ | — | Từ khóa |
| `mode` | string | ❌ | `contains` | `contains`, `startsWith`, `equals` |
| `page` | integer | ❌ | `0` | |
| `size` | integer | ❌ | `20` | |

**Response 200**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "items": [
      { "id": 1, "name": "Chill", "type": "mood", "colorHex": "#7AC9FF" }
    ],
    "page": 0,
    "size": 20,
    "totalItems": 1,
    "totalPages": 1
  }
}
```

### Delete tag
`DELETE /api/tags/{id}`

**Auth**: ✅ `ADMIN`, `CONTENT_MANAGER`

**Response 200**
```json
{
  "success": true,
  "message": "OK",
  "data": null
}
```

---

## 2) Song Tags (attach/detach existing tags)
Users only attach/detach existing tags to songs. No free text.

### Get tags of a song
`GET /api/songs/{id}/tags`

**Auth**: ✅ Yêu cầu (JWT)

**Response 200**
```json
{
  "success": true,
  "message": "OK",
  "data": [
    { "id": 1, "name": "Chill", "type": "mood", "colorHex": "#7AC9FF" },
    { "id": 4, "name": "Late Night", "type": "theme", "colorHex": "#E59F71" }
  ]
}
```

### Add tags to a song
`POST /api/songs/{id}/tags`

**Auth**: ✅ `ADMIN`, `CONTENT_MANAGER`

**Request body**
```json
{
  "tagIds": [1, 4, 7]
}
```

**Response 200**
```json
{
  "success": true,
  "message": "OK",
  "data": [
    { "id": 1, "name": "Chill", "type": "mood", "colorHex": "#7AC9FF" },
    { "id": 4, "name": "Late Night", "type": "theme", "colorHex": "#E59F71" },
    { "id": 7, "name": "Study", "type": "activity", "colorHex": "#8BC34A" }
  ]
}
```

### Remove tags from a song
`DELETE /api/songs/{id}/tags`

**Auth**: ✅ `ADMIN`, `CONTENT_MANAGER`

**Request body**
```json
{
  "tagIds": [4]
}
```

**Response 200**
```json
{
  "success": true,
  "message": "OK",
  "data": [
    { "id": 1, "name": "Chill", "type": "mood", "colorHex": "#7AC9FF" }
  ]
}
```

### Replace tags of a song
`PUT /api/songs/{id}/tags`

**Auth**: ✅ `ADMIN`, `CONTENT_MANAGER`

**Request body**
```json
{
  "tagIds": [2, 3]
}
```

**Response 200**
```json
{
  "success": true,
  "message": "OK",
  "data": [
    { "id": 2, "name": "Workout", "type": "activity", "colorHex": "#FF8A65" },
    { "id": 3, "name": "Romantic", "type": "theme", "colorHex": "#F48FB1" }
  ]
}

### Filter songs by tags (multi-select)
`GET /api/songs?tagIds=1&tagIds=4&tagIds=7&page=0&size=20`

**Auth**: ✅ Yêu cầu (JWT)

**Notes**
- Returns songs that match **all** provided tags.
- `tagIds` can be repeated as query params.

**Response 200**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "items": [
      {
        "id": "c1b9...",
        "title": "Ngay Thang Sau Nay",
        "artists": [
          { "id": "a1...", "name": "Lillie" }
        ],
        "genres": [
          { "id": 1, "name": "Nhac Tre" }
        ]
      }
    ],
    "page": 0,
    "size": 20,
    "totalItems": 1,
    "totalPages": 1
  }
}
```

---

## 3) UI Rendering Notes (for mobile/web)
- If `colorHex` is `null`, fallback to a color by `type` to avoid white pills.
- Suggested type colors (fallback):
  - `mood`: `#7AC9FF`
  - `theme`: `#E59F71`
  - `activity`: `#8BC34A`
  - `era`: `#9575CD`

---

## 4) Common Errors
- `400 Bad Request`: validation errors (missing `name`, invalid `colorHex` format, invalid `type`)
- `401 Unauthorized`: missing/invalid token
- `403 Forbidden`: insufficient role to manage tags
- `404 Not Found`: tag or song not found
- `409 Conflict`: duplicate tag name
