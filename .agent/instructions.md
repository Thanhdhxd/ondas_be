# Instructions — Ondas Backend

> **Phạm vi**: Toàn bộ project `ondas_be`
> **Mục đích**: Quy tắc bắt buộc cho developer và AI khi sinh/chỉnh sửa code.

---

## 1. CẤU TRÚC THƯ MỤC — BẮT BUỘC TUÂN THỦ

```
src/main/java/com/example/ondas/
│
├── presentation/
│   ├── controller/                # REST Controller
│   └── advice/                    # GlobalExceptionHandler
│
├── application/
│   ├── service/
│   │   ├── port/                  # Interface (contract) của service
│   │   └── impl/                  # Implementation của service
│   ├── dto/
│   │   ├── common/                # ApiResponse, PageResultDto, ...
│   │   ├── request/               # *Request DTO
│   │   └── response/              # *Response DTO
│   └── mapper/                    # MapStruct interface
│
├── infrastructure/
│   ├── persistence/
│   │   ├── adapter/               # Implements *RepoPort
│   │   ├── model/                 # JPA Entity (@Entity)
│   │   └── jparepo/               # Spring Data JPA repo
│   ├── security/                  # JWT filter, Spring Security config
│   ├── email/                     # Email provider (EmailAdapter)
│   ├── google/                    # Google API (GoogleAuthAdapter)
│   ├── storage/                   # MinIO / S3 upload
│   └── websocket/                 # WebSocket (nếu có)
│
└── domain/
    ├── entity/                    # Domain Entity — thuần Java
    └── repoport/                  # Repository interface (Port)

src/test/java/com/example/ondas/
├── unit/service/                  # Unit test — Service
└── integration/controller/        # Integration test — Controller
```

---

## 2. NGUYÊN TẮC PHÂN TẦNG

### 2.1 Dependency rule

```
presentation  →  application  →  domain
infrastructure               →  domain
```

| Tầng | Được phép import | Cấm import |
|---|---|---|
| `domain` | Lombok | Spring, JPA |
| `application` | `domain` | `infrastructure` |
| `infrastructure` | `domain`, Spring, JPA, `application/service/port` | `application/service/impl` |
| `presentation` | `application/dto`, `application/service/port` | `infrastructure`, `domain/entity` trực tiếp |

### 2.2 Quy tắc quan trọng nhất

- **TUYỆT ĐỐI KHÔNG** inject `*JpaRepo` vào Service. Service chỉ inject `*RepoPort`.
- **TUYỆT ĐỐI KHÔNG** để business logic trong Controller hay Adapter.
- **TUYỆT ĐỐI KHÔNG** gọi `*Adapter` trực tiếp từ Controller.
- Infrastructure **được phép** implement interface từ `application/service/port` (vd: `EmailPort`, `StoragePort`) — đây là Dependency Inversion bình thường, không vi phạm kiến trúc.

---

## 3. NAMING CONVENTION

### 3.1 Class

| Loại | Convention | Ví dụ |
|---|---|---|
| Domain Entity | `PascalCase` (danh từ số ít) | `User`, `Song`, `Playlist` |
| JPA Model | `PascalCase + Model` | `UserModel`, `SongModel` |
| Repository Port | `PascalCase + RepoPort` | `UserRepoPort`, `SongRepoPort` |
| JPA Repository | `PascalCase + JpaRepo` | `UserJpaRepo`, `SongJpaRepo` |
| Persistence Adapter | `PascalCase + Adapter` | `UserAdapter`, `SongAdapter` |
| External Adapter (Third-party)| `PascalCase + Adapter` | `EmailAdapter`, `GoogleAuthAdapter` |
| Service Port | `PascalCase + ServicePort` | `AuthServicePort`, `SongServicePort` |
| External Port (Third-party) | `PascalCase + Port` | `EmailPort`, `StoragePort` |
| Service Impl | `PascalCase + Service` | `AuthService`, `SongService` |
| Controller | `PascalCase + Controller` | `AuthController`, `SongController` |
| MapStruct Mapper | `PascalCase + Mapper` | `SongMapper`, `UserMapper` |
| Request DTO | `Verb + Noun + Request` | `LoginRequest`, `CreateSongRequest` |
| Response DTO | `Noun + Response` | `UserResponse`, `SongResponse` |
| Custom Exception | `PascalCase + Exception` | `NotFoundException`, `DuplicateSongException` |

### 3.2 Package & File

- Package: `lowercase` — `com.example.ondas.application.service.impl`
- File Java: `PascalCase.java` khớp với tên class

### 3.3 Database & API

| Loại | Convention | Ví dụ |
|---|---|---|
| Tên bảng | `snake_case` số nhiều | `users`, `songs`, `playlists` |
| Tên cột | `snake_case` | `created_at`, `artist_id` |
| API path | `kebab-case` | `/api/songs`, `/api/favorite-songs` |

---

## 4. CODE PATTERN BẮT BUỘC

### 4.1 Domain Entity — thuần Java, Lombok được phép

```java
// domain/entity/Song.java
@Getter
@AllArgsConstructor
public class Song {
    private Long id;
    private String title;
    private Long artistId;
    private Long albumId;       // nullable — single track
    private String audioUrl;
    private Integer durationSeconds;

    public boolean isSingle() {
        return this.albumId == null;
    }
}
```

### 4.2 Repository Port — interface ở domain

```java
// domain/repoport/SongRepoPort.java
public interface SongRepoPort {
    Song save(Song song);
    Optional<Song> findById(Long id);
    Page<Song> findAll(Pageable pageable);
    void deleteById(Long id);
    boolean existsByTitleAndArtistId(String title, Long artistId);
}
```

### 4.3 JPA Model — có annotation, có converter toDomain/fromDomain

```java
// infrastructure/persistence/model/SongModel.java
@Entity
@Table(name = "songs")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SongModel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "artist_id", nullable = false)
    private Long artistId;

    @Column(name = "album_id")
    private Long albumId;

    @Column(name = "audio_url", nullable = false)
    private String audioUrl;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    public Song toDomain() {
        return new Song(id, title, artistId, albumId, audioUrl, durationSeconds);
    }

    public static SongModel fromDomain(Song song) {
        return SongModel.builder()
                .id(song.getId())
                .title(song.getTitle())
                .artistId(song.getArtistId())
                .albumId(song.getAlbumId())
                .audioUrl(song.getAudioUrl())
                .durationSeconds(song.getDurationSeconds())
                .build();
    }
}
```

### 4.4 Persistence Adapter — implements RepoPort

```java
// infrastructure/persistence/adapter/SongAdapter.java
@Component
@RequiredArgsConstructor
public class SongAdapter implements SongRepoPort {
    private final SongJpaRepo songJpaRepo;

    @Override
    public Song save(Song song) {
        return songJpaRepo.save(SongModel.fromDomain(song)).toDomain();
    }

    @Override
    public Optional<Song> findById(Long id) {
        return songJpaRepo.findById(id).map(SongModel::toDomain);
    }

    @Override
    public Page<Song> findAll(Pageable pageable) {
        return songJpaRepo.findAll(pageable).map(SongModel::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        songJpaRepo.deleteById(id);
    }

    @Override
    public boolean existsByTitleAndArtistId(String title, Long artistId) {
        return songJpaRepo.existsByTitleAndArtistId(title, artistId);
    }
}
```

### 4.5 Service — inject Port, không inject JPA trực tiếp

```java
// application/service/impl/SongService.java
@Service
@RequiredArgsConstructor
public class SongService implements SongServicePort {
    private final SongRepoPort songRepoPort;
    private final ArtistRepoPort artistRepoPort;
    private final SongMapper songMapper;

    @Override
    public SongResponse createSong(CreateSongRequest request) {
        if (!artistRepoPort.existsById(request.getArtistId())) {
            throw new NotFoundException("Artist not found: " + request.getArtistId());
        }
        if (songRepoPort.existsByTitleAndArtistId(request.getTitle(), request.getArtistId())) {
            throw new DuplicateSongException("Song already exists for this artist");
        }
        Song song = new Song(null, request.getTitle(), request.getArtistId(),
                             request.getAlbumId(), request.getAudioUrl(),
                             request.getDurationSeconds());
        return songMapper.toResponse(songRepoPort.save(song));
    }
}
```

### 4.6 Controller — trả về ApiResponse chuẩn, dùng @Valid

```java
// presentation/controller/SongController.java
@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
public class SongController {
    private final SongServicePort songServicePort;

    @PostMapping
    public ResponseEntity<ApiResponse<SongResponse>> createSong(
            @Valid @RequestBody CreateSongRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(songServicePort.createSong(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SongResponse>> getSong(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(songServicePort.getSongById(id)));
    }
}
```

---

## 5. QUY TẮC MAPPER

| Hướng | Cách áp dụng |
|---|---|
| `JPA Model ↔ Domain Entity` | Method `toDomain()` / `fromDomain()` trong JPA Model |
| `Request DTO → Domain Entity` | Inline trong Service (thường kèm validate/logic) |
| `Domain Entity → Response DTO` | MapStruct interface trong `application/mapper/` |

### MapStruct interface

```java
// application/mapper/SongMapper.java
@Mapper(componentModel = "spring")
public interface SongMapper {
    @Mapping(source = "durationSeconds", target = "duration") // nếu field name khác nhau
    SongResponse toResponse(Song song);
    List<SongResponse> toResponseList(List<Song> songs);
}
```

> Lombok phải khai báo **trước** MapStruct trong `annotationProcessorPaths` của `pom.xml`.

---

## 6. API RESPONSE FORMAT

Mọi API trả về `ApiResponse<T>`:

```java
// application/dto/common/ApiResponse.java
@Data @Builder
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder().success(true).message("OK").data(data).build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder().success(false).message(message).data(null).build();
    }
}
```

**Thành công:**
```json
{ "success": true, "message": "OK", "data": { "id": 1, "title": "Nơi Này Có Anh" } }
```

**Lỗi:**
```json
{ "success": false, "message": "Song not found with id: 99", "data": null }
```

---

## 7. XỬ LÝ EXCEPTION

- Tất cả exception xử lý **tập trung** tại `GlobalExceptionHandler.java`.
- **KHÔNG** xử lý exception bằng `try-catch` trong Controller.
- Tạo custom exception riêng cho từng loại lỗi nghiệp vụ.

```java
// presentation/advice/GlobalExceptionHandler.java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(DuplicateSongException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicate(DuplicateSongException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(message));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(ex.getMessage()));
    }
}
```

---

## 8. VALIDATION

- Validate **format/type** ở Request DTO bằng Bean Validation (`jakarta.validation`).
- Validate **business rule** ở Service layer.
- **KHÔNG** validate thủ công trong Controller — dùng `@Valid` và để `GlobalExceptionHandler` xử lý.

```java
// application/dto/request/CreateSongRequest.java
@Data
public class CreateSongRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotNull(message = "Artist ID is required")
    private Long artistId;

    private Long albumId; // optional

    @NotNull(message = "Duration is required")
    @Positive(message = "Duration must be positive")
    private Integer durationSeconds;
}
```

---

## 9. BẢO MẬT

- Authentication bằng **JWT** — token đính kèm trong header `Authorization: Bearer <token>`.
- Authorization dựa trên **Role**: `USER` và `ADMIN`.

| Role | Quyền |
|---|---|
| `USER` | Nghe nhạc, quản lý playlist, yêu thích |
| `ADMIN` | CRUD bài hát, quản lý user, xem thống kê |

- Endpoint công khai (không cần auth): `POST /api/auth/login`, `POST /api/auth/register`
- Endpoint yêu cầu ADMIN: mọi route `/api/admin/**`
- **KHÔNG** log password, JWT secret, hay thông tin cá nhân.
- Message lỗi auth luôn dùng `"Invalid credentials"` — **không tiết lộ** email có tồn tại hay không.

---

## 10. QUẢN LÝ DATABASE SCHEMA

- Schema quản lý **hoàn toàn qua JPA Model** — Hibernate tự sync với `ddl-auto: update`.
- **Không dùng Flyway** hoặc migration thủ công.
- Khi thêm cột mới: đặt `nullable = true` hoặc có `DEFAULT` để tránh lỗi với dữ liệu cũ.

```yaml
# application.yml
spring:
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
```

---

## 11. QUY TẮC KIỂM THỬ

### Phạm vi

| Layer | Loại test | Mức độ |
|---|---|---|
| `Service` | Unit test (mock `*RepoPort`) | **Bắt buộc** |
| `Controller` | Integration test (`@WebMvcTest`) | **Nên có** |
| `Adapter` / `JpaRepo` | — | Không cần |

### Unit Test — Service

```java
@ExtendWith(MockitoExtension.class)
class SongServiceTest {
    @Mock private SongRepoPort songRepoPort;
    @Mock private ArtistRepoPort artistRepoPort;
    @InjectMocks private SongService songService;

    @Test
    void createSong_WhenValid_ShouldReturnSongResponse() {
        // arrange
        when(artistRepoPort.existsById(1L)).thenReturn(true);
        when(songRepoPort.existsByTitleAndArtistId(any(), any())).thenReturn(false);
        when(songRepoPort.save(any())).thenReturn(new Song(1L, "Test", 1L, null, "url", 210));
        // act & assert
        SongResponse result = songService.createSong(buildRequest());
        assertNotNull(result);
        verify(songRepoPort).save(any());
    }

    @Test
    void createSong_WhenArtistNotFound_ShouldThrowNotFoundException() {
        when(artistRepoPort.existsById(99L)).thenReturn(false);
        assertThrows(NotFoundException.class, () -> songService.createSong(buildRequest()));
        verify(songRepoPort, never()).save(any());
    }
}
```

### Integration Test — Controller

```java
@WebMvcTest(SongController.class)
class SongControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private SongServicePort songServicePort;

    @Test
    void createSong_ShouldReturn201_WhenRequestValid() throws Exception {
        when(songServicePort.createSong(any())).thenReturn(new SongResponse(1L, "Test", 1L, 210));

        mockMvc.perform(post("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "title": "Test", "artistId": 1, "genreId": 2, "durationSeconds": 210 }
                """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true));
    }
}
```

**Yêu cầu coverage:** Service layer ≥ 70% (đo bằng JaCoCo).

---

## 12. QUY TẮC CHUNG

1. **Không hardcode** URL, secret, config — đặt trong `application.yml`, đọc bằng `@Value` hoặc `@ConfigurationProperties`.
2. **Không inject** `*JpaRepo` vào Service — luôn inject `*RepoPort`.
3. **Không để** business logic trong Controller hay Adapter.
4. **Luôn dùng** `@Valid` cho request body trong Controller.
5. **Comment** business logic phức tạp bằng tiếng Việt; JavaDoc public API bằng tiếng Anh.
6. **Không log** thông tin nhạy cảm (password, token, thông tin cá nhân).
7. Mỗi Service chỉ xử lý nghiệp vụ của **1 entity chính** — nếu cần nhiều entity, inject thêm `*RepoPort` tương ứng.