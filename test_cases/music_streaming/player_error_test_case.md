# Ondas Mobile — Test Cases: Lỗi Phát nhạc (Player Errors)

> **Tính năng**: Lỗi audio URL, mạng, timeout, hardware, background, JWT  
> **Phiên bản**: 1.0  
> **Ngày**: 22/05/2026  
> **Loại test**: Functional E2E Test Cases (Manual / Exploratory)  
> **File gốc**: `music_streaming_test_case.md` — TCs: 41-60

---

## Bảng Test Cases

| STT (ID) | Nhóm | Tên Test Case | Các bước thực hiện (Steps) | Dữ liệu kiểm thử (Test Data) | Kết quả mong đợi (Expected Result) |
|---|---|---|---|---|---|
| 41 | Negative | Phát bài hát với audioUrl không hợp lệ (URL sai) | 1) Phát bài hát có `audioUrl` sai định dạng hoặc không tồn tại<br>2) Quan sát | audioUrl: "https://invalid-url.com/not-found.mp3" | Hiển thị lỗi trên PlayerScreen; `PlayerState.status = error`; `errorMessage` mô tả lỗi; ứng dụng không crash |
| 42 | Negative | Phát bài hát với audioUrl trả về HTTP 404 | 1) Phát bài hát có audioUrl trả về 404 từ server<br>2) Quan sát | audioUrl trả về 404 Not Found | Hiển thị thông báo lỗi: "Không thể phát bài hát này"; `status = error`; ứng dụng không crash |
| 43 | Negative | Phát bài hát với audioUrl trả về HTTP 403 (Forbidden) | 1) Phát bài hát bị chặn quyền truy cập<br>2) Quan sát | audioUrl trả về 403 Forbidden | Hiển thị thông báo lỗi phù hợp; không crash; có thể tự động chuyển bài tiếp theo nếu trong queue |
| 44 | Negative | Phát bài hát với audioUrl trả về HTTP 500 (Server Error) | 1) Phát bài hát khi streaming server gặp lỗi<br>2) Quan sát | audioUrl trả về 500 Internal Server Error | Hiển thị thông báo lỗi: "Máy chủ gặp sự cố, vui lòng thử lại sau"; ứng dụng không crash |
| 45 | Negative | Mất kết nối mạng trong khi đang phát nhạc (đang streaming) | 1) Đang phát một bài hát (streaming)<br>2) Tắt Wi-Fi + Data giữa chừng<br>3) Quan sát | Đang streaming, mất mạng đột ngột | Nhạc dừng; hiển thị thông báo lỗi kết nối; `status = error`; không crash; có nút thử lại (Retry) |
| 46 | Negative | Mất kết nối mạng trước khi phát (khi load audioUrl) | 1) Tắt kết nối mạng<br>2) Chạm vào một bài hát để phát<br>3) Quan sát | Không có kết nối mạng | Hiển thị thông báo: "Không thể kết nối máy chủ. Vui lòng kiểm tra kết nối mạng."; `status = error`; ứng dụng không crash |
| 47 | Negative | Server timeout khi fetch audio stream | 1) Giả lập server timeout khi request audio stream<br>2) Chạm vào một bài hát để phát<br>3) Quan sát | Server timeout > 30s | Hiển thị thông báo lỗi timeout; `status = error`; ứng dụng không crash; có thể thử lại |
| 48 | Negative | Định dạng file audio không được hỗ trợ | 1) Phát bài hát có audioUrl trỏ đến file không phải audio (vd: .exe, .pdf) hoặc codec lạ | audioUrl: file .mkv hoặc codec không hỗ trợ | Hiển thị lỗi: "Định dạng âm thanh không được hỗ trợ"; `status = error`; không crash |
| 49 | Negative | File audio bị hỏng (corrupted) | 1) Phát bài hát có audioUrl trỏ đến file audio bị hỏng<br>2) Quan sát | Audio file corrupted / incomplete | Hiển thị lỗi phát nhạc; `status = error`; không crash; có thể skip sang bài khác |
| 50 | Negative | Ngắt kết nối tai nghe Bluetooth khi đang phát nhạc | 1) Kết nối tai nghe Bluetooth<br>2) Phát một bài hát<br>3) Tắt tai nghe Bluetooth giữa chừng | Đang phát qua Bluetooth, ngắt kết nối | Nhạc tự động dừng (pause); không phát ra loa ngoài trừ khi có cài đặt khác; `status = paused`; không crash |
| 51 | Negative | Ngắt kết nối tai nghe có dây (wired) khi đang phát nhạc | 1) Cắm tai nghe có dây<br>2) Phát một bài hát<br>3) Rút tai nghe giữa chừng | Đang phát qua tai nghe có dây, rút jack | Nhạc tự động dừng (pause); `status = paused`; không crash |
| 52 | Negative | Cuộc gọi đến (incoming call) khi đang phát nhạc | 1) Đang phát nhạc<br>2) Nhận cuộc gọi đến<br>3) Kết thúc cuộc gọi | Đang streaming, có cuộc gọi đến | Nhạc tự động pause khi có cuộc gọi; sau khi kết thúc cuộc gọi, nhạc resume (nếu hệ thống hỗ trợ); không crash |
| 53 | Negative | Ứng dụng bị đưa xuống background khi đang phát nhạc | 1) Đang phát nhạc<br>2) Nhấn nút Home hoặc chuyển sang app khác<br>3) Quan sát sau 30 giây | Đang streaming, app → background | Nhạc vẫn tiếp tục phát trong background (nếu có quyền); Mini Player/notification hiển thị thông tin bài hát; không crash khi quay lại app |
| 54 | Negative | Ứng dụng bị kill (force stop) khi đang phát nhạc | 1) Đang phát nhạc<br>2) Force stop ứng dụng từ system settings<br>3) Mở lại ứng dụng | Đang streaming, force kill app | Ứng dụng dừng hoàn toàn; mở lại app không crash; trạng thái player reset về `idle` |
| 55 | Negative | JWT token hết hạn trong khi đang streaming audio | 1) Đang phát nhạc (audioUrl yêu cầu JWT)<br>2) JWT token hết hạn giữa chừng<br>3) Quan sát | Token hết hạn khi stream đang chạy | Stream có thể tiếp tục nếu đã authenticate; nếu bị ngắt, hiển thị lỗi xác thực; tự động refresh token nếu có interceptor; không crash |
| 56 | Negative | JWT token hết hạn — load bài hát mới thất bại | 1) JWT token đã hết hạn<br>2) Chạm vào một bài hát để phát<br>3) Quan sát | Token expired khi gọi API lấy stream URL | JWT interceptor tự động refresh token; nếu refresh thành công → phát bài hát; nếu refresh thất bại → redirect về Login |
| 57 | Negative | API lấy stream URL bị rate limit (429 Too Many Requests) | 1) Gửi nhiều request phát nhạc liên tục<br>2) Quan sát khi bị rate limit | 429 Too Many Requests từ server | Hiển thị thông báo lỗi phù hợp: "Quá nhiều yêu cầu, vui lòng thử lại sau"; không crash; không spam retry |
| 58 | Negative | Thiết bị sắp hết dung lượng lưu trữ (low storage) | 1) Thiết bị còn < 50MB trống<br>2) Phát một bài hát (streaming)<br>3) Quan sát | Dung lượng trống thấp | Streaming không yêu cầu cache lớn nên vẫn phát được; nếu có cache, hệ thống tự xóa cache cũ; không crash |
| 59 | Negative | Chuyển đổi giữa Wi-Fi và Mobile Data khi đang streaming | 1) Đang phát nhạc qua Wi-Fi<br>2) Tắt Wi-Fi, bật Mobile Data<br>3) Quan sát | Đang streaming, chuyển mạng | Nhạc có thể bị gián đoạn vài giây rồi tiếp tục; `status` có thể chuyển qua `loading` rồi `playing`; không crash; không mất vị trí phát |
| 60 | Negative | Thiết bị ở chế độ im lặng (Silent Mode) | 1) Bật chế độ im lặng / Do Not Disturb<br>2) Phát một bài hát<br>3) Quan sát | Silent mode ON (iOS) hoặc DND (Android) | Nhạc vẫn phát ra loa với âm lượng bình thường (media volume ≠ ringtone volume); nếu mute bằng nút cứng, âm lượng = 0 |

---

> **Tổng số Test Cases:** 20  
> **Phân bố:** Negative: 20  
> **Phạm vi:** Invalid/404/403/500 audioUrl, offline (during/before streaming), timeout, unsupported/corrupted format, hardware (Bluetooth/wired/call), background/kill, JWT, rate limit, storage, network switch, silent mode
