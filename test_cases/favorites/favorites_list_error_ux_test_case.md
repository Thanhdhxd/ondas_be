# Ondas Mobile — Test Cases: Danh sách Yêu thích — Xử lý lỗi & UX

> **Tính năng**: Xử lý lỗi danh sách yêu thích (offline, server error, JWT) và UX (theme, rotation, navigation)  
> **Phiên bản**: 1.0  
> **Ngày**: 22/05/2026  
> **Loại test**: Functional E2E Test Cases (Manual / Exploratory)  
> **File gốc**: `favorites_test_case.md` — TCs: 28-30, 33-35, 37, 46, 48, 50-52, 54-58

---

## Thành phần liên quan

| Thành phần | Mô tả |
|---|---|
| `FavoritesScreen` | Màn hình danh sách yêu thích — tạo `FavoritesBloc` và gọi `FavoritesListRequested` khi vào |
| `FavoritesListWidget` | Widget danh sách yêu thích — hỗ trợ loading, error, empty, scroll, pull-to-refresh, swipe-to-dismiss |
| `FavoritesBloc` | Quản lý danh sách yêu thích — phân trang (`_pageSize = 20`), loadMore, optimistic remove |
| Route | `/favorites` → `FavoritesScreen` |

---

## Bảng Test Cases

| STT (ID) | Nhóm | Tên Test Case | Các bước thực hiện (Steps) | Dữ liệu kiểm thử (Test Data) | Kết quả mong đợi (Expected Result) |
|---|---|---|---|---|---|
| 28 | Negative | Vuốt xóa bài hát khỏi danh sách yêu thích khi mất kết nối mạng | 1) Vào FavoritesScreen khi có mạng<br>2) Tắt kết nối mạng<br>3) Vuốt trái để xóa một bài hát<br>4) Quan sát | Song: id="song_swipe_offline"; không có mạng | Bài hát biến mất khỏi danh sách (optimistic remove) → API fail → revert: bài hát xuất hiện trở lại; `FavoritesBloc` emit lại state cũ; không crash |
| 29 | Negative | Tải danh sách yêu thích khi mất kết nối mạng | 1) Tắt kết nối mạng<br>2) Điều hướng đến `/favorites`<br>3) Quan sát | Không có kết nối mạng | Hiển thị `FavoritesListError` với message lỗi kết nối; hiển thị nút "Thử lại" (Retry); không crash; không hiển thị list rỗng |
| 30 | Negative | Kéo để làm mới (Pull-to-refresh) danh sách yêu thích khi mất mạng | 1) Vào FavoritesScreen khi đang có mạng<br>2) Tắt mạng<br>3) Kéo xuống để refresh<br>4) Quan sát | Không có mạng khi refresh | Refresh indicator xuất hiện rồi biến mất; danh sách cũ vẫn hiển thị; hiển thị `FavoritesListError` hoặc SnackBar lỗi; không xóa danh sách cũ |
| 33 | Negative | Server trả về lỗi 500 khi tải danh sách yêu thích | 1) Giả lập server lỗi 500<br>2) Điều hướng đến `/favorites`<br>3) Quan sát | Server 500 | Hiển thị `FavoritesListError` với message lỗi; hiển thị nút "Thử lại"; nhấn "Thử lại" dispatch `FavoritesListRequested`; không crash |
| 34 | Negative | Server trả về lỗi 500 khi tải thêm (LoadMore) | 1) FavoritesScreen đang hiển thị 20 bài (hasMore = true)<br>2) Giả lập server lỗi 500 cho page=1<br>3) Kéo xuống cuối danh sách<br>4) Quan sát | page=1 trả về 500 | `FavoritesLoadMoreRequested` dispatch; API fail; `isLoadingMore` revert về false; danh sách giữ nguyên 20 bài; không crash; không mất dữ liệu đã có |
| 35 | Negative | Server timeout khi tải danh sách yêu thích | 1) Giả lập server timeout (>30s)<br>2) Điều hướng đến `/favorites`<br>3) Quan sát | Server timeout | Hiển thị loading indicator → timeout → hiển thị `FavoritesListError` với message lỗi + nút Retry; không crash |
| 37 | Negative | JWT token hết hạn khi tải danh sách yêu thích | 1) JWT token đã hết hạn<br>2) Điều hướng đến `/favorites`<br>3) Quan sát | Token expired | JWT interceptor tự động refresh token; nếu refresh thành công → load danh sách bình thường; nếu refresh thất bại → redirect về Login |
| 46 | Negative | Nhấn nút "Thử lại" (Retry) khi danh sách yêu thích đang ở trạng thái lỗi | 1) Vào FavoritesScreen khi server lỗi → hiển thị ErrorView<br>2) Server đã hoạt động trở lại<br>3) Nhấn nút "Thử lại"<br>4) Quan sát | Server đã recover | `FavoritesListRequested` được dispatch lại; danh sách yêu thích tải thành công; hiển thị danh sách bài hát; không còn ErrorView |
| 48 | Negative | Ứng dụng bị kill khi đang tải danh sách yêu thích | 1) Điều hướng đến `/favorites`<br>2) Force kill app khi đang loading<br>3) Mở lại app, vào lại `/favorites` | Đang gọi API getFavorites; force kill | App khởi động lại bình thường; FavoritesScreen tải lại danh sách từ đầu; không crash; không cache data lỗi |
| 50 | Negative | Kéo để làm mới (Pull-to-refresh) khi danh sách đang loading | 1) Vào FavoritesScreen<br>2) Khi danh sách đang loading, kéo xuống để refresh<br>3) Quan sát | Đang loading | Refresh indicator không phản hồi hoặc bị chặn; không gửi duplicate `FavoritesListRequested`; không crash |
| 51 | Negative | Tải thêm (LoadMore) khi đang loading more (chặn duplicate) | 1) Vào FavoritesScreen với 25+ bài<br>2) Kéo xuống cuối → loadMore bắt đầu<br>3) Kéo lên rồi kéo xuống lại ngay lập tức<br>4) Quan sát | isLoadingMore = true | `FavoritesBloc._onLoadMoreRequested` kiểm tra `current.isLoadingMore` → return sớm; không gửi duplicate request; không crash |
| 52 | Negative | Danh sách yêu thích bị xóa toàn bộ bởi admin/backend | 1) Vào FavoritesScreen đang hiển thị danh sách<br>2) Admin xóa toàn bộ favorites của user<br>3) Pull-to-refresh<br>4) Quan sát | Tất cả favorites bị xóa từ server | Sau refresh: danh sách rỗng → hiển thị empty state; không crash; `hasMore = false` |
| 54 | Negative | Xóa bài hát khỏi favorites khi state hiện tại không phải FavoritesListLoaded | 1) Đang ở trạng thái FavoritesListLoading hoặc FavoritesListError<br>2) Dispatch FavoriteRemovedFromList<br>3) Quan sát | State hiện tại không phải FavoritesListLoaded | `_onRemovedFromList` kiểm tra `if (current is! FavoritesListLoaded) return;` → không làm gì; không crash |
| 55 | Boundary / UX | Chuyển đổi giữa Light Mode và Dark Mode khi đang ở FavoritesScreen | 1) Vào FavoritesScreen ở Light Mode<br>2) Chuyển sang Dark Mode (hệ thống)<br>3) Quan sát UI | Đổi theme hệ thống | UI cập nhật màu sắc theo theme mới; danh sách không bị reload; trạng thái yêu thích không bị ảnh hưởng; không crash |
| 56 | Boundary / UX | Xoay màn hình khi đang ở FavoritesScreen | 1) Vào FavoritesScreen<br>2) Xoay màn hình từ dọc sang ngang<br>3) Xoay lại dọc | Xoay màn hình | UI tự động thích ứng (responsive); danh sách không bị reload; scroll position được giữ; không crash |
| 57 | Boundary / UX | Nhấn nút Back từ FavoritesScreen | 1) Điều hướng đến `/favorites` từ tab profile hoặc từ PlayerScreen<br>2) Nhấn nút Back (mũi tên trái trên AppBar)<br>3) Quan sát | N/A | Gọi `Navigator.of(context).pop()`; quay lại màn hình trước đó; FavoritesBloc bị dispose; không leak memory |
| 58 | Boundary / UX | FavoritesScreen hiển thị loading indicator khi đang fetch dữ liệu | 1) Điều hướng đến `/favorites`<br>2) Quan sát ngay khi màn hình mở (trước khi API trả về) | Mạng bình thường hoặc chậm | Hiển thị `CircularProgressIndicator` (màu `AppColors.spotifyGreen`) ở giữa màn hình; không hiển thị màn trắng; sau khi load xong → hiển thị danh sách hoặc empty state |

---

> **Tổng số Test Cases:** 17  
> **Phân bố:** Negative: 13 | Boundary/UX: 4  
> **Phạm vi:** Offline list/refresh/swipe, server errors (500, timeout), JWT expired, retry, app kill, duplicate prevention, admin actions, theme, rotation, navigation, loading states
