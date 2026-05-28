# Ondas Mobile — Test Cases: Boundary — Seek, Volume, Queue cơ bản

> **Tính năng**: Boundary cases cho seek, volume, first/last song, duration, queue size  
> **Phiên bản**: 1.0  
> **Ngày**: 22/05/2026  
> **Loại test**: Functional E2E Test Cases (Manual / Exploratory)  
> **File gốc**: `music_streaming_test_case.md` — TCs: 17-29

---

## Bảng Test Cases

| STT (ID) | Nhóm | Tên Test Case | Các bước thực hiện (Steps) | Dữ liệu kiểm thử (Test Data) | Kết quả mong đợi (Expected Result) |
|---|---|---|---|---|---|
| 17 | Boundary | Phát bài hát đầu tiên trong danh sách | 1) Vào danh sách bài hát<br>2) Chạm vào bài hát đầu tiên (index 0) | index = 0; danh sách ≥ 1 bài | Bài hát phát bình thường; nút Previous ở trạng thái disable (không có bài trước) hoặc không phản hồi |
| 18 | Boundary | Phát bài hát cuối cùng trong danh sách | 1) Vào danh sách bài hát<br>2) Chạm vào bài hát cuối cùng (index = n-1) | index = n-1 (bài cuối); danh sách ≥ 1 bài | Bài hát phát bình thường; nút Next ở trạng thái disable hoặc không phản hồi khi chạm (repeat off) |
| 19 | Boundary | Queue chỉ có 1 bài hát — kiểm tra nút Next/Previous | 1) Phát danh sách chỉ có 1 bài<br>2) Nhấn Next<br>3) Nhấn Previous | Queue: [Song A] (chỉ 1 bài) | Nút Next không gây crash; nếu repeat = off, không chuyển bài; nếu repeat = one, phát lại bài hiện tại từ đầu. Nút Previous: nếu position < 3s thì seek về 0; nếu ≥ 3s thì seek về 0 |
| 20 | Boundary | Bài hát có duration = 0 giây | 1) Phát bài hát có `durationSeconds = 0` | Song: durationSeconds = 0 | Bài hát vẫn load, không crash; thanh seekbar hiển thị 0:00 / 0:00; có thể tự động kết thúc ngay và chuyển bài tiếp theo |
| 21 | Boundary | Bài hát có duration cực dài (≥ 3 giờ) | 1) Phát bài hát dài > 3 giờ<br>2) Quan sát seekbar và hiển thị thời gian | Song: durationSeconds = 10800 (3h) | Hiển thị thời gian đúng định dạng (vd: 3:00:00); seekbar hoạt động bình thường; không bị tràn UI |
| 22 | Boundary | Tua (Seek) về vị trí 0:00 | 1) Phát một bài hát<br>2) Kéo seekbar về 0:00 | Song: bất kỳ; seek to Duration.zero | `position = Duration.zero`; nhạc phát lại từ đầu bài |
| 23 | Boundary | Tua (Seek) đến vị trí = duration (cuối bài) | 1) Phát một bài hát<br>2) Kéo seekbar đến hết thanh (vị trí = duration) | Song: duration = 3:00; seek to 3:00 | `position` cập nhật về cuối; bài hát kết thúc; tự động chuyển bài tiếp theo nếu có trong queue |
| 24 | Boundary | Tua (Seek) đến vị trí vượt quá duration | 1) Phát một bài hát<br>2) Thử seek đến vị trí > duration (nếu UI cho phép) | Seek position > duration | Nếu UI chặn không cho kéo quá duration → không xảy ra; nếu không, service tự clamp về duration → bài kết thúc và chuyển bài tiếp |
| 25 | Boundary | Tua (Seek) đến vị trí âm | 1) Phát một bài hát<br>2) Thử seek đến vị trí < 0 (nếu UI cho phép) | Seek position < 0 | UI seekbar không cho phép giá trị âm; nếu có gửi event seek âm, service clamp về 0 hoặc bỏ qua |
| 26 | Boundary | Âm lượng = 0 (Muted) | 1) Phát một bài hát<br>2) Kéo volume về 0<br>3) Quan sát icon và âm thanh | Volume = 0.0 | `PlayerState.volume = 0.0`; không có âm thanh phát ra; icon volume hiển thị trạng thái mute |
| 27 | Boundary | Âm lượng = 1.0 (Tối đa) | 1) Phát một bài hát<br>2) Kéo volume lên tối đa (1.0) | Volume = 1.0 | `PlayerState.volume = 1.0`; âm thanh phát ở mức tối đa của thiết bị |
| 28 | Boundary | Âm lượng vượt quá 1.0 | 1) Thử set volume > 1.0 (nếu có thể) | Volume = 1.5 | Service tự động clamp về 1.0; không crash; không gây méo tiếng |
| 29 | Boundary | Âm lượng < 0 (âm) | 1) Thử set volume < 0 (nếu có thể) | Volume = -0.5 | Service tự động clamp về 0.0; không crash |

---

> **Tổng số Test Cases:** 13  
> **Phân bố:** Boundary: 13  
> **Phạm vi:** First/last song, queue size 1, duration (0/3h), seek boundaries (0/end/over/negative), volume boundaries (0/1/over/negative)
