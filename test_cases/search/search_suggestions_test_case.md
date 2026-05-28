# Ondas Mobile — Test Cases: Gợi ý Tìm kiếm (Search Suggestions)

> **Tính năng**: Suggestions, Trending, Recent Searches, Genres khi mở màn Search  
> **Phiên bản**: 1.0  
> **Ngày**: 22/05/2026  
> **Loại test**: Functional E2E Test Cases (Manual / Exploratory)  
> **File gốc**: `search_discovery_test_case.md` — TCs: 05-09, 18-19, 33-39, 87

---

## Thành phần liên quan

| Thành phần | Mô tả |
|---|---|
| `SearchScreen` | Màn hình Search chính |
| `SearchBloc` | Quản lý state search — bao gồm suggestions, search results |
| API | `GET /api/search/suggestions`, `DELETE /api/search/history` |
| State | `SearchSuggestionsLoaded` — hiển thị trending, recent, genres |

---

## Bảng Test Cases

| STT (ID) | Nhóm | Tên Test Case | Các bước thực hiện (Steps) | Dữ liệu kiểm thử (Test Data) | Kết quả mong đợi (Expected Result) |
|---|---|---|---|---|---|
| 05 | Happy Path | Hiển thị gợi ý tìm kiếm (Suggestions) khi mới vào màn Search | 1) Mở màn Search lần đầu<br>2) Quan sát màn hình (chưa nhập gì) | Chưa có lịch sử tìm kiếm | `SearchState` chuyển sang `SearchSuggestionsLoaded`; hiển thị: `trendingSearches`, `trendingSongs`, danh sách `genres`; mỗi section hiển thị đúng tiêu đề ("Xu hướng", "Bài hát phổ biến", "Thể loại") |
| 06 | Happy Path | Hiển thị lịch sử tìm kiếm gần đây (Recent Searches) | 1) Đã từng tìm kiếm ít nhất 1 từ khóa trước đó<br>2) Mở màn Search<br>3) Quan sát section "Recent Searches" | Lịch sử: ["pop", "rock", "jazz"] | Section "Tìm kiếm gần đây" hiển thị danh sách 3 từ khóa; mỗi mục có icon đồng hồ; nhấn vào 1 từ khóa điền vào ô search và submit ngay |
| 07 | Happy Path | Nhấn vào Trending Search để tìm kiếm nhanh | 1) Mở màn Search (có suggestions hiển thị)<br>2) Nhấn vào 1 mục trong "Xu hướng" (vd: "Summer Hits")<br>3) Quan sát | Từ khóa trending: "Summer Hits" | Ô search được điền "Summer Hits"; kết quả tìm kiếm hiển thị ngay (SearchSubmitted dispatch); `SearchLoaded` emit với query = "Summer Hits" |
| 08 | Happy Path | Nhấn vào Genre trong suggestions để tìm kiếm theo thể loại | 1) Mở màn Search (có suggestions)<br>2) Nhấn vào 1 thể loại (vd: "Rock")<br>3) Quan sát | Genre: "Rock" (id, name, slug) | Ô search được điền tên thể loại; kết quả hiển thị bài hát/album/artist thuộc thể loại đó; hoặc điều hướng đến màn `/genres/{id}` nếu có route |
| 09 | Happy Path | Xóa lịch sử tìm kiếm (Clear History) | 1) Mở màn Search, đang có recent searches<br>2) Nhấn nút "Xóa" hoặc "Clear All" ở section Recent<br>3) Xác nhận (nếu có dialog) | Recent: ["pop", "rock"] | API `DELETE /api/search/history` được gọi; section Recent biến mất hoặc trở về rỗng; suggestions được tải lại (re-fetch) |
| 18 | Happy Path | Nhấn vào Song tile trong Trending Songs (suggestions) để phát nhạc | 1) Mở màn Search (có trending songs)<br>2) Nhấn vào 1 bài hát trong "Bài hát phổ biến"<br>3) Quan sát | Trending song: "Shape of You" | PlayerScreen mở ra; bài hát được phát; queue = danh sách trending songs hiện tại |
| 19 | Happy Path | Nhập từ khóa → xóa từng ký tự → Suggestions hiển thị lại | 1) Nhập từ khóa "rock"<br>2) Kết quả hiển thị<br>3) Xóa từng ký tự một (backspace)<br>4) Khi xóa hết, quan sát | Từ khóa: "rock" → "roc" → "ro" → "r" → "" | Khi còn ký tự: debounce submit; khi xóa hết (`query.isEmpty`): `SearchCleared` dispatch → quay về `SearchSuggestionsLoaded` (dùng cache, không gọi lại API) |
| 33 | Boundary | Không có lịch sử tìm kiếm — section Recent ẩn hoặc hiển thị rỗng | 1) Xóa hết lịch sử tìm kiếm<br>2) Mở màn Search<br>3) Quan sát | recentSearches = [] | Section "Tìm kiếm gần đây" không hiển thị hoặc hiển thị rỗng (không crash, không hiển thị header "Recent" trống) |
| 34 | Boundary | Lịch sử tìm kiếm có 1 mục duy nhất | 1) Chỉ tìm kiếm đúng 1 từ khóa trước đó<br>2) Mở lại Search | recentSearches = ["rock"] | Hiển thị đúng 1 mục "rock" trong section Recent; không lỗi layout |
| 35 | Boundary | Lịch sử tìm kiếm bị trùng lặp — server trả về mảng có duplicate | 1) Server trả về recentSearches = ["rock", "rock", "pop"] | recentSearches có duplicate | UI hiển thị tất cả các mục (hoặc đã deduplicate ở client); không crash; nếu không deduplicate thì hiển thị 2 mục "rock" |
| 36 | Boundary | Suggestions trả về trendingSearches rỗng | 1) Server trả về trendingSearches = [] | trendingSearches = [] | Section "Xu hướng" ẩn hoặc hiển thị empty state nhỏ; không crash; các section khác vẫn hiển thị bình thường |
| 37 | Boundary | Suggestions trả về trendingSongs rỗng | 1) Server trả về trendingSongs = [] | trendingSongs = [] | Section "Bài hát phổ biến" ẩn; không crash |
| 38 | Boundary | Suggestions trả về genres rỗng | 1) Server trả về genres = [] | genres = [] | Section "Thể loại" ẩn; không crash |
| 39 | Boundary | Suggestions trả về tất cả các trường đều rỗng | 1) Server trả về SearchSuggestion với tất cả list đều [] | All fields = [] | Màn hình hiển thị trạng thái trống (empty state) duy nhất: "Không có gợi ý nào" hoặc chỉ hiển thị thanh search; không crash; không màn hình trắng |
| 87 | Negative | `SearchCleared` khi suggestions cache = null | 1) Suggestions chưa từng được load thành công<br>2) Dispatch `SearchCleared` | _cachedSuggestion = null | Gọi `_restoreSuggestions` → `cachedSuggestion` null → dispatch `SuggestionsRequested` mới; không crash |

---

> **Tổng số Test Cases:** 15  
> **Phân bố:** Happy Path: 7 | Boundary: 7 | Negative: 1  
> **Phạm vi:** Suggestions display, trending searches/songs, genres, recent searches, clear history, empty/null boundary cases, cache handling
