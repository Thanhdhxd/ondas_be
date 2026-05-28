# Ondas Backend — API Documentation

> **Base URL:** `http://localhost:8080`
> **Content-Type:** `application/json` (trừ các endpoint upload file dùng `multipart/form-data`)
> **Authentication:** `Authorization: Bearer <accessToken>`

---

## Mục lục

- [Response Format](#response-format)
- [Authentication](#authentication)
- [Profile](#profile)
- [Songs](#songs)
- [Lyrics](#lyrics)
- [Albums](#albums)
- [Artists](#artists)
- [Genres](#genres)
- [Home](#home)
- [Search](#search)
  - [GET `/api/search/suggestions`](#get-apisearchsuggestions)
  - [DELETE `/api/search/history`](#delete-apisearchhistory)
  - [GET `/api/search`](#get-apisearch)
- [Play History](#play-history)
  - [POST `/api/play-history`](#post-api-play-history)
  - [GET `/api/play-history`](#get-api-play-history)
- [Playlists](#playlists)
- [Favorites](#favorites)
- [Tags](#tagmood-api-standalone)
  - [Tags CRUD](#1-tag-crud-system-tags)
  - [Song Tags](#2-song-tags-attachdetach-existing-tags)
- [System Playlists](#system-playlists)
  - [Public Endpoints](#get-apisystem-playlists)
  - [Admin Endpoints](#admin-system-playlists)
- [Admin — Quản lý User](#admin--quản-lý-user)
- [E2E](#e2e-profile-e2e)
- [Phân quyền](#phân-quyền)
- [Mã lỗi thường gặp](#mã-lỗi-thường-gặp)

---

## Response Format

Mọi API đều trả về cấu trúc `ApiResponse<T>`:

**Lưu ý:** `message` là **code** (xem [error_codes.md](error_codes.md)).

```json
{
  "success": true,
  "message": "success.ok",
  "data": { ... }
}
```

**Lỗi:**
```json
{
  "success": false,
  "message": "error.not_found.song",
  "data": null
}
```

**Phân trang** (`PageResultDto<T>`):
```json
{
  "success": true,
  "message": "success.ok",
  "data": {
    "items": [ ... ],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5
  }
}
```

---

## Phân quyền

| Role | Mô tả |
|---|---|
| `USER` | Nghe nhạc, xem profile, đổi mật khẩu, quản lý yêu thích |
| `CONTENT_MANAGER` | CRUD bài hát, album, nghệ sĩ, thể loại |
| `ADMIN` | Tất cả quyền của `CONTENT_MANAGER` + quản lý user |

**Endpoint công khai (không cần auth):**
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `DELETE /api/auth/logout`
- `POST /api/auth/forgot-password`
- `POST /api/auth/reset-password`

**Endpoint công khai (có JWT):**
- `GET /api/songs/{id}`
- `GET /api/songs`, `GET /api/albums`, `GET /api/artists`, `GET /api/genres`
- `GET /api/songs/{id}/stream`
- `GET /api/songs/{id}/lyrics`, `GET /api/songs/{id}/tags`
- `GET /api/tags`, `GET /api/tags/search`, `GET /api/tags/{id}`
- `GET /api/system-playlists`, `GET /api/system-playlists/{id}`

**Endpoint yêu cầu ADMIN hoặc CONTENT_MANAGER:**
- `POST`, `PUT`, `DELETE` trên `/api/songs`, `/api/albums`, `/api/artists`, `/api/genres`
- `POST`, `PATCH`, `DELETE` trên `/api/songs/{id}/lyrics`
- `POST`, `PUT`, `DELETE` trên `/api/songs/{id}/tags`
- `POST`, `PUT`, `DELETE` trên `/api/tags`
- Tất cả `/api/admin/system-playlists/**`

**Endpoint yêu cầu ADMIN:**
- Tất cả `/api/admin/users/**`
- Tất cả `/api/admin/e2e/**`

---

## Mã lỗi thường gặp

Danh sách code chi tiết: [error_codes.md](error_codes.md)

| HTTP Status | Ý nghĩa |
|---|---|
| `400 Bad Request` | Dữ liệu request không hợp lệ (validation error) |
| `401 Unauthorized` | Chưa đăng nhập hoặc token hết hạn |
| `403 Forbidden` | Không đủ quyền truy cập |
| `404 Not Found` | Không tìm thấy tài nguyên |
| `409 Conflict` | Dữ liệu đã tồn tại (duplicate) |
| `500 Internal Server Error` | Lỗi server |

**Ví dụ lỗi validation (`400`):**
```json
{
  "success": false,
  "message": "title: validation.not_blank, artistIds: validation.not_empty",
  "data": null
}
```

**Ví dụ lỗi xác thực (`401`):**
```json
{
  "success": false,
  "message": "error.unauthorized.invalid_credentials",
  "data": null
}
```
