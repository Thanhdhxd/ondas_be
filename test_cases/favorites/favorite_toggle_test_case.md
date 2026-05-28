# Ondas Mobile — Test Cases: Nút Yêu thích (Favorite Toggle)

> **Tính năng**: Thêm/Xóa bài hát yêu thích qua nút trái tim (FavoriteButtonWidget)  
> **Phiên bản**: 1.0  
> **Ngày**: 22/05/2026  
> **Loại test**: Functional E2E Test Cases (Manual / Exploratory)  
> **File gốc**: `favorites_test_case.md` — TCs: 01-03, 11-16, 23, 59-60

---

## Thành phần liên quan

| Thành phần | Mô tả |
|---|---|
| `FavoriteButtonWidget` | Nút trái tim độc lập — mỗi nút tự tạo `FavoriteToggleBloc` riêng, tự kiểm tra trạng thái yêu thích khi mount |
| `FavoriteToggleBloc` | Quản lý toggle thêm/xóa — optimistic update + revert khi fail |
| Các UseCase | `AddFavoriteUseCase`, `RemoveFavoriteUseCase`, `CheckFavoriteStatusUseCase` |

---

## Bảng Test Cases

| STT (ID) | Nhóm | Tên Test Case | Các bước thực hiện (Steps) | Dữ liệu kiểm thử (Test Data) | Kết quả mong đợi (Expected Result) |
|---|---|---|---|---|---|
| 01 | Happy Path | Thêm bài hát vào danh sách yêu thích — từ PlayerScreen | 1) Phát một bài hát chưa có trong favorites<br>2) Mở PlayerScreen<br>3) Nhấn nút trái tim (FavoriteButtonWidget) đang ở trạng thái unchecked<br>4) Quan sát icon | Song: id="song_01", chưa được yêu thích | Icon chuyển từ `favorite_border` → `favorite` (màu đỏ); API `addFavorite(song_01)` được gọi thành công; trạng thái được lưu trên server |
| 02 | Happy Path | Xóa bài hát khỏi danh sách yêu thích — từ PlayerScreen | 1) Phát một bài hát đã có trong favorites<br>2) Mở PlayerScreen<br>3) Nhấn nút trái tim đang ở trạng thái checked (đỏ)<br>4) Quan sát icon | Song: id="song_01", đã được yêu thích | Icon chuyển từ `favorite` (đỏ) → `favorite_border` (trắng); API `removeFavorite(song_01)` được gọi thành công; bài hát bị xóa khỏi danh sách yêu thích |
| 03 | Happy Path | Thêm bài hát vào yêu thích — từ màn hình SearchResults | 1) Vào Search → tìm kiếm một bài hát<br>2) Trong danh sách kết quả, nhấn nút trái tim của một bài hát chưa được yêu thích<br>3) Quan sát icon | Song: id="song_search_01", chưa yêu thích | Icon chuyển sang trạng thái checked (màu đỏ); bài hát được thêm vào favorites; nếu vào FavoritesScreen, bài hát xuất hiện trong danh sách |
| 11 | Happy Path | Kiểm tra trạng thái yêu thích hiển thị đúng trên nhiều màn hình | 1) Vào Search, tìm một bài hát<br>2) Thêm bài hát vào yêu thích (nhấn tim)<br>3) Quay lại Home, vào lại Search cùng bài hát đó<br>4) Quan sát icon trái tim | Song: id="song_multi"; thêm yêu thích từ Search | Icon trái tim hiển thị checked (đỏ) ở mọi nơi: Search results, FavoritesScreen, PlayerScreen (nếu phát bài này); trạng thái đồng bộ |
| 12 | Happy Path | Nút trái tim hiển thị trạng thái loading khi đang kiểm tra | 1) Mở màn hình có FavoriteButtonWidget<br>2) Quan sát ngay khi widget mount (trước khi API check status trả về)<br>3) Đợi API trả về | Song: id="song_check"; mạng chậm hoặc bình thường | Trong lúc chờ API: hiển thị CircularProgressIndicator nhỏ thay cho icon trái tim; không hiển thị icon sai trạng thái; sau khi API trả về: hiển thị đúng trạng thái checked/unchecked |
| 13 | Happy Path | Nhiều nút trái tim trên cùng một màn hình hoạt động độc lập | 1) Vào SearchResults có ≥ 5 bài hát<br>2) Nhấn tim cho bài 1 và bài 3<br>3) Quan sát tất cả icon | 5 bài hát; mỗi bài có FavoriteButtonWidget riêng | Chỉ bài 1 và bài 3 hiển thị checked (đỏ); các bài còn lại unchecked (trắng); mỗi nút có `FavoriteToggleBloc` riêng, không ảnh hưởng lẫn nhau |
| 14 | Boundary | Thêm bài hát đã có trong danh sách yêu thích (toggle lần 2) | 1) Bài hát đang ở trạng thái checked (đã yêu thích)<br>2) Nhấn nút trái tim lần nữa<br>3) Quan sát | Song: id="song_toggle", đang checked | Icon chuyển về unchecked (trắng); bài hát bị xóa khỏi favorites; API `removeFavorite` được gọi; không có lỗi trùng lặp |
| 15 | Boundary | Xóa bài hát chưa có trong danh sách yêu thích (toggle từ unchecked) | 1) Bài hát đang ở trạng thái unchecked<br>2) Nhấn nút trái tim<br>3) Quan sát | Song: id="song_not_fav", đang unchecked | Icon chuyển sang checked (đỏ); API `addFavorite` được gọi; bài hát được thêm vào favorites |
| 16 | Boundary | Nhấn toggle nút trái tim liên tục 5 lần trong 2 giây (rapid toggle) | 1) Mở PlayerScreen với bài hát<br>2) Nhấn nút trái tim liên tục 5 lần trong 2 giây<br>3) Dừng lại, đợi API hoàn thành | Song: id="song_rapid"; bắt đầu ở unchecked | Optimistic update thực hiện mỗi lần nhấn; sau khi dừng, trạng thái cuối cùng khớp với API response cuối cùng; không bị duplicate request quá nhiều; không crash; UI không bị giật |
| 23 | Boundary | Toggle trạng thái yêu thích khi đang ở màn hình khác (đồng bộ trạng thái) | 1) Mở PlayerScreen — bài hát đang checked<br>2) Vào FavoritesScreen — vuốt xóa bài hát đó<br>3) Quay lại PlayerScreen<br>4) Quan sát icon trái tim | Song: id="song_sync" | Khi quay lại PlayerScreen, `FavoriteButtonWidget` được mount lại với `ValueKey(songId)` → `FavoriteToggleBloc` mới được tạo → gọi `FavoriteStatusCheckRequested` → trạng thái được cập nhật thành unchecked (trắng); đồng bộ đúng |
| 59 | Boundary / UX | Nút trái tim hiển thị đúng màu sắc theo trạng thái | 1) Quan sát nút trái tim khi unchecked<br>2) Quan sát nút trái tim khi checked<br>3) Quan sát nút trái tim khi loading | Trạng thái: unchecked → checked → loading | Unchecked: icon `favorite_border`, màu trắng (`AppColors.silver` nếu có `inactiveColor`); Checked: icon `favorite`, màu đỏ (`Colors.redAccent`); Loading: `CircularProgressIndicator` nhỏ, màu primary |
| 60 | Negative | Nút trái tim bị dispose khi widget unmount — không leak memory | 1) Vào màn hình có FavoriteButtonWidget<br>2) Rời khỏi màn hình (pop)<br>3) Quan sát | Widget unmount | `FavoriteToggleBloc` được dispose tự động bởi `BlocProvider`; không còn lắng nghe sự kiện; không leak memory; không crash khi quay lại |

---

> **Tổng số Test Cases:** 12  
> **Phân bố:** Happy Path: 5 | Boundary: 5 | Boundary/UX: 1 | Negative: 1
