# Ondas Mobile — Test Cases: Danh sách Yêu thích (Favorites List)

> **Tính năng**: Hiển thị, phân trang, và tương tác trên danh sách bài hát yêu thích  
> **Phiên bản**: 1.0  
> **Ngày**: 22/05/2026  
> **Loại test**: Functional E2E Test Cases (Manual / Exploratory)  
> **File gốc**: `favorites_test_case.md` — TCs: 04-10, 17-22, 24-25

---

## Thành phần liên quan

| Thành phần | Mô tả |
|---|---|
| `FavoritesScreen` | Màn hình danh sách yêu thích — tạo `FavoritesBloc` và gọi `FavoritesListRequested` khi vào |
| `FavoritesListWidget` | Widget danh sách yêu thích (embed được) — hỗ trợ loading, error, empty, scroll, pull-to-refresh, swipe-to-dismiss |
| `FavoritesBloc` | Quản lý danh sách yêu thích — phân trang (`_pageSize = 20`), loadMore, optimistic remove |
| Các UseCase | `GetFavoritesUseCase`, `RemoveFavoriteUseCase` |
| Route | `/favorites` → `FavoritesScreen` |

---

## Bảng Test Cases

| STT (ID) | Nhóm | Tên Test Case | Các bước thực hiện (Steps) | Dữ liệu kiểm thử (Test Data) | Kết quả mong đợi (Expected Result) |
|---|---|---|---|---|---|
| 04 | Happy Path | Xem danh sách bài hát yêu thích (FavoritesScreen) | 1) Đăng nhập tài khoản có ≥ 3 bài hát yêu thích<br>2) Điều hướng đến `/favorites`<br>3) Quan sát màn hình | User có 3 bài yêu thích: "Song A", "Song B", "Song C" | Hiển thị danh sách 3 bài hát với đầy đủ: ảnh bìa (coverUrl), tiêu đề (title), tên nghệ sĩ (artistDisplay), nút trái tim (checked, màu đỏ); sắp xếp theo thời gian thêm gần nhất |
| 05 | Happy Path | Danh sách yêu thích hiển thị empty state khi chưa có bài nào | 1) Đăng nhập tài khoản chưa có bài hát yêu thích nào<br>2) Điều hướng đến `/favorites`<br>3) Quan sát màn hình | User có 0 bài yêu thích | Hiển thị empty state: icon/illustration + text "Chưa có bài hát yêu thích nào" (hoặc tương tự); không hiển thị list rỗng; không crash |
| 06 | Happy Path | Kéo để làm mới (Pull-to-refresh) danh sách yêu thích | 1) Vào FavoritesScreen đang hiển thị danh sách<br>2) Thêm một bài hát yêu thích từ thiết bị khác<br>3) Kéo xuống để refresh<br>4) Quan sát | Có bài hát mới được thêm từ thiết bị khác | Danh sách được tải lại; bài hát mới từ server xuất hiện; `FavoritesListRequested` được dispatch; loading indicator hiển thị trong lúc refresh |
| 07 | Happy Path | Tải thêm (Load More / Infinite Scroll) khi kéo xuống cuối danh sách | 1) Tài khoản có ≥ 25 bài hát yêu thích<br>2) Vào FavoritesScreen<br>3) Kéo xuống cuối danh sách (tới bài thứ 20)<br>4) Quan sát | User có 25+ bài; pageSize = 20 | Khi kéo đến gần cuối: hiển thị loading indicator ở bottom; `FavoritesLoadMoreRequested` được dispatch; page=1 được tải; danh sách nối thêm 5+ bài; `hasMore` cập nhật đúng |
| 08 | Happy Path | Tải thêm khi đã hết dữ liệu (hasMore = false) | 1) Tài khoản có đúng 20 bài yêu thích<br>2) Vào FavoritesScreen<br>3) Kéo xuống cuối danh sách<br>4) Quan sát | User có đúng 20 bài; pageSize = 20 | Không hiển thị loading indicator; không dispatch `FavoritesLoadMoreRequested`; không có lỗi; danh sách hiển thị bình thường |
| 09 | Happy Path | Vuốt trái để xóa bài hát khỏi danh sách yêu thích (Swipe-to-dismiss) | 1) Vào FavoritesScreen với ≥ 2 bài<br>2) Vuốt trái (swipe left) một bài hát<br>3) Quan sát danh sách và icon trái tim | Favorites: [Song A, Song B]; vuốt xóa Song A | Bài hát biến mất khỏi danh sách (optimistic remove); `FavoriteRemovedFromList(songA_id)` được dispatch; API `removeFavorite` được gọi; nếu vào lại FavoritesScreen, bài hát không còn trong danh sách |
| 10 | Happy Path | Nhấn vào bài hát trong danh sách yêu thích để phát nhạc | 1) Vào FavoritesScreen<br>2) Chạm vào bài hát thứ 2 trong danh sách<br>3) Quan sát PlayerScreen | Favorites: 5 bài; chạm bài thứ 2 | `PlayerBloc` dispatch `PlaySongRequested` với songs = toàn bộ favorites list, index = 1; PlayerScreen mở ra; bài hát thứ 2 bắt đầu phát |
| 17 | Boundary | Danh sách yêu thích có đúng 1 bài hát | 1) Tài khoản có đúng 1 bài yêu thích<br>2) Vào FavoritesScreen<br>3) Vuốt xóa bài hát đó | Favorites: [Song A] (1 bài) | Hiển thị 1 bài hát bình thường; sau khi vuốt xóa: danh sách rỗng → hiển thị empty state; không crash |
| 18 | Boundary | Danh sách yêu thích có đúng 20 bài (đúng 1 page) | 1) Tài khoản có đúng 20 bài yêu thích<br>2) Vào FavoritesScreen<br>3) Kéo xuống cuối danh sách | User có 20 bài; pageSize = 20 | Hiển thị đủ 20 bài; không hiển thị loading indicator ở cuối; `hasMore = false`; không gọi loadMore |
| 19 | Boundary | Danh sách yêu thích có đúng 21 bài (vượt 1 page) | 1) Tài khoản có đúng 21 bài yêu thích<br>2) Vào FavoritesScreen<br>3) Kéo xuống cuối danh sách | User có 21 bài; pageSize = 20 | Hiển thị 20 bài đầu; kéo cuối → loadMore được gọi → hiển thị thêm 1 bài; `hasMore = false` sau khi load xong |
| 20 | Boundary | Bài hát có tiêu đề rất dài (≥ 100 ký tự) trong danh sách yêu thích | 1) Thêm bài hát có title > 100 ký tự vào favorites<br>2) Vào FavoritesScreen<br>3) Quan sát hiển thị | Title: "A Very Long Song Title That Exceeds Normal Display Length And Goes On And On And On And On..." | Tiêu đề hiển thị với `TextOverflow.ellipsis`; không bị tràn layout; không crash; vẫn hiển thị đủ artistDisplay |
| 21 | Boundary | Bài hát có tên nghệ sĩ gồm nhiều nghệ sĩ (≥ 5 artists) trong danh sách yêu thích | 1) Thêm bài hát có 5+ nghệ sĩ vào favorites<br>2) Vào FavoritesScreen<br>3) Quan sát artistDisplay | artistNames: ["Artist A", "Artist B", "Artist C", "Artist D", "Artist E"] | Hiển thị đúng định dạng "A, B, C, D, E"; dùng ellipsis nếu quá dài; không crash |
| 22 | Boundary | Bài hát trong danh sách yêu thích không có ảnh bìa (coverUrl = null) | 1) Thêm bài hát có coverUrl = null vào favorites<br>2) Vào FavoritesScreen<br>3) Quan sát ảnh bìa | Song: coverUrl = null | Hiển thị placeholder (icon music_note + nền xám `AppColors.darkCard`) thay cho ảnh bìa; không crash; không hiển thị lỗi load ảnh |
| 24 | Boundary | Cuộn nhanh danh sách yêu thích 100+ bài (performance) | 1) Tài khoản có ≥ 100 bài yêu thích<br>2) Cuộn nhanh từ đầu xuống cuối<br>3) Quan sát UI | 100+ bài; pageSize = 20 | UI cuộn mượt, không giật lag; loadMore được gọi khi chạm ngưỡng; các bài hát hiển thị đúng; không crash |
| 25 | Boundary | Thêm bài hát vào yêu thích khi đang ở FavoritesScreen (real-time cập nhật) | 1) Vào FavoritesScreen đang hiển thị<br>2) Dùng một thiết bị khác thêm bài hát mới vào favorites<br>3) Pull-to-refresh<br>4) Quan sát | Bài hát mới được thêm từ thiết bị khác | Sau refresh, bài hát mới xuất hiện trong danh sách; tổng số bài tăng lên; thứ tự đúng (mới nhất lên đầu) |

---

> **Tổng số Test Cases:** 15  
> **Phân bố:** Happy Path: 7 | Boundary: 8  
> **Phạm vi:** Hiển thị danh sách, empty state, pull-to-refresh, load more, swipe-to-dismiss, phát nhạc từ list, phân trang, hiển thị dữ liệu đặc biệt (long text, null cover, nhiều artists), performance
