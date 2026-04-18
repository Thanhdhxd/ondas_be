# Skill: Architecture — Ondas Backend

> Kéo file này vào khi: thiết kế feature mới, refactor, hỏi về kiến trúc tổng quan.

---

## Tổng quan dự án

**Ondas** là ứng dụng nghe nhạc trực tuyến. Backend cung cấp REST API cho:

| Client | Mô tả |
|---|---|
| Mobile App | Flutter — User: nghe nhạc, playlist, yêu thích |
| Admin Web | Flutter Web — Quản lý bài hát, user, thống kê |

**Package gốc**: `com.example.ondas`

---

## Onion Architecture — 4 tầng

```
domain  ←  application  ←  infrastructure
                        ←  presentation
```

| Tầng | Vai trò | Phụ thuộc vào |
|---|---|---|
| `domain` | Nghiệp vụ thuần — Entity, RepoPort | Không ai |
| `application` | Service (port, impl), DTO, Mapper | `domain` |
| `infrastructure` | JPA, Security, MinIO, WebSocket | `domain`, `application/service/port` |
| `presentation` | REST Controller, ExceptionHandler | `application` |

**Lý do chọn**: Tách biệt hoàn toàn business logic khỏi framework — dễ test, dễ thay đổi công nghệ mà không ảnh hưởng nghiệp vụ.

---

## Port & Adapter Pattern

```
Service  →  *RepoPort (interface, domain)
                 ↑
            *Adapter (impl, infrastructure)
                 ↑
            *JpaRepo (Spring Data JPA)
```

- `*RepoPort` là **contract** do domain định nghĩa — Service chỉ biết đến interface này.
- `*Adapter` ở `infrastructure` implement `*RepoPort` — chứa logic JPA cụ thể.
- **Lợi ích**: Domain và Application layer không biết JPA tồn tại → có thể swap sang MongoDB hay bất kỳ DB nào mà không đụng Service.

---

## Luồng xử lý một API request

```
HTTP Request
    ↓
Controller (presentation)
    ↓  gọi *ServicePort
Service (application)
    ↓  gọi *RepoPort
Adapter (infrastructure)
    ↓  gọi *JpaRepo
PostgreSQL
```

---

## Dependency Rule — tóm tắt

| Tầng | Được import | Cấm import |
|---|---|---|
| `domain` | Lombok | Spring, JPA, bất kỳ framework nào |
| `application` | `domain` | `infrastructure` |
| `infrastructure` | `domain`, Spring, JPA, `application/service/port` | `application/service/impl` |
| `presentation` | `application/dto`, `application/service/port` | `infrastructure`, `domain/entity` trực tiếp |

> **Giải thích `infrastructure` → `application/service/port`**: Infrastructure được phép implement các interface port định nghĩa ở application (ví dụ `EmailServicePort`, `StoragePort`). Đây là Dependency Inversion — infrastructure biết *contract* để implement, nhưng không được gọi ngược lên *implementation* của Service.
