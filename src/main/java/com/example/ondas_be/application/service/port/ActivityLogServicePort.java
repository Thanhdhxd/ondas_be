package com.example.ondas_be.application.service.port;

import java.util.UUID;

public interface ActivityLogServicePort {

    void recordLog(String action, String resourceType, UUID resourceId, String resourceName, String metadata);
}
