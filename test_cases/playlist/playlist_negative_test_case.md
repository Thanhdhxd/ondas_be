# Ondas Mobile — Test Cases: Lỗi Playlist (Network, Server, Auth)

> **Tính năng**: Xử lý lỗi mạng, server, JWT, trùng lặp, quyền truy cập khi thao tác playlist  
> **Phiên bản**: 1.0  
> **Ngày**: 22/05/2026  
> **Loại test**: Functional E2E Test Cases (Manual / Exploratory)  
> **File gốc**: `playlist_management_test_case.md` — TCs: 29-44

---

## Bảng Test Cases

| STT (ID) | Nhóm | Tên Test Case | Các bước thực hiện (Steps) | Dữ liệu kiểm thử (Test Data) | Kết quả mong đợi (Expected Result) |
|---|---|---|---|---|---|
| 29 | Negative | Tạo playlist với tên trùng với playlist đã có | 1) Đã có playlist tên "Chill Vibes"<br>2) Tạo playlist mới cũng tên "Chill Vibes"<br>3) Quan sát | Tên trùng: "Chill Vibes" | Cho phép tạo (tên playlist có thể trùng); 2 playlist cùng tên tồn tại song song (phân biệt bằng id); không lỗi |
| 30 | Negative | Thêm bài hát đã có sẵn vào playlist (trùng lặp) | 1) Bài hát "song_01" đã có trong playlist<br>2) Thử thêm lại "song_01" qua SaveToPlaylistBottomSheet | Playlist đã chứa "song_01"; toggle đang checked | Toggle đã ở trạng thái checked; nếu nhấn bỏ chọn → bài hát bị xóa; không thể thêm trùng; server có thể trả về lỗi 409 Conflict |
| 31 | Negative | Xóa bài hát không tồn tại trong playlist | 1) Gửi request xóa bài hát "song_999" khỏi playlist (API test) | Song id: "song_999" (không có trong playlist) | Server trả về lỗi 404 hoặc success (idempotent); UI không crash |
| 32 | Negative | Xóa playlist không tồn tại | 1) Gửi request xóa playlist với id không tồn tại | Playlist id: "pl_nonexistent" | Server trả về lỗi 404; hiển thị thông báo lỗi: "Playlist không tồn tại"; không crash |
| 33 | Negative | Truy cập playlist đã bị xóa bởi người khác | 1) Mở playlist "pl_01" đang hiển thị<br>2) Người dùng khác (hoặc thiết bị khác) xóa playlist này<br>3) Refresh hoặc tương tác với playlist | Playlist "pl_01" đã bị xóa khỏi server | Khi refresh: playlist biến mất khỏi Library; nếu đang ở màn detail, hiển thị lỗi "Playlist không tồn tại" và quay lại Library |
| 34 | Negative | Thêm bài hát vào playlist của người dùng khác (private playlist) | 1) Lấy id playlist của người dùng khác (private)<br>2) Gửi request thêm bài hát | Playlist id của user khác | Server trả về lỗi 403 Forbidden; hiển thị thông báo lỗi phù hợp; không crash |
| 35 | Negative | Mất kết nối mạng khi tạo playlist | 1) Tắt kết nối mạng<br>2) Mở dialog tạo playlist<br>3) Nhập tên và nhấn "Tạo" | Tên: "New Playlist"; không có mạng | Hiển thị thông báo lỗi: "Không thể kết nối máy chủ. Vui lòng kiểm tra kết nối mạng."; playlist không được tạo; dialog đóng và hiển thị lỗi |
| 36 | Negative | Mất kết nối mạng khi thêm bài hát vào playlist | 1) Mở SaveToPlaylistBottomSheet (đã load danh sách)<br>2) Tắt mạng<br>3) Chọn một playlist để thêm bài hát | Song: id="song_01"; Playlist: id="pl_01"; không có mạng | Toggle thực hiện optimistic update (checked) → API fail → revert về trạng thái cũ (unchecked); hiển thị SnackBar lỗi |
| 37 | Negative | Mất kết nối mạng khi xóa bài hát khỏi playlist | 1) Mở SaveToPlaylistBottomSheet<br>2) Tắt mạng<br>3) Bỏ chọn một playlist đang chứa bài hát | Song: id="song_01"; Playlist đang checked; không có mạng | Toggle thực hiện optimistic update (unchecked) → API fail → revert về checked; hiển thị SnackBar lỗi |
| 38 | Negative | Mất kết nối mạng khi xóa playlist | 1) Mở playlist<br>2) Tắt mạng<br>3) Nhấn "Xóa playlist" và xác nhận | Playlist: "To Delete"; không có mạng | Hiển thị thông báo lỗi kết nối; playlist không bị xóa; vẫn hiển thị trong Library |
| 39 | Negative | Server timeout khi load danh sách playlist | 1) Giả lập server timeout (>30s)<br>2) Mở Library hoặc SaveToPlaylistBottomSheet<br>3) Quan sát | Server không phản hồi | Hiển thị loading indicator → timeout → hiển thị thông báo lỗi + nút Retry; không crash |
| 40 | Negative | Server timeout khi tạo playlist | 1) Giả lập server timeout<br>2) Nhập tên playlist và nhấn "Tạo" | Server timeout | Hiển thị thông báo lỗi timeout; playlist không được tạo; người dùng có thể thử lại |
| 41 | Negative | Server trả về lỗi 500 khi tạo playlist | 1) Giả lập server lỗi 500<br>2) Nhập tên playlist và nhấn "Tạo" | Server 500 Internal Server Error | Hiển thị SnackBar đỏ với thông báo lỗi server; ứng dụng không crash; người dùng có thể thử lại |
| 42 | Negative | Server trả về lỗi 500 khi load chi tiết playlist | 1) Giả lập server lỗi 500<br>2) Mở một playlist | Server 500 | Hiển thị màn hình lỗi với message và nút "Thử lại"; không crash |
| 43 | Negative | JWT token hết hạn khi thêm bài hát vào playlist | 1) JWT token hết hạn<br>2) Thêm bài hát vào playlist<br>3) Quan sát | Token expired | JWT interceptor tự động refresh token; nếu refresh thành công → thêm thành công; nếu refresh thất bại → redirect về Login |
| 44 | Negative | JWT token hết hạn khi tạo playlist mới | 1) JWT token hết hạn<br>2) Tạo playlist mới<br>3) Quan sát | Token expired | Tương tự trên: auto-refresh hoặc redirect về Login |

---

> **Tổng số Test Cases:** 16  
> **Phân bố:** Negative: 16  
> **Phạm vi:** Duplicate name/song, nonexistent playlist/song, deleted by other, private playlist (403), offline (create/add/remove/delete), timeout, server 500, JWT expired
