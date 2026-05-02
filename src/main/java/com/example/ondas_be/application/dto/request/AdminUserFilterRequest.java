package com.example.ondas_be.application.dto.request;

import com.example.ondas_be.domain.entity.Role;
import lombok.Data;

@Data
public class AdminUserFilterRequest {

    private String keyword;   // tìm theo email hoặc displayName
    private Role role;        // lọc theo role (USER / ADMIN / ...)
    private Boolean active;   // lọc theo trạng thái active

    private int page = 0;
    private int size = 20;
}
