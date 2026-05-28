# Ondas Mobile — Test Cases: Boundary — Repeat Mode, Display, Rapid Actions

> **Tính năng**: Repeat mode edge cases, hiển thị dữ liệu đặc biệt, rapid actions, empty queue  
> **Phiên bản**: 1.0  
> **Ngày**: 22/05/2026  
> **Loại test**: Functional E2E Test Cases (Manual / Exploratory)  
> **File gốc**: `music_streaming_test_case.md` — TCs: 30-40

---

## Bảng Test Cases

| STT (ID) | Nhóm | Tên Test Case | Các bước thực hiện (Steps) | Dữ liệu kiểm thử (Test Data) | Kết quả mong đợi (Expected Result) |
|---|---|---|---|---|---|
| 30 | Boundary | Chế độ Repeat = All — bài cuối → bài đầu | 1) Phát bài cuối cùng trong queue ≥ 2 bài<br>2) Để repeat mode = All<br>3) Nhấn Next hoặc chờ bài hát kết thúc | Queue: [A, B, C]; currentIndex = 2; repeatMode = all | Khi chuyển bài, `currentIndex` quay về 0; bài đầu tiên (A) được phát |
| 31 | Boundary | Chế độ Repeat = One — phát lặp 1 bài | 1) Phát một bài hát<br>2) Bật repeat mode = One<br>3) Chờ bài hát kết thúc | Queue: [A, B, C]; currentIndex = 0; repeatMode = one | Bài A phát lặp lại từ đầu khi kết thúc; `currentIndex` không đổi |
| 32 | Boundary | Chế độ Repeat = Off — bài cuối kết thúc | 1) Phát bài cuối cùng trong queue<br>2) Để repeat mode = Off<br>3) Chờ bài hát kết thúc | Queue: [A, B, C]; currentIndex = 2; repeatMode = off | Khi bài hát kết thúc, `PlayerState.status` chuyển về `idle`; `currentSong = null`; không tự động phát bài mới |
| 33 | Boundary | Tiêu đề bài hát quá dài (nhiều ký tự) | 1) Phát bài hát có title > 100 ký tự<br>2) Quan sát PlayerScreen và Mini Player | Title: "A Very Long Song Title That Exceeds Normal Length And Goes On And On And On..." | Tiêu đề hiển thị với ellipsis (`TextOverflow.ellipsis`); không bị tràn layout; không crash |
| 34 | Boundary | Tên nghệ sĩ có nhiều nghệ sĩ (≥ 5 artists) | 1) Phát bài hát có 5+ nghệ sĩ<br>2) Quan sát hiển thị | artistNames: ["Artist A", "Artist B", "Artist C", "Artist D", "Artist E"] | Hiển thị `artistDisplay` đúng định dạng "A, B, C, D, E"; dùng ellipsis nếu quá dài; không crash |
| 35 | Boundary | Ảnh bìa bài hát = null (không có ảnh) | 1) Phát bài hát có `coverUrl = null`<br>2) Quan sát Player Artwork Widget | Song: coverUrl = null | Hiển thị placeholder / ảnh mặc định thay cho ảnh bìa; không crash; không hiển thị lỗi load ảnh |
| 36 | Boundary | Tua liên tục (rapid seek) nhiều lần trong thời gian ngắn | 1) Phát một bài hát<br>2) Kéo seekbar liên tục 5-10 lần trong 3 giây | Seek: 0:10 → 0:30 → 1:00 → 0:45 → 1:20... | Không crash; `position` cập nhật về giá trị seek cuối cùng; không có hiện tượng giật/nhảy âm thanh nghiêm trọng |
| 37 | Boundary | Nhấn Play/Pause liên tục (rapid toggle) | 1) Phát một bài hát<br>2) Nhấn nút Pause/Play liên tục 10 lần trong 3 giây | Toggle nhanh giữa Play và Pause | Không crash; trạng thái cuối cùng đúng với lần nhấn cuối; không có âm thanh lặp/vỡ |
| 38 | Boundary | Nhấn Next liên tục khi đang load bài mới | 1) Phát bài đầu trong queue ≥ 10 bài<br>2) Nhấn Next liên tục nhiều lần khi trạng thái `loading` | Queue: 10 bài; nhấn Next nhanh 5 lần | Không crash; không skip quá nhiều bài; chỉ skip đến bài tiếp theo hợp lệ sau khi load xong |
| 39 | Boundary | Queue rỗng (empty queue) khi khởi tạo PlayerState | 1) App mới khởi động, chưa phát bài nào<br>2) Quan sát PlayerState mặc định | queue = []; currentSong = null; status = idle | `PlayerState.status = idle`; `currentSong = null`; PlayerScreen không hiển thị lỗi; Mini Player ẩn hoặc hiển thị trạng thái "Chưa có bài hát" |
| 40 | Boundary | Chạm vào Mini Player khi chưa có bài hát nào đang phát | 1) App mới mở, chưa phát nhạc<br>2) Nếu Mini Player hiển thị, chạm vào | PlayerState.status = idle; currentSong = null | Không crash; có thể không mở PlayerScreen hoặc mở với trạng thái idle |

---

> **Tổng số Test Cases:** 11  
> **Phân bố:** Boundary: 11  
> **Phạm vi:** Repeat mode (all/one/off), long title/artists, null cover, rapid seek/toggle/next, empty queue, mini player idle state
