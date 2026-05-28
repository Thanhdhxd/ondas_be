# Ondas Mobile — Test Cases: Edge Cases & UX Tìm kiếm

> **Tính năng**: Race conditions, BLoC edge cases, UX (theme, keyboard, rotation, navigation), performance  
> **Phiên bản**: 1.0  
> **Ngày**: 22/05/2026  
> **Loại test**: Functional E2E Test Cases (Manual / Exploratory)  
> **File gốc**: `search_discovery_test_case.md` — TCs: 68-73, 78-81, 88-90, 95-97, 100

---

## Bảng Test Cases

| STT (ID) | Nhóm | Tên Test Case | Các bước thực hiện (Steps) | Dữ liệu kiểm thử (Test Data) | Kết quả mong đợi (Expected Result) |
|---|---|---|---|---|---|
| 68 | Negative | Gửi đồng thời nhiều request search (race condition) | 1) Gõ nhanh "a" → xóa → "ab" → xóa → "abc" (mỗi lần submit ngay)<br>2) Quan sát state cuối cùng | Gõ/xóa liên tục, nhiều request chồng chéo | State cuối cùng tương ứng với request hoàn thành sau cùng; không hiển thị kết quả của request cũ; không crash; không leak memory |
| 69 | Negative | Gửi `SearchLoadMoreRequested` khi `hasMore = false` | 1) Tìm kiếm từ khóa ít kết quả (`hasMore = false`)<br>2) Trigger load more (nếu UI vẫn cho phép) | hasMore = false | `SearchBloc._onLoadMore` kiểm tra `!current.hasMore` → return sớm; không gửi request; không crash |
| 70 | Negative | Gửi `SearchLoadMoreRequested` khi state không phải `SearchLoaded` | 1) Đang ở `SearchSuggestionsLoaded`<br>2) Trigger load more (nếu có thể) | State = SearchSuggestionsLoaded | `_onLoadMore` kiểm tra `current is! SearchLoaded` → return sớm; không gửi request; không crash |
| 71 | Negative | Nhấn nút "Retry" nhiều lần liên tục | 1) Đang ở `SearchFailure`<br>2) Nhấn nút "Retry" 5 lần trong 2 giây | Đang ở màn lỗi | Nút Retry không bị disable nhưng request chỉ gửi theo debounce hoặc lần cuối; không spam API; không crash |
| 72 | Negative | Xóa lịch sử tìm kiếm nhiều lần liên tục | 1) Nhấn "Clear All" 3 lần liên tục<br>2) Quan sát | Đang có recent searches | Mỗi lần nhấn đều dispatch `SearchHistoryCleared`; `_clearHistoryUseCase` được gọi; không crash; không xóa nhiều hơn mức cần |
| 73 | Negative | API clear history trả về lỗi — hiển thị thông báo | 1) Giả lập server lỗi khi clear history<br>2) Nhấn "Clear All" | API DELETE /api/search/history trả về 500 | `SearchFailure` emit; section Recent vẫn hiển thị (không bị xóa optimistic); SnackBar lỗi hiển thị |
| 78 | Boundary / UX | Chạm vào Mini Player khi đang ở màn Search | 1) Đang ở Search, có bài hát đang phát (Mini Player hiển thị)<br>2) Chạm vào Mini Player<br>3) Quan sát | Bài hát đang phát | PlayerScreen mở ra (slide-up); khi đóng PlayerScreen, quay lại đúng màn Search với state cũ (không reload); giữ nguyên kết quả tìm kiếm và vị trí cuộn |
| 79 | Boundary / UX | Xoay màn hình (portrait ↔ landscape) khi đang xem kết quả tìm kiếm | 1) Đang ở `SearchLoaded`<br>2) Xoay màn hình từ dọc → ngang → dọc | Đang hiển thị kết quả | UI tự động thích ứng; kết quả tìm kiếm không bị mất; vị trí cuộn được giữ; không reload API |
| 80 | Boundary / UX | Keyboard hiển thị / ẩn khi focus / unfocus ô search | 1) Mở Search → keyboard hiển thị (autofocus hoặc tap vào)<br>2) Cuộn danh sách suggestions (nếu có)<br>3) Nhấn ra ngoài để ẩn keyboard | Autofocus vào ô search | Keyboard hiển thị khi focus; ẩn khi nhấn ra ngoài hoặc cuộn; không che mất nội dung; không crash |
| 81 | Boundary / UX | Nhấn Back từ màn Search để quay lại màn trước | 1) Đang ở Search (có thể đang xem kết quả)<br>2) Nhấn nút Back (Android) hoặc swipe back (iOS) | Đang ở Search | Quay lại màn trước đó (Home/Library); lần sau vào Search, state được khởi tạo lại (`SearchSuggestionsLoading` → fetch suggestions mới) |
| 88 | Negative | `SearchHistoryCleared` khi đang ở state không phải `SearchSuggestionsLoaded` | 1) Đang ở `SearchLoaded`<br>2) Dispatch `SearchHistoryCleared` (nhấn Clear All trên UI nếu có) | State = SearchLoaded | `_onHistoryCleared` gọi `clearHistoryUseCase` → dispatch `SuggestionsRequested`; UI chuyển sang loading rồi suggestions; không crash |
| 89 | Negative | Nhiều BLoC instance — mỗi lần vào Search tạo Bloc mới | 1) Vào Search → tìm "rock"<br>2) Back về Home → vào Search lại<br>3) Quan sát | 2 Bloc instance khác nhau | Mỗi lần vào Search, `BlocProvider` tạo `SearchBloc` mới; suggestions được fetch lại; không dùng chung state cũ; không conflict |
| 90 | Negative | Từ khóa tìm kiếm chứa ký tự tab / newline | 1) Nhập từ khóa có ký tự `\t` (tab) hoặc `\n` (newline) | Từ khóa: "rock\tpop" hoặc "rock\npop" | Input field có thể không cho phép nhập newline; nếu có, query được trim và gửi đi; server xử lý an toàn; không crash |
| 95 | Boundary / Performance | Tìm kiếm từ khóa trả về > 1000 kết quả | 1) Tìm kiếm từ khóa phổ biến (vd: "a") trả về > 1000 kết quả<br>2) Cuộn qua nhiều trang | Tổng kết quả > 1000; cuộn đến trang 20+ | Pagination hoạt động đúng; UI không bị lag; memory không tăng đột biến; ListView dùng `itemBuilder` để lazy load |
| 96 | Boundary / Performance | Nhập/xóa liên tục trong ô search (stress test UI) | 1) Nhập 1 ký tự → xóa → nhập 1 ký tự → xóa... lặp 30 lần trong 10 giây | Gõ/xóa liên tục | Debounce ngăn spam API; UI không bị lag/đơ; không crash; không leak memory; state cuối cùng đúng với ký tự cuối |
| 97 | Boundary / UX | Chế độ Dark Mode / Light Mode — màn Search | 1) Chuyển đổi giữa Dark Mode và Light Mode<br>2) Mở màn Search<br>3) Quan sát giao diện | App theme: Dark / Light | Màu nền, chữ, icon, ô search, suggestion tiles, result tiles đều thích ứng đúng với theme hiện tại; độ tương phản đủ; không có text bị ẩn |
| 100 | Boundary / UX | Nhấn vào bài hát trong kết quả tìm kiếm khi PlayerScreen đang mở | 1) PlayerScreen đang mở (slide-up panel)<br>2) Đóng PlayerScreen<br>3) Vào Search → tìm kiếm → nhấn vào bài hát khác | Đang phát Song A; nhấn Song B từ Search | Player chuyển sang phát Song B; queue cập nhật thành danh sách kết quả tìm kiếm mới; `currentIndex = index` của Song B |

---

> **Tổng số Test Cases:** 17  
> **Phân bố:** Negative: 9 | Boundary/UX: 6 | Boundary/Performance: 2  
> **Phạm vi:** Race conditions, BLoC state edge cases (loadMore/clear/history), retry spam, UX (mini player, rotation, keyboard, back, theme), performance (1000+ results, stress test)
