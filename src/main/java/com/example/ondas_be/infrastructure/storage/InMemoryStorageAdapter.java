package com.example.ondas_be.infrastructure.storage;

import com.example.ondas_be.application.service.port.StoragePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@Profile("e2e")
public class InMemoryStorageAdapter implements StoragePort {

    private static final String DEFAULT_STORAGE_PATH = "/api/e2e/storage";

    private final Map<String, StoredObject> objects = new ConcurrentHashMap<>();

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.e2e.storage-path:/api/e2e/storage}")
    private String storagePath;

    @Override
    public String upload(String bucket, String objectName, InputStream inputStream, long size, String contentType) {
        byte[] bytes;
        try (InputStream in = inputStream) {
            bytes = in.readAllBytes();
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read upload stream", ex);
        }
        objects.put(buildKey(bucket, objectName), new StoredObject(bytes, contentType));
        log.info("E2E storage: uploaded {}/{} ({} bytes)", bucket, objectName, bytes.length);
        return getPublicUrl(bucket, objectName);
    }

    @Override
    public void delete(String bucket, String objectName) {
        if (objectName == null || objectName.isBlank()) {
            return;
        }
        objects.remove(buildKey(bucket, objectName));
        log.info("E2E storage: deleted {}/{}", bucket, objectName);
    }

    @Override
    public String getPublicUrl(String bucket, String objectName) {
        String base = trimTrailingSlash(baseUrl);
        String path = normalizePath(storagePath);
        return base + path + "/" + bucket + "/" + objectName;
    }

    @Override
    public String extractObjectName(String bucket, String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        String path = normalizePath(storagePath);
        String marker = path + "/" + bucket + "/";
        int idx = url.indexOf(marker);
        if (idx >= 0) {
            return url.substring(idx + marker.length());
        }
        String mockPrefix = "mock://" + bucket + "/";
        if (url.startsWith(mockPrefix)) {
            return url.substring(mockPrefix.length());
        }
        String bucketPrefix = bucket + "/";
        if (url.startsWith(bucketPrefix)) {
            return url.substring(bucketPrefix.length());
        }
        return url;
    }

    @Override
    public InputStream getObjectStream(String bucket, String objectName, long offset, long length) {
        StoredObject stored = objects.get(buildKey(bucket, objectName));
        if (stored == null) {
            throw new IllegalStateException("E2E storage object not found: " + bucket + "/" + objectName);
        }
        byte[] data = stored.data();
        int safeOffset = (int) Math.max(0, Math.min(offset, data.length));
        int maxLength = data.length - safeOffset;
        int safeLength = length > 0 ? (int) Math.min(length, maxLength) : maxLength;
        return new ByteArrayInputStream(data, safeOffset, safeLength);
    }

    private String buildKey(String bucket, String objectName) {
        return bucket + "/" + objectName;
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String normalizePath(String value) {
        String path = value == null || value.isBlank() ? DEFAULT_STORAGE_PATH : value.trim();
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

    private record StoredObject(byte[] data, String contentType) {
    }
}
