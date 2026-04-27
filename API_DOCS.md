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
- [Home](#home)
- [Play History](#play-history)
  - [POST `/api/play-history`](#post-api-play-history)
  - [GET `/api/play-history`](#get-api-play-history)
- [Playlists](#playlists)
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
    "items": [ ... ],
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

```json
{
  "displayName": "Nguyen Van B"
}
```

**Response `200 OK`:** Trả về `UserProfileResponse` đã cập nhật.

---

### PATCH `/api/profile/avatar`

Cập nhật ảnh đại diện.

**Auth:** ✅ Yêu cầu (JWT)

**Content-Type:** `multipart/form-data`

**Parts:**
| Part | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `avatar` | File | ✅ | File ảnh đại diện mới |

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
    "artists": [
      { "id": "uuid", "name": "Sơn Tùng M-TP", "avatarUrl": "https://..." }
    ],
    "genres": [
      { "id": 1, "name": "V-Pop" }
    ]
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

**`UpdateSongRequest` fields:** `title`, `albumId`, `trackNumber`, `releaseDate`, `artistIds`, `genreIds`, `active` — tất cả đều optional.

**Response `200 OK`:** Trả về `SongResponse` đã cập nhật.

---

### GET `/api/songs/{id}`

Lấy chi tiết bài hát theo ID.

**Auth:** ✅ Yêu cầu (JWT)

**Response `200 OK`:** Trả về `SongResponse`.

---

### GET `/api/songs`

Lấy danh sách bài hát có phân trang, hỗ trợ lọc theo nhiều tiêu chí. Các filter loại trừ nhau theo thứ tự ưu tiên: `query` → `artistId` → `albumId` → `genreId` → tất cả.

**Auth:** ✅ Yêu cầu (JWT)

**Query Params:**
| Param | Type | Bắt buộc | Mặc định | Mô tả |
|---|---|---|---|---|
| `query` | string | ❌ | — | Tìm kiếm theo tên bài hát |
| `mode` | string | ❌ | `contains` | Chế độ tìm kiếm: `contains`, `fulltext` |
| `artistId` | UUID | ❌ | — | Lọc theo nghệ sĩ |
| `albumId` | UUID | ❌ | — | Lọc theo album |
| `genreId` | Long | ❌ | — | Lọc theo thể loại |
| `page` | integer | ❌ | `0` | Trang (0-based) |
| `size` | integer | ❌ | `20` | Số phần tử mỗi trang |

**Ví dụ:**
```
GET /api/songs?page=0&size=20                          → tất cả (paginated)
GET /api/songs?query=noi+nay&mode=contains&page=0&size=20 → tìm theo tên
GET /api/songs?artistId=<uuid>&page=0&size=20          → theo nghệ sĩ
GET /api/songs?albumId=<uuid>&page=0&size=20           → theo album
GET /api/songs?genreId=1&page=0&size=20                → theo thể loại
```

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

### GET `/api/songs/{id}/stream`

Stream audio bài hát. Chỉ trả về dữ liệu audio, **không** tự động ghi lịch sử hay tăng play count. Client cần gọi riêng `POST /api/play-history` khi bắt đầu phát.

**Auth:** ✅ Yêu cầu (JWT)

**Path Params:**
| Param | Type | Mô tả |
|---|---|---|
| `id` | UUID | ID bài hát |

**Request Headers:**
| Header | Bắt buộc | Mô tả |
|---|---|---|
| `Range` | ❌ | Byte range, ví dụ: `bytes=0-65535` |

**Response `200 OK`:** Toàn bộ file audio (không có `Range` header).

**Response `206 Partial Content`:** Đoạn audio theo Range request (có `Content-Range` header).

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

Lấy danh sách album có phân trang, hỗ trợ tìm kiếm theo tên.

**Auth:** ✅ Yêu cầu (JWT)

**Query Params:**
| Param | Type | Bắt buộc | Mặc định | Mô tả |
|---|---|---|---|---|
| `query` | string | ❌ | — | Tìm kiếm theo tên album |
| `mode` | string | ❌ | `contains` | Chế độ tìm kiếm: `contains`, `fulltext` |
| `page` | integer | ❌ | `0` | Trang (0-based) |
| `size` | integer | ❌ | `20` | Số phần tử mỗi trang |

**Ví dụ:**
```
GET /api/albums?page=0&size=20              → tất cả (paginated)
GET /api/albums?query=tam+9&page=0&size=20  → tìm theo tên
```

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

Lấy danh sách nghệ sĩ có phân trang, hỗ trợ tìm kiếm theo tên.

**Auth:** ✅ Yêu cầu (JWT)

**Query Params:**
| Param | Type | Bắt buộc | Mặc định | Mô tả |
|---|---|---|---|---|
| `query` | string | ❌ | — | Tìm kiếm theo tên nghệ sĩ |
| `mode` | string | ❌ | `contains` | Chế độ tìm kiếm: `contains`, `fulltext` |
| `page` | integer | ❌ | `0` | Trang (0-based) |
| `size` | integer | ❌ | `20` | Số phần tử mỗi trang |

**Ví dụ:**
```
GET /api/artists?page=0&size=20               → tất cả (paginated)
GET /api/artists?query=son+tung&page=0&size=20 → tìm theo tên
```

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

## Playlists

> Mỗi user quản lý playlist của chính mình. Playlist có thể là **public** (ai cũng xem được) hoặc **private** (chỉ chủ sở hữu).

### POST `/api/playlists`

Tạo playlist mới.

**Auth:** ✅ Yêu cầu (JWT)

**Content-Type:** `multipart/form-data`

**Parts:**
| Part | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `data` | JSON (`CreatePlaylistRequest`) | ✅ | Thông tin playlist |
| `cover` | File | ❌ | Ảnh bìa |

**`CreatePlaylistRequest`:**
| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `name` | string | ✅ | Tên playlist |
| `description` | string | ❌ | Mô tả |
| `isPublic` | boolean | ❌ | Công khai hay không (mặc định `false`) |

```json
{
  "name": "Nhạc hay 2026",
  "description": "Tuyển tập bài hát yêu thích",
  "isPublic": true
}
```

**Response `201 Created`:**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": "uuid",
    "userId": "uuid",
    "name": "Nhạc hay 2026",
    "description": "Tuyển tập bài hát yêu thích",
    "coverUrl": null,
    "isPublic": true,
    "totalSongs": 0,
    "createdAt": "2026-04-27T10:00:00",
    "updatedAt": "2026-04-27T10:00:00",
    "songs": []
  }
}
```

---

### PUT `/api/playlists/{id}`

Cập nhật playlist. Chỉ chủ sở hữu mới được cập nhật.

**Auth:** ✅ Yêu cầu (JWT)

**Content-Type:** `multipart/form-data`

**Path Params:**
| Param | Type | Mô tả |
|---|---|---|
| `id` | UUID | ID playlist |

**Parts:**
| Part | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `data` | JSON (`UpdatePlaylistRequest`) | ✅ | Partial update |
| `cover` | File | ❌ | Ảnh bìa mới |

**`UpdatePlaylistRequest` fields:** `name`, `description`, `isPublic` — tất cả đều optional.

**Response `200 OK`:** Trả về `PlaylistResponse` đã cập nhật.

---

### GET `/api/playlists/{id}`

Lấy chi tiết playlist kèm danh sách bài hát.

**Auth:** ✅ Yêu cầu (JWT) — playlist private chỉ chủ sở hữu mới xem được.

**Response `200 OK`:** Trả về `PlaylistResponse`.

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": "uuid",
    "userId": "uuid",
    "name": "Nhạc hay 2026",
    "description": "...",
    "coverUrl": "https://...",
    "isPublic": true,
    "totalSongs": 2,
    "createdAt": "2026-04-27T10:00:00",
    "updatedAt": "2026-04-27T10:00:00",
    "songs": [
      {
        "position": 1,
        "addedAt": "2026-04-27T10:05:00",
        "song": {
          "id": "uuid",
          "title": "Nơi Này Có Anh",
          "coverUrl": "https://...",
          "durationSeconds": 210,
          "audioUrl": "https://..."
        }
      }
    ]
  }
}
```

---

### GET `/api/playlists`

Lấy danh sách playlist có phân trang.

**Auth:** ✅ Yêu cầu (JWT)

**Query Params:**
| Param | Type | Bắt buộc | Mặc định | Mô tả |
|---|---|---|---|---|
| `query` | string | ❌ | — | Tìm kiếm theo tên playlist |
| `owner` | boolean | ❌ | `false` | `true` = chỉ lấy playlist của user hiện tại |
| `isPublic` | boolean | ❌ | — | Lọc theo trạng thái công khai |
| `page` | integer | ❌ | `0` | Trang (0-based) |
| `size` | integer | ❌ | `20` | Số phần tử mỗi trang |

**Ví dụ:**
```
GET /api/playlists?owner=true&page=0&size=20          → playlist của tôi
GET /api/playlists?isPublic=true&page=0&size=20       → tất cả playlist public
GET /api/playlists?query=nhac+hay&page=0&size=20      → tìm theo tên
```

**Response `200 OK`:** Trả về `PageResultDto<PlaylistResponse>`.

---

### DELETE `/api/playlists/{id}`

Xóa playlist. Chỉ chủ sở hữu mới được xóa.

**Auth:** ✅ Yêu cầu (JWT)

**Response `200 OK`:**
```json
{ "success": true, "message": "OK", "data": null }
```

---

### POST `/api/playlists/{id}/songs`

Thêm bài hát vào playlist.

**Auth:** ✅ Yêu cầu (JWT)

**Path Params:**
| Param | Type | Mô tả |
|---|---|---|
| `id` | UUID | ID playlist |

**Request Body:**
| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `songId` | UUID | ✅ | ID bài hát muốn thêm |

```json
{
  "songId": "uuid"
}
```

**Response `200 OK`:** Trả về `PlaylistResponse` đã cập nhật.

---

### DELETE `/api/playlists/{id}/songs/{songId}`

Xóa bài hát khỏi playlist.

**Auth:** ✅ Yêu cầu (JWT)

**Path Params:**
| Param | Type | Mô tả |
|---|---|---|
| `id` | UUID | ID playlist |
| `songId` | UUID | ID bài hát muốn xóa |

**Response `200 OK`:** Trả về `PlaylistResponse` đã cập nhật.

---

### PUT `/api/playlists/{id}/songs/reorder`

Sắp xếp lại thứ tự bài hát trong playlist. Danh sách `songIds` xác định thứ tự mới.

**Auth:** ✅ Yêu cầu (JWT)

**Request Body:**
| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `songIds` | UUID[] | ✅ | Danh sách ID bài hát theo thứ tự mới |

```json
{
  "songIds": ["uuid-b", "uuid-a", "uuid-c"]
}
```

**Response `200 OK`:** Trả về `PlaylistResponse` với thứ tự đã cập nhật.

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
  "message": "title: Title is required, artistIds: Artist IDs are required",
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
