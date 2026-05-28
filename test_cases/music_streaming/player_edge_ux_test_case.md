# Ondas Mobile — Test Cases: Edge Cases & UX Player

> **Tính năng**: Validation input, injection, UX (multi-entry, swipe, rotation, loading), race condition  
> **Phiên bản**: 1.0  
> **Ngày**: 22/05/2026  
> **Loại test**: Functional E2E Test Cases (Manual / Exploratory)  
> **File gốc**: `music_streaming_test_case.md` — TCs: 61-72

---

## Bảng Test Cases

| STT (ID) | Nhóm | Tên Test Case | Các bước thực hiện (Steps) | Dữ liệu kiểm thử (Test Data) | Kết quả mong đợi (Expected Result) |
|---|---|---|---|---|---|
| 61 | Negative | Phát bài hát có trường `id` rỗng hoặc null | 1) Thử phát bài hát không có `id` | Song: id = "" hoặc id = null | Hệ thống từ chối phát hoặc báo lỗi validation; không crash; `PlaySongRequested` không được dispatch nếu dữ liệu không hợp lệ |
| 62 | Negative | Phát bài hát có trường `audioUrl` rỗng hoặc null | 1) Thử phát bài hát không có `audioUrl` | Song: audioUrl = "" hoặc audioUrl = null | Hiển thị lỗi: "Không tìm thấy nguồn phát cho bài hát này"; `status = error`; không crash |
| 63 | Negative | Tấn công injection vào audioUrl | 1) Bài hát có audioUrl chứa các ký tự đặc biệt: `file:///etc/passwd`, `javascript:alert(1)`, `../../etc/shadow` | audioUrl = "javascript:alert(1)" | Hệ thống chỉ chấp nhận URL hợp lệ (http/https); các URL độc hại bị từ chối hoặc không gây hại; không crash |
| 64 | Negative | Gửi event PlaySongRequested với songs rỗng | 1) Dispatch `PlaySongRequested(songs: [], index: 0)` | songs = []; index = 0 | BLoC từ chối hoặc báo lỗi; không crash; không gọi service |
| 65 | Negative | Gửi event PlaySongRequested với index ngoài phạm vi (out of bounds) | 1) Dispatch `PlaySongRequested(songs: [A, B], index: 5)` | songs.length = 2; index = 5 (> length-1) | BLoC clamp index về 0 hoặc báo lỗi; không crash; ứng dụng xử lý gracefully |
| 66 | Negative | Gửi event PlaySongRequested với index âm | 1) Dispatch `PlaySongRequested(songs: [A, B], index: -1)` | songs.length = 2; index = -1 | BLoC clamp index về 0 hoặc báo lỗi; không crash |
| 67 | Boundary / UX | Mở PlayerScreen từ nhiều điểm vào khác nhau (Search, Home, Favorites, Playlist...) | 1) Phát bài từ Search<br>2) Quay lại, phát bài từ Favorites<br>3) Quay lại, phát bài từ Playlist | Phát từ 3 nguồn khác nhau liên tiếp | Mỗi lần phát, PlayerState cập nhật đúng queue và currentSong tương ứng; không bị lẫn lộn queue cũ |
| 68 | Boundary / UX | Vuốt xuống (swipe down) để đóng PlayerScreen | 1) Mở PlayerScreen<br>2) Vuốt từ trên xuống để đóng<br>3) Quan sát | PlayerScreen đang mở | PlayerScreen đóng (slide-down transition); nhạc vẫn tiếp tục phát; Mini Player hiển thị ở bottom |
| 69 | Boundary / UX | Xoay màn hình (portrait ↔ landscape) khi đang phát nhạc | 1) Đang phát nhạc<br>2) Xoay màn hình từ dọc sang ngang<br>3) Xoay lại dọc | Đang streaming, xoay màn hình | UI tự động thích ứng (responsive); nhạc không bị gián đoạn; `position` không bị reset; không crash |
| 70 | Boundary / UX | Khi bài hát đang load, UI hiển thị trạng thái loading | 1) Phát một bài hát<br>2) Ngay khi nhấn play, quan sát UI trước khi nhạc bắt đầu | Mạng bình thường hoặc chậm | Hiển thị CircularProgressIndicator hoặc shimmer; title/artist hiển thị đúng bài đang load; không hiển thị màn hình trắng |
| 71 | Negative | Nhiều instance BLoC cùng phát nhạc — race condition | 1) Mở app trên 2 màn hình/vùng khác nhau (nếu có thể)<br>2) Phát bài A từ màn 1, nhanh chóng phát bài B từ màn 2 | 2 nguồn PlaySongRequested gần như đồng thời | Chỉ 1 bài hát được phát; queue cuối cùng giữ bài phát sau; không crash; không phát 2 bài cùng lúc |
| 72 | Negative | Ứng dụng crash khi đang streaming → khởi động lại app | 1) Phát một bài hát<br>2) Giả lập crash (nếu test được)<br>3) Mở lại app | App crash khi đang streaming | App khởi động lại bình thường; player reset về idle; không tự động phát lại bài cũ |

---

> **Tổng số Test Cases:** 12  
> **Phân bố:** Negative: 8 | Boundary/UX: 4  
> **Phạm vi:** Input validation (empty id/audioUrl, empty songs, index OOB), injection, multi-entry navigation, swipe close, rotation, loading state, race condition, crash recovery
