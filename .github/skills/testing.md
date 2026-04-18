# Skill: Testing — Ondas Backend

> Kéo file này vào khi: viết unit test, integration test, setup JaCoCo coverage.

---

## Tech Stack

| Thư viện | Vai trò |
|---|---|
| `spring-boot-starter-test` | JUnit 5, Mockito, AssertJ (tích hợp sẵn) |
| `spring-security-test` | Test với security context (`@WithMockUser`) |
| `H2` (scope: test) | In-memory DB thay PostgreSQL khi chạy test |
| **JaCoCo** | Code coverage report |

---

## Phạm vi test

| Layer | Loại test | Mức độ | Lý do |
|---|---|---|---|
| `Service` | Unit test (mock `*RepoPort`) | **Bắt buộc** | Chứa toàn bộ business logic |
| `Controller` | Integration test (`@WebMvcTest`) | **Nên có** | Kiểm tra HTTP status, request/response format |
| `Adapter` / `JpaRepo` | — | Không cần | Không có business logic |

**Yêu cầu coverage**: Service layer ≥ 70% (đo bằng JaCoCo).

---

## Vị trí đặt test

```
src/test/java/com/example/ondas/
├── unit/
│   └── service/
│       ├── AuthServiceTest.java
│       ├── SongServiceTest.java
│       └── ...
└── integration/
    └── controller/
        ├── AuthControllerTest.java
        ├── SongControllerTest.java
        └── ...
```

---

## Unit Test — Service

Dùng `@ExtendWith(MockitoExtension.class)`, mock toàn bộ `*RepoPort`.

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
        when(songRepoPort.save(any())).thenReturn(
            new Song(1L, "Nơi Này Có Anh", 1L, null, "url", 210));

        // act
        SongResponse result = songService.createSong(buildRequest());

        // assert
        assertNotNull(result);
        assertEquals("Nơi Này Có Anh", result.getTitle());
        verify(songRepoPort).save(any());
    }

    @Test
    void createSong_WhenArtistNotFound_ShouldThrowNotFoundException() {
        when(artistRepoPort.existsById(99L)).thenReturn(false);
        assertThrows(NotFoundException.class, () -> songService.createSong(buildRequest()));
        verify(songRepoPort, never()).save(any());
    }

    @Test
    void createSong_WhenDuplicateTitle_ShouldThrowDuplicateSongException() {
        when(artistRepoPort.existsById(1L)).thenReturn(true);
        when(songRepoPort.existsByTitleAndArtistId(any(), any())).thenReturn(true);
        assertThrows(DuplicateSongException.class, () -> songService.createSong(buildRequest()));
    }
}
```

---

## Integration Test — Controller

Dùng `@WebMvcTest` — không khởi động full context, mock Service bằng `@MockBean`.

```java
@WebMvcTest(SongController.class)
class SongControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private SongServicePort songServicePort;

    @Test
    void createSong_ShouldReturn201_WhenRequestValid() throws Exception {
        when(songServicePort.createSong(any()))
            .thenReturn(new SongResponse(1L, "Nơi Này Có Anh", 1L, 210));

        mockMvc.perform(post("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "title": "Nơi Này Có Anh", "artistId": 1, "genreId": 2, "durationSeconds": 210 }
                """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.title").value("Nơi Này Có Anh"));
    }

    @Test
    void createSong_ShouldReturn400_WhenTitleMissing() throws Exception {
        mockMvc.perform(post("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{ "artistId": 1, "durationSeconds": 210 }"""))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }
}
```

---

## Chạy test

```bash
# Chạy toàn bộ test
./mvnw test

# Chạy 1 class cụ thể
./mvnw test -Dtest=SongServiceTest

# Coverage report (JaCoCo) — xem tại target/site/jacoco/index.html
./mvnw test jacoco:report
```
