# Ondas Mobile — Test Cases: Phát nhạc (Player Playback)

> **Tính năng**: Phát/tạm dừng, chuyển bài, tua, âm lượng, repeat, mini player, queue, lyrics, auto-play  
> **Phiên bản**: 1.0  
> **Ngày**: 22/05/2026  
> **Loại test**: Functional E2E Test Cases (Manual / Exploratory)  
> **File gốc**: `music_streaming_test_case.md` — TCs: 01-16

---

## Thành phần liên quan

| Thành phần | Mô tả |
|---|---|
| `PlayerScreen` | Màn hình phát nhạc đầy đủ — controls, seekbar, artwork, tabs (Queue/Lyrics) |
| `MiniPlayer` | Mini player hiển thị ở bottom khi rời PlayerScreen |
| `PlayerBloc` | Quản lý state phát nhạc — `PlaySongRequested`, `PauseRequested`, `ResumeRequested`, etc. |
| `PlayerService` | Service xử lý audio — play, pause, seek, volume, repeat mode |

---

## Bảng Test Cases

| STT (ID) | Nhóm | Tên Test Case | Các bước thực hiện (Steps) | Dữ liệu kiểm thử (Test Data) | Kết quả mong đợi (Expected Result) |
|---|---|---|---|---|---|
| 01 | Happy Path | Phát một bài hát từ danh sách bài hát (SongListScreen) | 1) Mở màn danh sách bài hát của một Artist hoặc Album<br>2) Chạm vào một bài hát trong danh sách<br>3) Quan sát màn hình Player | Song: bất kỳ bài hát hợp lệ có `audioUrl` khả dụng | PlayerScreen mở ra (slide-up transition); `PlayerState.status = loading` rồi chuyển sang `playing`; hiển thị đúng tiêu đề, nghệ sĩ, ảnh bìa của bài hát |
| 02 | Happy Path | Phát một bài hát từ kết quả tìm kiếm (SearchScreen) | 1) Vào màn Search<br>2) Nhập từ khóa tìm kiếm<br>3) Chạm vào một bài hát trong kết quả<br>4) Quan sát màn hình Player | Từ khóa: "We Are the World" | PlayerScreen mở ra; bài hát bắt đầu phát; `PlaySongRequested` được dispatch với đúng `index` và danh sách `songs` từ kết quả tìm kiếm |
| 03 | Happy Path | Phát một bài hát từ danh sách yêu thích (Favorites) | 1) Vào màn Favorites<br>2) Chạm vào một bài hát yêu thích<br>3) Quan sát màn hình Player | Song đã được thêm vào favorites | PlayerScreen mở ra; bài hát phát thành công; queue chứa toàn bộ danh sách favorites hiện tại |
| 04 | Happy Path | Phát một bài hát từ Playlist Detail | 1) Mở một Playlist có nhiều bài hát<br>2) Chạm vào một bài hát bất kỳ trong playlist<br>3) Quan sát màn hình Player | Playlist: "My Summer Mix" (≥ 3 bài) | PlayerScreen mở ra; bài hát được chọn phát đúng; queue = toàn bộ bài hát trong playlist; `source` được truyền đúng |
| 05 | Happy Path | Tạm dừng (Pause) và tiếp tục (Resume) phát nhạc | 1) Phát một bài hát đang chạy<br>2) Nhấn nút Pause<br>3) Quan sát trạng thái<br>4) Nhấn nút Play (Resume)<br>5) Quan sát trạng thái | Bài hát bất kỳ đang `playing` | B1: `status = playing`; B2: PlayerState chuyển sang `paused`, nút đổi thành Play; B3: thanh seekbar ngừng di chuyển; B4: `status = playing`, nhạc tiếp tục từ vị trí đã dừng |
| 06 | Happy Path | Chuyển bài tiếp theo (Skip Next) | 1) Phát bài đầu tiên trong queue ≥ 3 bài<br>2) Nhấn nút Next<br>3) Quan sát bài hát hiện tại | Queue: [Song A, Song B, Song C]; currentIndex = 0 | `currentIndex` chuyển thành 1; PlayerState cập nhật `currentSong = Song B`; bài hát B bắt đầu phát từ đầu; `position = Duration.zero` |
| 07 | Happy Path | Chuyển bài trước đó (Skip Previous) — bài đã phát > 3 giây | 1) Phát bài thứ 2 trong queue<br>2) Đợi > 3 giây<br>3) Nhấn nút Previous<br>4) Quan sát | Queue: [Song A, Song B, Song C]; currentIndex = 1; position > 3s | Player quay về đầu bài hiện tại (Song B); `position = Duration.zero`; `currentIndex` vẫn = 1 |
| 08 | Happy Path | Chuyển bài trước đó (Skip Previous) — bài mới phát < 3 giây | 1) Phát bài thứ 2 trong queue<br>2) Nhấn nút Previous ngay trong vòng < 3 giây<br>3) Quan sát | Queue: [Song A, Song B, Song C]; currentIndex = 1; position < 3s | Player chuyển về bài trước đó (Song A); `currentIndex = 0`; `position = Duration.zero` |
| 09 | Happy Path | Tua (Seek) đến một vị trí cụ thể trong bài hát | 1) Phát một bài hát dài ≥ 3 phút<br>2) Kéo thanh seekbar đến vị trí 1:30<br>3) Thả ra | Bài hát dài 3:00+; seek đến 1:30 | `position` cập nhật thành ~1:30; nhạc tiếp tục phát từ vị trí 1:30 |
| 10 | Happy Path | Điều chỉnh âm lượng (Volume) | 1) Phát một bài hát<br>2) Kéo thanh trượt âm lượng đến 50%<br>3) Quan sát | Volume: kéo từ 1.0 → 0.5 | `PlayerState.volume = 0.5`; âm thanh phát ra nhỏ hơn; thanh trượt hiển thị đúng 50% |
| 11 | Happy Path | Chuyển đổi chế độ lặp (Repeat Mode): Off → All → One → Off | 1) Phát một bài hát<br>2) Nhấn nút Repeat 1 lần<br>3) Nhấn nút Repeat lần 2<br>4) Nhấn nút Repeat lần 3<br>5) Quan sát icon qua mỗi lần nhấn | Repeat mode bắt đầu từ `off` | B2: `repeatMode = all`, icon hiển thị repeat-all; B3: `repeatMode = one`, icon hiển thị repeat-one; B4: `repeatMode = off`, icon tắt; chu kỳ lặp đúng |
| 12 | Happy Path | Mini Player hiển thị đúng bài hát đang phát | 1) Phát một bài hát<br>2) Quay về màn hình chính (Home)<br>3) Quan sát Mini Player ở bottom | Song: "Blinding Lights" – The Weeknd | Mini Player hiển thị ở bottom; đúng tiêu đề + nghệ sĩ; có nút Play/Pause; chạm vào Mini Player mở PlayerScreen |
| 13 | Happy Path | Tab Queue hiển thị đúng danh sách chờ | 1) Phát một bài trong queue ≥ 3 bài<br>2) Vào PlayerScreen<br>3) Swipe/chọn tab "Queue" | Queue: [Song A, Song B, Song C]; currentIndex = 0 | Tab Queue hiển thị đầy đủ 3 bài; bài đang phát (Song A) được đánh dấu; thứ tự đúng |
| 14 | Happy Path | Auto-play bài tiếp theo khi bài hiện tại kết thúc | 1) Phát bài đầu tiên trong queue ≥ 2 bài<br>2) Chờ bài hát phát hết (hoặc seek đến gần cuối)<br>3) Quan sát | Queue: [Song A, Song B]; currentIndex = 0 | Khi Song A kết thúc, tự động chuyển sang Song B; `currentIndex = 1`; `status = playing` |
| 15 | Happy Path | Ghi nhận lịch sử phát (Play History) khi phát bài hát | 1) Phát một bài hát thành công<br>2) Kiểm tra API ghi nhận lịch sử phát | Song: id = "song_001"; source = "search" | Gọi API `recordPlayHistory(songId, source)` được thực thi; không crash nếu API lỗi (`.ignore()`) |
| 16 | Happy Path | Tab Lyrics hiển thị lời bài hát (nếu có) | 1) Phát một bài hát có lyrics<br>2) Vào PlayerScreen<br>3) Chọn tab "Lyrics" | Bài hát có dữ liệu lyrics (synced hoặc static) | Tab Lyrics hiển thị lời bài hát; nếu là synced lyrics, lời được highlight theo thời gian thực; nếu là static, hiển thị toàn bộ lời |

---

> **Tổng số Test Cases:** 16  
> **Phân bố:** Happy Path: 16  
> **Phạm vi:** Phát nhạc từ nhiều nguồn (list/search/favorites/playlist), pause/resume, next/previous, seek, volume, repeat mode, mini player, queue, auto-play, play history, lyrics
