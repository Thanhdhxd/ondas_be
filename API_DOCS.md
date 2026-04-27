♫...Xuân Bắc
xuanbac3112
Vô hình
♫...Xuân Bắc [SX],  — 30/03/2026 10:25 CH


Đinh Triệu — 30/03/2026 10:51 CH
https://docs.google.com/forms/d/e/1FAIpQLSfXBem-pa0elVqtm3yFdsXg-OO7_J7DOVcpY8lCkXtd1Br08g/viewform?usp=dialog
Google Docs
LỜI CHÀO & GIỚI THIỆU
🎵 KHẢO SÁT VỀ ỨNG DỤNG NGHE NHẠC 🎵

Xin chào bạn!

Mình là sinh viên ngành Khoa học Máy tính tại Đại học Xây Dựng. Hiện tại mình đang thực hiện đồ án "Phát triển ứng dụng đa nền tảng nâng cao" với đề tài xây dựng một ứng dụng nghe nhạc trên nền tảng Flutter. ...
♫...Xuân Bắc [SX],  — 30/03/2026 10:59 CH

Đinh Triệu — 31/03/2026 3:33 CH
User App (Flutter)
Đăng ký/đăng nhập bằng Email + Password
Phát nhạc cơ bản: Play/Pause, Next/Previous, Seek
Danh sách bài hát từ server
Tìm kiếm bài hát, nghệ sĩ, album
Phân loại theo nghệ sĩ, album, thể loại
Yêu thích bài hát
Playlist cá nhân cơ bản: tạo, thêm bài, xóa bài
Hiển thị metadata: ảnh bìa, tên bài, nghệ sĩ, thời lượng
Profile cơ bản: xem/sửa thông tin

Admin Web (Flutter Web)
Đăng nhập admin
CRUD bài hát (upload audio + metadata)
CRUD nghệ sĩ
CRUD album + tracklist đơn giản
CRUD thể loại
Quản lý user cơ bản: xem danh sách, khóa/mở khóa
Dashboard đơn giản: tổng user, tổng bài hát, tổng lượt phát

Backend (Spring Boot)
Auth JWT (user/admin)
API cho songs/artists/albums/genres/playlists/favorites/users
Upload file audio + ảnh bìa
Tracking lượt phát cơ bản
Phân quyền cơ bản: Admin và User
Đinh Triệu — 31/03/2026 3:52 CH
# THIẾT KẾ CƠ SỞ DỮ LIỆU — ONDAS MUSIC STREAMING

> **Stack**: PostgreSQL (relational core) + Redis (cache/session) + Elasticsearch (search)  
> **Nguyên tắc**: mỗi bảng có `created_at`, `updated_at`; soft-delete qua `deleted_at` (nullable).

---

database_design.md
17 KB
Đinh Triệu — 01/04/2026 3:29 CH
USER APP
Đăng ký (Register)
Đăng nhập (Login)
Phát nhạc (Play Music)
Tìm kiếm (Search)
Yêu thích bài hát (Favorite)
Tạo & Quản lý Playlist
Xem & Sửa Profile
ADMIN WEB
Đăng nhập Admin
CRUD Bài hát (Admin)
CRUD Nghệ sĩ / Album / Thể loại (Admin)
(Luồng tương tự CRUD Bài hát, chỉ khác endpoint và fields)

Quản lý User (Admin)
Dashboard (Admin)
BACKEND (Cross-cutting)
JWT Authentication Flow
Upload File Flow
Thành — 01/04/2026 4:45 CH

Đinh Triệu — 02/04/2026 4:07 CH
# Copilot Chat Conversation Export: Task management system project guide

**User:** @nguyenhuutung11
**Thread URL:** https://github.com/copilot/c/f7b55461-ccd0-4782-8e6d-44bc33076ada

---... (Còn 152 KB)

task_management_guide.md
202 KB
♫...Xuân Bắc [SX],  — 09/04/2026 4:27 CH
xuanbac0531@gmail.com
Đinh Triệu — 09/04/2026 4:28 CH
3Anhemtrieuthanhbac
dinhtrieuxtnd@gmail.com
♫...Xuân Bắc [SX],  — 09/04/2026 4:41 CH
ssh root@103.245.237.251
Đinh Triệu — 09/04/2026 10:29 CH
# THIẾT KẾ CƠ SỞ DỮ LIỆU — ỨNG DỤNG NGHE NHẠC ONDAS
> Stack: PostgreSQL | ORM: Spring Data JPA (Hibernate)
> Kiến trúc: Onion/Clean Architecture

---

database_design.md
21 KB
Đinh Triệu — 09/04/2026 11:34 CH
Loại tệp đính kèm: unknown
functional_decomposition.puml
3.19 KB
Đinh Triệu — 12/04/2026 3:18 CH
# 🚀 HƯỚNG DẪN CI/CD: SPRING BOOT → JENKINS (TRÊN VPS) → APP (CÙNG VPS)

## 📌 TỔNG QUAN HỆ THỐNG

### Sơ đồ luồng:
```

HUONG-DAN-CICD-SPRINGBOOT-VPS.md
32 KB
ondas_user_jenkins

ondas_password_jenkins
jenkins_info.txt
1 KB
ip
103.245.237.251

password
3Anhemtrieuthanhbac

vps_info.txt
1 KB
Đinh Triệu — 18/04/2026 3:06 CH
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
POSTGRES_DB=ondas_db
POSTGRES_USER=ondas_user
POSTGRES_PASSWORD=ondas_password
POSTGRES_HOST=localhost
JWT_SECRET=CHANGE_ME_run_openssl_rand_hex_32
JWT_EXPIRATION=86400000
JWT_PASSWORD_RESET_EXPIRATION=60000
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=dinhtrieuxtnd@gmail.com
MAIL_PASSWORD=jdvucvinbttqlmgz
MAIL_FROM=dinhtrieuxtnd@gmail.com
APP_BASE_URL=http://localhost:8080/
https://support.google.com/accounts/answer/185833?hl=vi
Đăng nhập bằng mật khẩu ứng dụng - Tài khoản Googl...
Quan trọng: Bạn không nên dùng mật khẩu ứng dụng và cũng không cần sử dụng trong hầu hết các trường hợp. Để bảo mật tài khoản, hãy sử dụng tính năng “Đăng nhập bằng Google” để kết nối các ứng dụng với
Đinh Triệu — 18/04/2026 3:19 CH
pm.test("", function () {
    pm.response.to.be.success
    const {accessToken, refreshToken} = pm.response.json().data
    pm.environment.set("accessToken", accessToken);
    pm.environment.set("refreshToken", refreshToken);
})
{
    "refreshToken": "{{refreshToken}}"
}
Đinh Triệu — 20/04/2026 10:52 CH
https://www.postman.com/workspace/ondas~42a58d50-ba2a-4470-83db-e0f67a7dd520/collection/40831114-3ff57459-cfcf-4722-8ba7-bbbd98d6b544?action=share&source=copy-link&creator=40831114
Thành — 21/04/2026 12:17 SA
https://gemini.google.com/app/ed46ce4e6e0cb1fc?hl=vi
https://gemini.google.com/share/7c6b64f568f5
Thành — 21/04/2026 9:38 CH
https://app.visily.ai/projects/5db46790-9b07-420d-ba46-5f07cea72b0f/boards/2583098
Visily
♫...Xuân Bắc [SX],  — 21/04/2026 9:49 CH
xuanbac0531@gmil.com
Thành — 21/04/2026 10:09 CH
https://gemini.google.com/share/a7d6ee71497b
Đinh Triệu — 10:59 SA
POSTGRES_DB=ondas_db
POSTGRES_USER=ondas_user
POSTGRES_PASSWORD=ondas_password
POSTGRES_HOST=localhost
JWT_SECRET=CHANGE_ME_run_openssl_rand_hex_32
JWT_EXPIRATION=86400000
JWT_PASSWORD_RESET_EXPIRATION=60000
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=dinhtrieuxtnd@gmail.com
MAIL_PASSWORD=jdvucvinbttqlmgz
MAIL_FROM=dinhtrieuxtnd@gmail.com
APP_BASE_URL=http://localhost:8080/
Đinh Triệu — 11:10 SA
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
♫...Xuân Bắc [SX],  — 12:06 CH
C:\Users\xuanb>flutter doctor
Checking Dart SDK version...
Downloading Dart SDK from Flutter engine ...
Expanding downloaded archive with PowerShell...
Building flutter tool...
Running pub upgrade...

message.txt
3 KB
Đinh Triệu — 4:03 CH
-- =============================================================
-- ONDAS MUSIC APP — PostgreSQL Schema
-- Generated: 2026-04-09
-- =============================================================

-- Enable UUID extension

schema.sql
19 KB
Đinh Triệu — 4:52 CH
Đinh Triệu — 6:16 CH
# Ondas Backend — API Documentation

> **Base URL:** `http://localhost:8080`
> **Content-Type:** `application/json` (trừ các endpoint upload file dùng `multipart/form-data`)
> **Authentication:** `Authorization: Bearer <accessToken>`

API_DOCS.md
25 KB
﻿
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

> **Lưu ý:** `play_count` của bài hát tăng khi gọi `POST /api/play-history` thành công. Client nên gọi API này sau khi user nghe được ≥ 30 giây để tránh tính lượt nghe không hợp lệ.

### POST `/api/play-history`

Ghi lại lượt nghe nhạc.

**Auth:** ✅ Yêu cầu (JWT)

**Request Body:**
| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `songId` | UUID | ✅ | ID bài hát đã nghe |
| `durationPlayedSeconds` | integer | ❌ | Số giây đã nghe trong lượt này |
| `completed` | boolean | ❌ | Nghe hết bài hay chưa (mặc định `false`) |
| `source` | string | ❌ | Nguồn nghe: `search`, `album`, `playlist`, `home`, `artist`, `favorites`, `history` |

```json
{
  "songId": "uuid",
  "durationPlayedSeconds": 210,
  "completed": true,
  "source": "home"
}
```

**Response `201 Created`:**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": 1,
    "song": {
      "id": "uuid",
      "title": "Nơi Này Có Anh",
      "coverUrl": "https://...",
      "durationSeconds": 210,
      "audioUrl": "https://..."
    },
    "playedAt": "2026-04-27T15:30:00",
    "durationPlayedSeconds": 210,
    "completed": true,
    "source": "home"
  }
}
```

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
        "durationPlayedSeconds": 210,
        "completed": true,
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
