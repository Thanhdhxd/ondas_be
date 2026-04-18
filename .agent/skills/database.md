# Skill: Database & Persistence — Ondas Backend

> Kéo file này vào khi: thiết kế schema, viết JPA Model, query tùy chỉnh, xử lý migration.

---

## Tech Stack

| Công nghệ | Phiên bản | Vai trò |
|---|---|---|
| **PostgreSQL** | 15+ | Database chính (production) |
| `spring-boot-starter-data-jpa` | (Spring Boot managed) | ORM với Hibernate |
| **Hibernate** | (Spring Boot managed) | JPA implementation |
| `H2` | (scope: test) | In-memory DB cho unit/integration test |

---

## Quản lý Schema

- Schema quản lý **hoàn toàn qua JPA Model** — Hibernate tự sync.
- **Không dùng Flyway** hoặc bất kỳ công cụ migration thủ công nào.
- Mọi thay đổi schema: **chỉnh sửa JPA Model** tương ứng.

```yaml
# application.yml
spring:
  jpa:
    hibernate:
      ddl-auto: update      # Tự update schema khi khởi động
    show-sql: true          # Bật khi dev để kiểm tra SQL sinh ra
    properties:
      hibernate:
        format_sql: true
```

> ⚠️ Khi thêm cột mới: đặt `nullable = true` hoặc có `DEFAULT` để tránh lỗi với dữ liệu cũ.

---

## JPA Model Convention

- Đặt tại: `infrastructure/persistence/model/`
- Tên class: `PascalCase + Model` — `UserModel`, `SongModel`
- Tên bảng: `snake_case` số nhiều — `users`, `songs`, `playlists`
- Tên cột: `snake_case` — `created_at`, `artist_id`
- **Bắt buộc có** converter `toDomain()` và `fromDomain()` trong mỗi Model.

```java
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
    private Long albumId;  // nullable — single track

    @Column(name = "audio_url", nullable = false)
    private String audioUrl;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    // Converter: Model → Domain
    public Song toDomain() {
        return new Song(id, title, artistId, albumId, audioUrl, durationSeconds);
    }

    // Converter: Domain → Model
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

---

## JPA Repository Convention

- Đặt tại: `infrastructure/persistence/jparepo/`
- Tên interface: `PascalCase + JpaRepo` — `UserJpaRepo`, `SongJpaRepo`
- Extend `JpaRepository<Model, ID>` hoặc `PagingAndSortingRepository` nếu cần paging.
- Chỉ khai báo method query — **không viết logic** ở đây.

```java
public interface SongJpaRepo extends JpaRepository<SongModel, Long> {
    boolean existsByTitleAndArtistId(String title, Long artistId);
    List<SongModel> findByArtistId(Long artistId);
}
```

---

## Persistence Adapter Convention

- Đặt tại: `infrastructure/persistence/adapter/`
- Tên class: `PascalCase + Adapter` — `UserAdapter`, `SongAdapter`
- **Implements** `*RepoPort` từ `domain`.
- Chỉ chứa logic convert Model ↔ Domain + gọi JpaRepo.

```java
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
}
```

---

## Environment Variables — Database

| Biến | Mô tả |
|---|---|
| `DB_URL` | JDBC URL — `jdbc:postgresql://host:5432/ondas_db` |
| `DB_USERNAME` | Username PostgreSQL |
| `DB_PASSWORD` | Password PostgreSQL |
