# Ondas Mobile — Test Cases: Xử lý lỗi Mạng & Server (Search Errors)

> **Tính năng**: Xử lý lỗi mạng, server, JWT, response format khi tìm kiếm  
> **Phiên bản**: 1.0  
> **Ngày**: 22/05/2026  
> **Loại test**: Functional E2E Test Cases (Manual / Exploratory)  
> **File gốc**: `search_discovery_test_case.md` — TCs: 47-62, 74-76, 98-99

---

## Thành phần liên quan

| Thành phần | Mô tả |
|---|---|
| `SearchBloc` | Quản lý state — `SearchFailure` emit khi có lỗi |
| API | `GET /api/search`, `GET /api/search/suggestions` |
| JWT Interceptor | Tự động refresh token khi hết hạn |

---

## Bảng Test Cases

| STT (ID) | Nhóm | Tên Test Case | Các bước thực hiện (Steps) | Dữ liệu kiểm thử (Test Data) | Kết quả mong đợi (Expected Result) |
|---|---|---|---|---|---|
| 47 | Negative | Mất kết nối mạng khi vào màn Search (load suggestions) | 1) Tắt kết nối mạng<br>2) Mở màn Search<br>3) Quan sát | Không có mạng | `SearchFailure` emit với message: "Không thể kết nối máy chủ. Vui lòng kiểm tra kết nối mạng."; hiển thị màn lỗi + nút "Retry" |
| 48 | Negative | Mất kết nối mạng khi đang gõ từ khóa (submit search) | 1) Mở Search, đang có mạng<br>2) Nhập từ khóa, đợi debounce<br>3) Tắt mạng ngay khi request đang gửi | Từ khóa: "rock"; mất mạng giữa request | `SearchFailure` emit với message lỗi kết nối; màn kết quả cũ (nếu có) được thay bằng màn lỗi; nút "Retry" khả dụng |
| 49 | Negative | Mất kết nối mạng khi đang load thêm (infinite scroll) | 1) Đang ở `SearchLoaded` với `hasMore = true`<br>2) Tắt mạng<br>3) Cuộn xuống cuối danh sách | Đang ở trang 0; mất mạng trước khi load trang 1 | `SearchFailure` emit; màn hình hiển thị lỗi; danh sách cũ không bị mất; nút "Retry" khả dụng |
| 50 | Negative | Server timeout khi gọi API search | 1) Giả lập server timeout (>30s)<br>2) Nhập từ khóa và submit | Server không phản hồi | `SearchFailure` emit: "Yêu cầu quá thời gian. Vui lòng thử lại."; UI hiển thị lỗi + Retry |
| 51 | Negative | Server timeout khi gọi API suggestions | 1) Giả lập server timeout<br>2) Mở màn Search | Server suggestions timeout | `SearchFailure` emit; màn hình lỗi + Retry |
| 52 | Negative | Server trả về HTTP 500 Internal Server Error khi search | 1) Giả lập server lỗi 500<br>2) Nhập từ khóa và submit | Server 500 | `SearchFailure` emit: "Máy chủ gặp sự cố, vui lòng thử lại sau."; không crash |
| 53 | Negative | Server trả về HTTP 500 khi load suggestions | 1) Giả lập server lỗi 500<br>2) Mở màn Search | Server 500 | `SearchFailure` emit với message lỗi server; không crash |
| 54 | Negative | Server trả về HTTP 503 Service Unavailable | 1) Giả lập server lỗi 503<br>2) Submit tìm kiếm | Server 503 | `SearchFailure` emit; message phù hợp: "Dịch vụ tạm thời không khả dụng." |
| 55 | Negative | Server trả về HTTP 429 Too Many Requests (Rate Limit) | 1) Gửi nhiều request liên tục trong thời gian ngắn<br>2) Quan sát khi bị rate limit | Rate limit exceeded | `SearchFailure` emit; message: "Quá nhiều yêu cầu, vui lòng thử lại sau."; không spam retry; không crash |
| 56 | Negative | JWT token hết hạn khi gọi API search | 1) JWT token hết hạn<br>2) Nhập từ khóa và submit | Token expired | JWT interceptor tự động refresh token; nếu refresh thành công → kết quả hiển thị; nếu refresh thất bại → redirect về Login |
| 57 | Negative | JWT token hết hạn khi load suggestions | 1) JWT token hết hạn<br>2) Mở màn Search | Token expired | Tương tự trên: auto-refresh hoặc redirect Login |
| 58 | Negative | JWT token hết hạn khi clear search history | 1) JWT token hết hạn<br>2) Nhấn "Clear All" trong Recent | Token expired | Tương tự trên: auto-refresh hoặc redirect Login |
| 59 | Negative | Server trả về response format không đúng (thiếu trường) | 1) Giả lập API trả về JSON thiếu trường `songs`<br>2) Nhập từ khóa và submit | Response: `{"query": "test", "page": 0}` (thiếu songs) | Model parsing có thể fail → `SearchFailure` emit với message lỗi parse; không crash; nút Retry khả dụng |
| 60 | Negative | Server trả về response với kiểu dữ liệu sai | 1) Giả lập API trả về `songs` là string thay vì array<br>2) Submit tìm kiếm | Response: `{"songs": "invalid"}` | `SearchFailure` emit với message lỗi; không crash; không hiển thị dữ liệu sai |
| 61 | Negative | Server trả về response rỗng hoàn toàn (empty object) | 1) Giả lập API trả về `{}`<br>2) Submit tìm kiếm | Response: `{}` | Model parse lỗi → `SearchFailure` emit; không crash |
| 62 | Negative | Server trả về response null | 1) Giả lập API trả về `null` body<br>2) Submit tìm kiếm | Response body: null | `SearchFailure` emit; không crash |
| 74 | Negative | Ứng dụng bị kill khi đang load suggestions | 1) Mở Search (đang `SearchSuggestionsLoading`)<br>2) Force kill app<br>3) Mở lại app | Đang loading | App khởi động lại bình thường; vào Search sẽ load lại suggestions từ đầu; không crash; không corrupt state |
| 75 | Negative | Ứng dụng bị kill khi đang search | 1) Đang `SearchLoading`<br>2) Force kill app<br>3) Mở lại app | Đang loading kết quả | App khởi động lại; Search trở về trạng thái `SearchSuggestionsLoading`; không lưu kết quả tìm kiếm cũ |
| 76 | Negative | Chuyển màn hình (pop/push) khi đang load search | 1) Nhập từ khóa và submit<br>2) Ngay lập tức nhấn Back để quay lại màn trước<br>3) Quan sát | Đang `SearchLoading` | Không crash; BLoC vẫn xử lý response nhưng Widget đã unmount → state được emit trên BlocBuilder không còn listener; không leak memory |
| 98 | Negative | Kết nối mạng chậm (2G/3G) — load suggestions và search | 1) Giả lập mạng chậm (throttle 50kbps)<br>2) Mở Search<br>3) Nhập từ khóa và submit | Mạng 2G (50kbps) | Loading indicator hiển thị lâu hơn; kết quả vẫn trả về khi request hoàn thành; không crash; không timeout quá sớm (nên có timeout hợp lý, vd: 30s) |
| 99 | Negative | Kết nối mạng không ổn định (intermittent) | 1) Mở Search<br>2) Bật/tắt mạng liên tục trong khi đang search | Mạng chập chờn | State thay đổi giữa Loading và Failure; hiển thị thông báo lỗi khi mất mạng; nút Retry khả dụng; không crash; không corrupt data |

---

> **Tổng số Test Cases:** 21  
> **Phân bố:** Negative: 21  
> **Phạm vi:** Offline (suggestions/search/loadmore), timeout, HTTP errors (500/503/429), JWT expired, response format errors, app lifecycle (kill/pop), slow/unstable network
