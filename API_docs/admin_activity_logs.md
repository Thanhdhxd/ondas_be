## Admin — Nhat ky hoat dong (Activity Log)

> Tat ca endpoint trong section nay yeu cau role `ADMIN`.

### GET `/api/admin/activity-logs`

Lay danh sach nhat ky hoat dong cua admin voi kha nang loc theo nhieu tieu chi. Ket qua phan trang, sap xep theo `createdAt` giam dan (moi nhat len dau).

**Auth:** ✅ `ADMIN`

**Query Params:**
| Param | Type | Bat buoc | Mac dinh | Mo ta |
|---|---|---|---|---|
| `actorId` | UUID | ❌ | — | Loc theo UUID cua admin thuc hien |
| `searchUser` | string | ❌ | — | Tim kiem theo email hoac ten hien thi cua admin |
| `action` | string | ❌ | — | Loc theo loai hanh dong (xem bang AuditAction bên duoi) |
| `from` | datetime | ❌ | — | Thoi gian bat dau (ISO 8601, vd: `2026-05-01T00:00:00`) |
| `to` | datetime | ❌ | — | Thoi gian ket thuc (ISO 8601, vd: `2026-05-31T23:59:59`) |
| `page` | integer | ❌ | `0` | So trang (0-indexed) |
| `size` | integer | ❌ | `20` | So ban ghi moi trang |

**Vi du:**
```
GET /api/admin/activity-logs?action=BAN_USER&from=2026-06-01T00:00:00&page=0&size=20
GET /api/admin/activity-logs?searchUser=thanh&page=0&size=10
GET /api/admin/activity-logs?actorId=3fd8c1a2-cf94-40b2-af18-217ad671f82f
```

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "success.ok",
  "data": {
    "items": [
      {
        "id": 2,
        "actorId": "3fd8c1a2-cf94-40b2-af18-217ad671f82f",
        "actorEmail": "admin@example.com",
        "actorDisplayName": "Hoang Thanh",
        "action": "BAN_USER",
        "resourceType": "USER",
        "resourceId": "737b932d-9abf-4660-aa87-160a4d6cf076",
        "resourceName": "Thanh Hoang",
        "metadata": "{\"id\": \"737b932d-...\", \"request\": {\"banReason\": \"Vi pham dieu khoan\"}}",
        "ipAddress": "192.168.1.10",
        "createdAt": "2026-06-04T20:49:10.993"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

**Mo ta cac truong response:**
| Truong | Type | Mo ta |
|---|---|---|
| `id` | long | ID cua ban ghi log |
| `actorId` | UUID | ID cua admin da thuc hien hanh dong |
| `actorEmail` | string | Email cua admin |
| `actorDisplayName` | string | Ten hien thi cua admin |
| `action` | string | Ma hanh dong (xem bang AuditAction bên duoi) |
| `resourceType` | string | Loai tai nguyen bi tac dong (`USER`, `SONG`, `ARTIST`, `ALBUM`, `SYSTEM_PLAYLIST`) |
| `resourceId` | UUID | ID cua tai nguyen bi tac dong |
| `resourceName` | string | Ten tai nguyen bi tac dong tai thoi diem thuc hien |
| `metadata` | string (JSON) | Thong tin chi tiet cua yeu cau (vd: ly do ban, noi dung chinh sua) |
| `ipAddress` | string | Dia chi IP cua admin khi thuc hien hanh dong |
| `createdAt` | datetime | Thoi diem ghi nhat ky (ISO 8601, khong co timezone) |

---

### Bang gia tri `action` (AuditAction)

| Gia tri | Nhom | Mo ta |
|---|---|---|
| `BAN_USER` | User | Ban mot nguoi dung |
| `UNBAN_USER` | User | Go ban mot nguoi dung |
| `CREATE_SONG` | Song | Tao bai hat moi |
| `UPDATE_SONG` | Song | Cap nhat thong tin bai hat |
| `DELETE_SONG` | Song | Xoa bai hat |
| `ADD_SONG_TAGS` | Song/Tag | Them tag vao bai hat |
| `REMOVE_SONG_TAGS` | Song/Tag | Xoa tag khoi bai hat |
| `REPLACE_SONG_TAGS` | Song/Tag | Thay the toan bo tag cua bai hat |
| `CREATE_ARTIST` | Artist | Tao nghe si moi |
| `UPDATE_ARTIST` | Artist | Cap nhat thong tin nghe si |
| `DELETE_ARTIST` | Artist | Xoa nghe si |
| `CREATE_ALBUM` | Album | Tao album moi |
| `UPDATE_ALBUM` | Album | Cap nhat thong tin album |
| `DELETE_ALBUM` | Album | Xoa album |
| `CREATE_SYSTEM_PLAYLIST` | System Playlist | Tao playlist he thong moi |
| `UPDATE_SYSTEM_PLAYLIST` | System Playlist | Cap nhat playlist he thong |
| `DELETE_SYSTEM_PLAYLIST` | System Playlist | Xoa playlist he thong |
| `ADD_SONG_TO_SYSTEM_PLAYLIST` | System Playlist | Them bai hat vao playlist he thong |
| `REMOVE_SONG_FROM_SYSTEM_PLAYLIST` | System Playlist | Xoa bai hat khoi playlist he thong |
| `REORDER_SYSTEM_PLAYLIST_SONGS` | System Playlist | Sap xep lai thu tu bai hat trong playlist he thong |
