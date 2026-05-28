# Ondas Mobile — Test Cases: Bảo mật Tìm kiếm (Search Security)

> **Tính năng**: SQL Injection, XSS, Unicode, path traversal, auth bypass trong tìm kiếm  
> **Phiên bản**: 1.0  
> **Ngày**: 22/05/2026  
> **Loại test**: Functional E2E Test Cases (Manual / Exploratory)  
> **File gốc**: `search_discovery_test_case.md` — TCs: 63-67, 77, 91-94

---

## Bảng Test Cases

| STT (ID) | Nhóm | Tên Test Case | Các bước thực hiện (Steps) | Dữ liệu kiểm thử (Test Data) | Kết quả mong đợi (Expected Result) |
|---|---|---|---|---|---|
| 63 | Negative | SQL Injection trong từ khóa tìm kiếm | 1) Nhập chuỗi SQL injection vào ô tìm kiếm<br>2) Submit | Từ khóa: `"'; DROP TABLE songs; --"` | Input được xử lý an toàn (parameterized query phía server); kết quả tìm kiếm trả về rỗng hoặc kết quả cho đúng chuỗi đó; không có SQL được thực thi |
| 64 | Negative | XSS trong từ khóa tìm kiếm | 1) Nhập chuỗi script vào ô tìm kiếm<br>2) Submit<br>3) Quan sát kết quả hiển thị | Từ khóa: `<script>alert('xss')</script>` | Input được escape/sanitize; script không được thực thi; kết quả hiển thị dưới dạng text thuần; không có alert popup |
| 65 | Negative | XSS trong tên nghệ sĩ / album / bài hát (từ server) | 1) Server trả về artist name có chứa script<br>2) Quan sát UI | Artist name: `<img src=x onerror=alert(1)>` | Tên hiển thị dạng text thuần (Flutter Text widget); script không thực thi; UI an toàn |
| 66 | Negative | Special characters trong từ khóa: ký tự Unicode đặc biệt | 1) Nhập từ khóa chứa ký tự Unicode control characters<br>2) Submit | Từ khóa: "test\u0000\u001F" (null byte, control chars) | Input được sanitize hoặc server xử lý an toàn; không crash; không gây lỗi backend |
| 67 | Negative | Từ khóa tìm kiếm là URL / path traversal | 1) Nhập từ khóa dạng URL hoặc path traversal<br>2) Submit | Từ khóa: `"../../../etc/passwd"` hoặc `"http://evil.com"` | Input được xử lý như text thường; không truy cập file system; không redirect; không gây hại |
| 77 | Negative | Navigate đến màn Search khi chưa đăng nhập (nếu bypass guard) | 1) Chưa đăng nhập / không có token<br>2) Điều hướng thủ công đến `/search` | Không có token | Route guard redirect về `/login`; nếu bypass: API search trả về 401 → JWT interceptor redirect Login |
| 91 | Negative | Gửi request search với page âm (page = -1) | 1) Thử gọi API với page = -1 (manual test hoặc qua developer tools) | page = -1 | Server trả về lỗi 400 Bad Request hoặc tự clamp về 0; không crash client |
| 92 | Negative | Gửi request search với size âm (size = -1) | 1) Thử gọi API với size = -1 | size = -1 | Server trả về lỗi 400 hoặc tự clamp về giá trị mặc định; không crash |
| 93 | Negative | Gửi request search với size = 0 | 1) Thử gọi API với size = 0 | size = 0 | Server trả về lỗi 400 hoặc kết quả rỗng; client không crash |
| 94 | Negative | Gửi request search với size cực lớn (size = 99999) | 1) Thử gọi API với size = 99999 | size = 99999 | Server có thể giới hạn size tối đa (vd: 100) và trả về tối đa 100 kết quả; client vẫn hiển thị bình thường; không crash |

---

> **Tổng số Test Cases:** 10  
> **Phân bố:** Negative: 10  
> **Phạm vi:** SQL Injection, XSS (input + server), Unicode control chars, path traversal, auth bypass, API parameter tampering (page/size)
