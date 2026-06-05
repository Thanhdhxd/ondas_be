package com.example.ondas_be.application.service.port;

import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.dto.request.AdminActivityLogFilterRequest;
import com.example.ondas_be.application.dto.response.AdminActivityLogResponse;

public interface AdminActivityLogServicePort {

    PageResultDto<AdminActivityLogResponse> getActivityLogs(AdminActivityLogFilterRequest filter);
}
