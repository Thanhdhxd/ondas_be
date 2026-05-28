# Ondas Mobile — Test Cases: Nhập & Submit Tìm kiếm (Search Input)

> **Tính năng**: Nhập từ khóa, submit, debounce, clear, xử lý input đặc biệt  
> **Phiên bản**: 1.0  
> **Ngày**: 22/05/2026  
> **Loại test**: Functional E2E Test Cases (Manual / Exploratory)  
> **File gốc**: `search_discovery_test_case.md` — TCs: 01-04, 10-14, 20, 23-29, 86

---

## Thành phần liên quan

| Thành phần | Mô tả |
|---|---|
| `SearchScreen` | Màn hình Search chính — ô nhập liệu, autofocus |
| `SearchBloc` | Quản lý state search — debounce 500ms, `SearchSubmitted`, `SearchCleared` |
| API | `GET /api/search` |
| State | `SearchLoaded`, `SearchSuggestionsLoaded` |

---

## Bảng Test Cases

| STT (ID) | Nhóm | Tên Test Case | Các bước thực hiện (Steps) | Dữ liệu kiểm thử (Test Data) | Kết quả mong đợi (Expected Result) |
|---|---|---|---|---|---|
| 01 | Happy Path | Tìm kiếm bài hát thành công với từ khóa chính xác | 1) Mở màn Search<br>2) Nhập từ khóa tìm kiếm<br>3) Chờ debounce 500ms (hoặc nhấn Enter)<br>4) Quan sát kết quả trả về | Từ khóa: "We Are the World" | `SearchState` chuyển sang `SearchLoaded`; hiển thị danh sách Songs, Artists, Albums; tổng số `totalSongs`, `totalArtists`, `totalAlbums` chính xác; Mỗi section có header hiển thị số lượng |
| 02 | Happy Path | Tìm kiếm nghệ sĩ (Artist) theo từ khóa | 1) Mở màn Search<br>2) Nhập từ khóa tên nghệ sĩ<br>3) Chờ kết quả<br>4) Quan sát section "Artists" | Từ khóa: "Michael Jackson" | Section Artists hiển thị danh sách nghệ sĩ khớp; mỗi artist tile hiển thị `name`, `imageUrl`; `totalArtists` ≥ 1; nhấn vào artist điều hướng sang `/songs/artist/{id}` |
| 03 | Happy Path | Tìm kiếm album (Album) theo từ khóa | 1) Mở màn Search<br>2) Nhập từ khóa tên album<br>3) Chờ kết quả<br>4) Quan sát section "Albums" | Từ khóa: "Thriller" | Section Albums hiển thị danh sách album khớp; mỗi album tile hiển thị `title`, `coverUrl`, `artistName`; `totalAlbums` ≥ 1; nhấn vào album điều hướng sang `/songs/album/{id}` |
| 04 | Happy Path | Tìm kiếm bài hát (Song) và phát nhạc từ kết quả | 1) Mở màn Search<br>2) Nhập từ khóa tìm kiếm → kết quả hiển thị<br>3) Chạm vào một bài hát trong section Songs<br>4) Quan sát | Từ khóa: "Blinding Lights" | PlayerScreen mở ra; bài hát đúng được phát; queue = toàn bộ danh sách songs trong kết quả tìm kiếm; `PlaySongRequested` dispatch với đúng `index` |
| 10 | Happy Path | Xóa nội dung ô tìm kiếm (Clear button) để trở về Suggestions | 1) Đang ở màn kết quả tìm kiếm<br>2) Nhấn nút X (clear) trong ô search<br>3) Quan sát | Đang ở `SearchLoaded` | Ô search trở về rỗng; `SearchCleared` event dispatch; `SearchState` quay về `SearchSuggestionsLoaded` với cached suggestions (không cần gọi lại API) |
| 11 | Happy Path | Tìm kiếm không phân biệt chữ hoa / chữ thường (Case-insensitive) | 1) Mở màn Search<br>2) Nhập từ khóa chữ thường<br>3) Xóa và nhập lại chữ hoa<br>4) So sánh kết quả | Từ khóa 1: "we are the world"<br>Từ khóa 2: "WE ARE THE WORLD" | Cả 2 lần đều trả về cùng danh sách kết quả (hoặc ít nhất không trả về rỗng cho lần 2); tìm kiếm không phân biệt case |
| 12 | Happy Path | Tìm kiếm với từ khóa có dấu và không dấu (nếu hỗ trợ) | 1) Mở màn Search<br>2) Nhập từ khóa có dấu: "nhạc"<br>3) Nhập từ khóa không dấu: "nhac"<br>4) So sánh kết quả | Từ khóa có dấu / không dấu | Cả 2 đều trả về kết quả liên quan (nếu backend hỗ trợ fuzzy search tiếng Việt); nếu không hỗ trợ, kết quả có thể khác nhau nhưng không crash |
| 13 | Happy Path | Tìm kiếm với từ khóa tiếng Anh + số | 1) Mở màn Search<br>2) Nhập từ khóa chứa chữ và số<br>3) Quan sát kết quả | Từ khóa: "song 2024" | Kết quả trả về bình thường; hiển thị bài hát/album có liên quan đến "2024"; không lỗi |
| 14 | Happy Path | Tìm kiếm với emoji trong từ khóa | 1) Mở màn Search<br>2) Nhập từ khóa chứa emoji<br>3) Quan sát kết quả | Từ khóa: "🎵 summer" | Input được encode đúng; server xử lý an toàn; kết quả có thể rỗng; không crash; emoji hiển thị đúng trong ô search |
| 20 | Happy Path | Tìm kiếm với từ khóa chỉ chứa 1 ký tự | 1) Mở màn Search<br>2) Nhập 1 ký tự<br>3) Chờ debounce<br>4) Quan sát | Từ khóa: "a" | `SearchSubmitted` dispatch với "a"; server trả về kết quả (có thể rỗng hoặc nhiều); không crash; UI hiển thị đúng |
| 23 | Happy Path | Tìm kiếm được kích hoạt bởi nhấn Enter / phím Search trên bàn phím | 1) Mở màn Search<br>2) Nhập từ khóa<br>3) Nhấn Enter / nút Search trên bàn phím ảo<br>4) Quan sát | Từ khóa: "jazz" | Kết quả hiển thị ngay lập tức (không cần đợi debounce); `SearchSubmitted` dispatch |
| 24 | Happy Path | Debounce 500ms hoạt động đúng — không gửi quá nhiều request khi gõ nhanh | 1) Mở màn Search<br>2) Gõ nhanh từ khóa "blinding lights" (gõ liên tục không dừng)<br>3) Quan sát số request gửi lên server | Gõ nhanh 15 ký tự trong < 2 giây | Chỉ gửi 1 request duy nhất sau khi dừng gõ 500ms; không spam API; kết quả là "blinding lights" (đầy đủ) |
| 25 | Boundary | Từ khóa tìm kiếm đúng bằng độ dài tối thiểu (1 ký tự) | 1) Mở màn Search<br>2) Nhập chính xác 1 ký tự<br>3) Nhấn Enter | Từ khóa: "a" | Tìm kiếm được thực thi; server trả về kết quả cho "a"; không lỗi validation |
| 26 | Boundary | Từ khóa tìm kiếm rỗng — submit bị chặn | 1) Mở màn Search<br>2) Nhập vài ký tự rồi xóa hết<br>3) Nhấn Enter khi ô trống | Từ khóa: "" (rỗng) | `SearchSubmitted` không dispatch nếu query.trim().isEmpty; thay vào đó `SearchCleared` → quay về Suggestions |
| 27 | Boundary | Từ khóa tìm kiếm chỉ gồm khoảng trắng | 1) Mở màn Search<br>2) Nhập 5 dấu cách<br>3) Nhấn Enter | Từ khóa: "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" (5 spaces) | Sau `trim()`, query rỗng → không gửi request; quay về Suggestions; không lỗi |
| 28 | Boundary | Từ khóa tìm kiếm có khoảng trắng đầu/cuối | 1) Mở màn Search<br>2) Nhập "&nbsp;&nbsp;rock&nbsp;&nbsp;" | Từ khóa: "&nbsp;&nbsp;rock&nbsp;&nbsp;" | Từ khóa được trim thành "rock" trước khi gửi API; kết quả trả về cho "rock" |
| 29 | Boundary | Từ khóa tìm kiếm có độ dài cực lớn (500+ ký tự) | 1) Mở màn Search<br>2) Nhập chuỗi 500+ ký tự<br>3) Nhấn Enter | Từ khóa: "a" × 500 | Một trong hai: (a) input bị giới hạn maxLength, không cho nhập quá; (b) nếu gửi được, server trả về lỗi 400 hoặc kết quả rỗng; không crash |
| 86 | Negative | Gửi `SearchSubmitted` với query rỗng sau trim | 1) `SearchBloc.add(SearchSubmitted("   "))` | query = "&nbsp;&nbsp;&nbsp;" (3 spaces) | `_onSubmitted` kiểm tra `query.isEmpty` → gọi `_restoreSuggestions`; không gọi API search; không crash |

---

> **Tổng số Test Cases:** 18  
> **Phân bố:** Happy Path: 12 | Boundary: 5 | Negative: 1  
> **Phạm vi:** Search query types (song/artist/album), case-insensitive, diacritics, emoji, debounce, enter submit, clear, empty/spaces/long input validation
