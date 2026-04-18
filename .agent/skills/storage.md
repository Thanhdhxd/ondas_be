# Skill: Storage & Mail — Ondas Backend

> Kéo file này vào khi: viết logic upload file, xử lý lưu trữ audio/ảnh, gửi email.

---

## Tech Stack

| Công nghệ | Vai trò |
|---|---|
| **MinIO** | Object storage tự host — lưu file âm thanh, ảnh nghệ sĩ, ảnh album |
| **AWS S3** | Thay thế MinIO trên production cloud (cùng API) |
| `spring-boot-starter-mail` | Gửi email xác thực, thông báo qua SMTP |

---

## MinIO / S3

### Vị trí code

```
infrastructure/storage/
├── StorageService.java         # Interface (hoặc trực tiếp implementation)
├── MinioStorageService.java    # Upload, delete, get URL
└── StorageConfig.java          # Bean cấu hình MinioClient
```

### Các loại file lưu trữ

| Loại file | Bucket gợi ý | Mô tả |
|---|---|---|
| File âm thanh | `ondas-audio` | `.mp3`, `.flac`, `.wav` |
| Ảnh nghệ sĩ | `ondas-images` | Avatar nghệ sĩ |
| Ảnh album / playlist | `ondas-images` | Cover art |

### Quy tắc

- Upload file → lưu **URL** vào DB, không lưu binary vào PostgreSQL.
- Tên file nên được **rename** theo UUID để tránh trùng lặp.
- URL trả về client phải là public URL hoặc pre-signed URL tùy policy bucket.

---

## Spring Mail

### Vị trí code

```
infrastructure/mail/
├── MailService.java            # Interface
└── MailServiceImpl.java        # Implement — gọi JavaMailSender
```

### Các loại email

| Loại | Trigger |
|---|---|
| Đặt lại mật khẩu | Khi user yêu cầu reset password |

---

## Environment Variables

| Biến | Mô tả |
|---|---|
| `MINIO_ENDPOINT` | URL MinIO server — vd: `http://localhost:9000` |
| `MINIO_ACCESS_KEY` | Access key MinIO |
| `MINIO_SECRET_KEY` | Secret key MinIO |
| `MINIO_BUCKET_AUDIO` | Tên bucket audio |
| `MINIO_BUCKET_IMAGE` | Tên bucket ảnh |
| `MAIL_HOST` | SMTP host — vd: `smtp.gmail.com` |
| `MAIL_PORT` | SMTP port — vd: `587` |
| `MAIL_USERNAME` | Email gửi |
| `MAIL_PASSWORD` | App password SMTP |
