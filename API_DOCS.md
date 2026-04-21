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
- [Albums](#albums)
- [Artists](#artists)
- [Genres](#genres)
- [Phân quyền](#phân-quyền)
- [Mã lỗi thường gặp](#mã-lỗi-thường-gặp)

---

## Response Format

Mọi API đều trả về cấu trúc `ApiResponse<T>`:

```json
{
  "success": true,
  "message": "OK",
  "data": { ... }
}
```

**Lỗi:**
```json
{
  "success": false,
  "message": "Song not found with id: ...",
  "data": null
}
```

**Phân trang** (`PageResultDto<T>`):
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "content": [ ... ],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5
  }
}
```

---

## Authentication

### POST `/api/auth/register`

Đăng ký tài khoản mới.

**Auth:** Không yêu cầu

**Request Body:**
| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `email` | string | ✅ | Email hợp lệ, tối đa 255 ký tự |
| `password` | string | ✅ | Tối thiểu 8 ký tự |
| `displayName` | string | ✅ | Tối đa 100 ký tự |

```json
{
  "email": "user@example.com",
  "password": "password123",
  "displayName": "Nguyen Van A"
}
```

**Response `201 Created`:**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "accessToken": "eyJhbGciOi...",
    "refreshToken": "dGhpcyBpcyBh...",
    "user": {
      "id": "uuid",
      "email": "user@example.com",
      "displayName": "Nguyen Van A",
      "role": "USER"
    }
  }
}
```

---

### POST `/api/auth/login`

Đăng nhập.

**Auth:** Không yêu cầu

**Request Body:**
| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `email` | string | ✅ | Email hợp lệ |
| `password` | string | ✅ | |

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "accessToken": "eyJhbGciOi...",
    "refreshToken": "dGhpcyBpcyBh...",
    "user": {
      "id": "uuid",
      "email": "user@example.com",
      "displayName": "Nguyen Van A",
      "role": "USER"
    }
  }
}
```

---

### POST `/api/auth/refresh`

Lấy access token mới bằng refresh token.

**Auth:** Không yêu cầu

**Request Body:**
| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `refreshToken` | string | ✅ | Refresh token hiện tại |

```json
{
  "refreshToken": "dGhpcyBpcyBh..."
}
```

**Response `200 OK`:** Tương tự login.

---

### DELETE `/api/auth/logout`

Đăng xuất, thu hồi refresh token.

**Auth:** Không yêu cầu (gửi kèm refreshToken trong body)

**Request Body:**
| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `refreshToken` | string | ✅ | |

```json
{
  "refreshToken": "dGhpcyBpcyBh..."
}
```

**Response `200 OK`:**
```json
{ "success": true, "message": "OK", "data": null }
```

---

### POST `/api/auth/forgot-password`

Gửi OTP về email để đặt lại mật khẩu.

**Auth:** Không yêu cầu

**Request Body:**
| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `email` | string | ✅ | Email đã đăng ký |

```json
{
  "email": "user@example.com"
}
```

**Response `200 OK`:**
```json
{ "success": true, "message": "OK", "data": null }
```

> **Lưu ý:** Luôn trả về thành công để tránh tiết lộ email tồn tại trong hệ thống.

---

### POST `/api/auth/reset-password`

Đặt lại mật khẩu bằng OTP.

**Auth:** Không yêu cầu

**Request Body:**
| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `email` | string | ✅ | Email đăng ký |
| `otp` | string | ✅ | 6 chữ số |
| `newPassword` | string | ✅ | Tối thiểu 8 ký tự |

```json
{
  "email": "user@example.com",
  "otp": "123456",
  "newPassword": "newpassword123"
}
```

**Response `200 OK`:**
```json
{ "success": true, "message": "OK", "data": null }
```

---

## Profile

### GET `/api/profile`

Lấy thông tin profile của user đang đăng nhập.

**Auth:** ✅ Yêu cầu (JWT)

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": "uuid",
    "email": "user@example.com",
    "displayName": "Nguyen Van A",
    "avatarUrl": "https://...",
    "role": "USER",
    "lastLoginAt": "2026-04-21T10:00:00",
    "createdAt": "2026-01-01T00:00:00"
  }
}
```

---

### PUT `/api/profile`

Cập nhật thông tin profile.

**Auth:** ✅ Yêu cầu (JWT)

**Request Body:**
| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `displayName` | string | ✅ | 1–50 ký tự |
| `avatarUrl` | string | ❌ | URL ảnh đại diện, `null` giữ nguyên |

```json
{
  "displayName": "Nguyen Van B",
  "avatarUrl": "https://example.com/avatar.jpg"
}
```

**Response `200 OK`:** Trả về `UserProfileResponse` đã cập nhật.

---

### PUT `/api/profile/password`

Đổi mật khẩu.

**Auth:** ✅ Yêu cầu (JWT)

**Request Body:**
| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `currentPassword` | string | ✅ | Mật khẩu hiện tại |
| `newPassword` | string | ✅ | Mật khẩu mới, tối thiểu 8 ký tự |

```json
{
  "currentPassword": "oldpassword",
  "newPassword": "newpassword123"
}
```

**Response `200 OK`:**
```json
{ "success": true, "message": "OK", "data": null }
```

---

## Songs

### POST `/api/songs`

Tạo bài hát mới (kèm upload file).

**Auth:** ✅ `ADMIN` hoặc `CONTENT_MANAGER`

**Content-Type:** `multipart/form-data`

**Parts:**
| Part | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `data` | JSON (`CreateSongRequest`) | ✅ | Metadata bài hát |
| `audio` | File | ✅ | File audio |
| `cover` | File | ❌ | Ảnh bìa |

**`CreateSongRequest` (JSON part `data`):**
| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `title` | string | ✅ | Tên bài hát |
| `durationSeconds` | integer | ✅ | Thời lượng (giây), > 0 |
| `albumId` | UUID | ❌ | ID album (nếu là single thì bỏ qua) |
| `trackNumber` | integer | ❌ | Số thứ tự trong album |
| `releaseDate` | date (`yyyy-MM-dd`) | ❌ | Ngày phát hành |
| `artistIds` | UUID[] | ✅ | Danh sách ID nghệ sĩ |
| `genreIds` | Long[] | ✅ | Danh sách ID thể loại |

**Response `201 Created`:**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": "uuid",
    "title": "Nơi Này Có Anh",
    "slug": "noi-nay-co-anh",
    "durationSeconds": 210,
    "audioUrl": "https://...",
    "audioFormat": "mp3",
    "audioSizeBytes": 5242880,
    "coverUrl": "https://...",
    "albumId": "uuid",
    "trackNumber": 1,
    "releaseDate": "2026-01-01",
    "playCount": 0,
    "active": true,
    "artistIds": ["uuid"],
    "genreIds": [1]
  }
}
```

---

### PUT `/api/songs/{id}`

Cập nhật bài hát.

**Auth:** ✅ `ADMIN` hoặc `CONTENT_MANAGER`

**Content-Type:** `multipart/form-data`

**Path Params:**
| Param | Type | Mô tả |
|---|---|---|
| `id` | UUID | ID bài hát |

**Parts:**
| Part | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `data` | JSON (`UpdateSongRequest`) | ✅ | Các field muốn cập nhật (partial) |
| `audio` | File | ❌ | File audio mới |
| `cover` | File | ❌ | Ảnh bìa mới |

**`UpdateSongRequest` fields:** `title`, `durationSeconds`, `albumId`, `trackNumber`, `releaseDate`, `artistIds`, `genreIds`, `active` — tất cả đều optional.

**Response `200 OK`:** Trả về `SongResponse` đã cập nhật.

---

### GET `/api/songs/{id}`

Lấy chi tiết bài hát theo ID.

**Auth:** ✅ Yêu cầu (JWT)

**Response `200 OK`:** Trả về `SongResponse`.

---

### GET `/api/songs`

Lấy tất cả bài hát.

**Auth:** ✅ Yêu cầu (JWT)

**Response `200 OK`:** Trả về `List<SongResponse>`.

---

### GET `/api/songs/by-artist/{artistId}`

Lấy bài hát theo nghệ sĩ (có phân trang).

**Auth:** ✅ Yêu cầu (JWT)

**Path Params:**
| Param | Type | Mô tả |
|---|---|---|
| `artistId` | UUID | ID nghệ sĩ |

**Query Params:**
| Param | Type | Mặc định | Mô tả |
|---|---|---|---|
| `page` | integer | `0` | Trang (0-based) |
| `size` | integer | `20` | Số phần tử mỗi trang |

**Response `200 OK`:** Trả về `PageResultDto<SongResponse>`.

---

### GET `/api/songs/by-album/{albumId}`

Lấy bài hát theo album (có phân trang).

**Auth:** ✅ Yêu cầu (JWT)

**Path Params:**
| Param | Type | Mô tả |
|---|---|---|
| `albumId` | UUID | ID album |

**Query Params:** `page`, `size` (tương tự trên)

---

### GET `/api/songs/by-genre/{genreId}`

Lấy bài hát theo thể loại (có phân trang).

**Auth:** ✅ Yêu cầu (JWT)

**Path Params:**
| Param | Type | Mô tả |
|---|---|---|
| `genreId` | Long | ID thể loại |

**Query Params:** `page`, `size` (tương tự trên)

---

### GET `/api/songs/search`

Tìm kiếm bài hát theo tên.

**Auth:** ✅ Yêu cầu (JWT)

**Query Params:**
| Param | Type | Bắt buộc | Mặc định | Mô tả |
|---|---|---|---|---|
| `query` | string | ✅ | — | Từ khóa tìm kiếm |
| `mode` | string | ❌ | `contains` | Chế độ: `contains`, `startsWith`, `exact` |
| `page` | integer | ❌ | `0` | |
| `size` | integer | ❌ | `20` | |

**Response `200 OK`:** Trả về `PageResultDto<SongResponse>`.

---

### DELETE `/api/songs/{id}`

Xóa bài hát.

**Auth:** ✅ `ADMIN` hoặc `CONTENT_MANAGER`

**Response `200 OK`:**
```json
{ "success": true, "message": "OK", "data": null }
```

---

## Albums

### POST `/api/albums`

Tạo album mới.

**Auth:** ✅ `ADMIN` hoặc `CONTENT_MANAGER`

**Content-Type:** `multipart/form-data`

**Parts:**
| Part | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `data` | JSON (`CreateAlbumRequest`) | ✅ | Metadata album |
| `cover` | File | ❌ | Ảnh bìa album |

**`CreateAlbumRequest`:**
| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `title` | string | ✅ | Tên album |
| `slug` | string | ❌ | Slug URL |
| `releaseDate` | date (`yyyy-MM-dd`) | ❌ | Ngày phát hành |
| `albumType` | string | ❌ | Loại album (vd: `ALBUM`, `EP`, `SINGLE`) |
| `description` | string | ❌ | Mô tả album |
| `artistIds` | UUID[] | ✅ | Danh sách ID nghệ sĩ |

**Response `201 Created`:**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": "uuid",
    "title": "Tâm 9",
    "slug": "tam-9",
    "coverUrl": "https://...",
    "releaseDate": "2020-01-01",
    "albumType": "ALBUM",
    "description": "...",
    "totalTracks": 0,
    "artistIds": ["uuid"],
    "tracklist": []
  }
}
```

---

### PUT `/api/albums/{id}`

Cập nhật album.

**Auth:** ✅ `ADMIN` hoặc `CONTENT_MANAGER`

**Content-Type:** `multipart/form-data`

**Parts:**
| Part | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `data` | JSON (`UpdateAlbumRequest`) | ✅ | Partial update |
| `cover` | File | ❌ | Ảnh bìa mới |

**`UpdateAlbumRequest` fields:** `title`, `slug`, `releaseDate`, `albumType`, `description`, `artistIds` — tất cả đều optional.

**Response `200 OK`:** Trả về `AlbumResponse` đã cập nhật.

---

### GET `/api/albums/{id}`

Lấy chi tiết album.

**Auth:** ✅ Yêu cầu (JWT)

**Response `200 OK`:** Trả về `AlbumResponse` (bao gồm `tracklist`).

---

### GET `/api/albums`

Lấy tất cả album.

**Auth:** ✅ Yêu cầu (JWT)

**Response `200 OK`:** Trả về `List<AlbumResponse>`.

---

### GET `/api/albums/search`

Tìm kiếm album theo tên.

**Auth:** ✅ Yêu cầu (JWT)

**Query Params:**
| Param | Type | Bắt buộc | Mặc định | Mô tả |
|---|---|---|---|---|
| `query` | string | ✅ | — | Từ khóa |
| `mode` | string | ❌ | `contains` | `contains`, `startsWith`, `exact` |
| `page` | integer | ❌ | `0` | |
| `size` | integer | ❌ | `20` | |

**Response `200 OK`:** Trả về `PageResultDto<AlbumResponse>`.

---

### DELETE `/api/albums/{id}`

Xóa album.

**Auth:** ✅ `ADMIN` hoặc `CONTENT_MANAGER`

**Response `200 OK`:**
```json
{ "success": true, "message": "OK", "data": null }
```

---

## Artists

### POST `/api/artists`

Tạo nghệ sĩ mới.

**Auth:** ✅ `ADMIN` hoặc `CONTENT_MANAGER`

**Content-Type:** `multipart/form-data`

**Parts:**
| Part | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `data` | JSON (`CreateArtistRequest`) | ✅ | Thông tin nghệ sĩ |
| `avatar` | File | ❌ | Ảnh đại diện |

**`CreateArtistRequest`:**
| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `name` | string | ✅ | Tên nghệ sĩ |
| `slug` | string | ❌ | Slug URL |
| `bio` | string | ❌ | Tiểu sử |
| `country` | string | ❌ | Quốc gia |

**Response `201 Created`:**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": "uuid",
    "name": "Sơn Tùng M-TP",
    "slug": "son-tung-m-tp",
    "bio": "...",
    "avatarUrl": "https://...",
    "country": "Vietnam"
  }
}
```

---

### PUT `/api/artists/{id}`

Cập nhật nghệ sĩ.

**Auth:** ✅ `ADMIN` hoặc `CONTENT_MANAGER`

**Content-Type:** `multipart/form-data`

**Parts:**
| Part | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `data` | JSON (`UpdateArtistRequest`) | ✅ | Partial update |
| `avatar` | File | ❌ | Ảnh mới |

**`UpdateArtistRequest` fields:** `name`, `slug`, `bio`, `country` — tất cả optional.

**Response `200 OK`:** Trả về `ArtistResponse` đã cập nhật.

---

### GET `/api/artists/{id}`

Lấy chi tiết nghệ sĩ.

**Auth:** ✅ Yêu cầu (JWT)

**Response `200 OK`:** Trả về `ArtistResponse`.

---

### GET `/api/artists`

Lấy tất cả nghệ sĩ.

**Auth:** ✅ Yêu cầu (JWT)

**Response `200 OK`:** Trả về `List<ArtistResponse>`.

---

### GET `/api/artists/search`

Tìm kiếm nghệ sĩ theo tên.

**Auth:** ✅ Yêu cầu (JWT)

**Query Params:**
| Param | Type | Bắt buộc | Mặc định | Mô tả |
|---|---|---|---|---|
| `query` | string | ✅ | — | Từ khóa |
| `mode` | string | ❌ | `contains` | `contains`, `startsWith`, `exact` |
| `page` | integer | ❌ | `0` | |
| `size` | integer | ❌ | `20` | |

**Response `200 OK`:** Trả về `PageResultDto<ArtistResponse>`.

---

### DELETE `/api/artists/{id}`

Xóa nghệ sĩ.

**Auth:** ✅ `ADMIN` hoặc `CONTENT_MANAGER`

**Response `200 OK`:**
```json
{ "success": true, "message": "OK", "data": null }
```

---

## Genres

### POST `/api/genres`

Tạo thể loại mới.

**Auth:** ✅ `ADMIN` hoặc `CONTENT_MANAGER`

**Content-Type:** `multipart/form-data`

**Parts:**
| Part | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `data` | JSON (`CreateGenreRequest`) | ✅ | Thông tin thể loại |
| `cover` | File | ❌ | Ảnh bìa |

**`CreateGenreRequest`:**
| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `name` | string | ✅ | Tên thể loại |
| `slug` | string | ❌ | Slug URL |
| `description` | string | ❌ | Mô tả |
| `coverUrl` | string | ❌ | URL ảnh bìa (nếu không upload file) |

**Response `201 Created`:**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": 1,
    "name": "V-Pop",
    "slug": "v-pop",
    "description": "...",
    "coverUrl": "https://..."
  }
}
```

---

### PUT `/api/genres/{id}`

Cập nhật thể loại.

**Auth:** ✅ `ADMIN` hoặc `CONTENT_MANAGER`

**Content-Type:** `multipart/form-data`

**Parts:**
| Part | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `data` | JSON (`UpdateGenreRequest`) | ✅ | Partial update |
| `cover` | File | ❌ | Ảnh mới |

**`UpdateGenreRequest` fields:** `name`, `slug`, `description`, `coverUrl` — tất cả optional.

**Response `200 OK`:** Trả về `GenreResponse` đã cập nhật.

---

### GET `/api/genres/{id}`

Lấy chi tiết thể loại.

**Auth:** ✅ Yêu cầu (JWT)

**Response `200 OK`:** Trả về `GenreResponse`.

---

### GET `/api/genres`

Lấy tất cả thể loại.

**Auth:** ✅ Yêu cầu (JWT)

**Response `200 OK`:** Trả về `List<GenreResponse>`.

---

### GET `/api/genres/search`

Tìm kiếm thể loại theo tên.

**Auth:** ✅ Yêu cầu (JWT)

**Query Params:**
| Param | Type | Bắt buộc | Mặc định | Mô tả |
|---|---|---|---|---|
| `query` | string | ✅ | — | Từ khóa |
| `mode` | string | ❌ | `contains` | `contains`, `startsWith`, `exact` |
| `page` | integer | ❌ | `0` | |
| `size` | integer | ❌ | `20` | |

**Response `200 OK`:** Trả về `PageResultDto<GenreResponse>`.

---

### DELETE `/api/genres/{id}`

Xóa thể loại.

**Auth:** ✅ `ADMIN` hoặc `CONTENT_MANAGER`

**Response `200 OK`:**
```json
{ "success": true, "message": "OK", "data": null }
```

---

## Phân quyền

| Role | Mô tả |
|---|---|
| `USER` | Nghe nhạc, xem profile, đổi mật khẩu |
| `CONTENT_MANAGER` | CRUD bài hát, album, nghệ sĩ, thể loại |
| `ADMIN` | Tất cả quyền của `CONTENT_MANAGER` + quản lý user |

**Endpoint công khai (không cần auth):**
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `DELETE /api/auth/logout`
- `POST /api/auth/forgot-password`
- `POST /api/auth/reset-password`

**Endpoint yêu cầu ADMIN hoặc CONTENT_MANAGER:**
- `POST`, `PUT`, `DELETE` trên `/api/songs`, `/api/albums`, `/api/artists`, `/api/genres`

---

## Mã lỗi thường gặp

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
  "message": "title: Title is required, durationSeconds: Duration must be positive",
  "data": null
}
```

**Ví dụ lỗi xác thực (`401`):**
```json
{
  "success": false,
  "message": "Invalid credentials",
  "data": null
}
```
