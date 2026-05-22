package com.example.ondas_be.presentation.controller;

import com.example.ondas_be.application.service.port.StoragePort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

@RestController
@RequestMapping("/api/e2e/storage")
@Profile("e2e")
@RequiredArgsConstructor
public class E2eStorageController {

    private final StoragePort storagePort;

    @GetMapping("/{bucket}/{objectName:.+}")
    public ResponseEntity<InputStreamResource> getObject(
            @PathVariable String bucket,
            @PathVariable String objectName,
            @RequestHeader(value = HttpHeaders.RANGE, required = false) String rangeHeader) {

        byte[] data = loadObject(bucket, objectName);
        long totalSize = data.length;

        if (totalSize == 0) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(resolveContentType(objectName)));
            headers.setContentLength(0);
            return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(new ByteArrayInputStream(new byte[0])));
        }

        long rangeStart = 0;
        long rangeEnd = totalSize - 1;
        boolean isPartial = false;

        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            String range = rangeHeader.substring("bytes=".length());
            String[] parts = range.split("-", 2);
            try {
                rangeStart = parts[0].isBlank() ? 0 : Long.parseLong(parts[0].trim());
                rangeEnd = (parts.length > 1 && !parts[1].isBlank())
                        ? Long.parseLong(parts[1].trim())
                        : totalSize - 1;
            } catch (NumberFormatException ignored) {
                rangeStart = 0;
                rangeEnd = totalSize - 1;
            }
            rangeStart = Math.max(0, Math.min(rangeStart, totalSize - 1));
            rangeEnd = Math.max(rangeStart, Math.min(rangeEnd, totalSize - 1));
            isPartial = true;
        }

        int start = (int) rangeStart;
        int end = (int) rangeEnd + 1;
        byte[] body = Arrays.copyOfRange(data, start, end);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
        headers.setContentType(MediaType.parseMediaType(resolveContentType(objectName)));
        headers.setContentLength(body.length);

        if (isPartial) {
            headers.set(HttpHeaders.CONTENT_RANGE,
                    "bytes " + rangeStart + "-" + rangeEnd + "/" + totalSize);
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .headers(headers)
                    .body(new InputStreamResource(new ByteArrayInputStream(body)));
        }

        return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(new ByteArrayInputStream(body)));
    }

    private byte[] loadObject(String bucket, String objectName) {
        try (InputStream stream = storagePort.getObjectStream(bucket, objectName, 0, -1)) {
            return stream.readAllBytes();
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read e2e storage object", ex);
        }
    }

    private String resolveContentType(String objectName) {
        if (objectName == null) return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        String lower = objectName.toLowerCase();
        if (lower.endsWith(".mp3")) return "audio/mpeg";
        if (lower.endsWith(".flac")) return "audio/flac";
        if (lower.endsWith(".ogg")) return "audio/ogg";
        if (lower.endsWith(".wav")) return "audio/wav";
        if (lower.endsWith(".aac")) return "audio/aac";
        if (lower.endsWith(".m4a")) return "audio/mp4";
        if (lower.endsWith(".opus")) return "audio/opus";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return MediaType.IMAGE_JPEG_VALUE;
        if (lower.endsWith(".png")) return MediaType.IMAGE_PNG_VALUE;
        if (lower.endsWith(".webp")) return "image/webp";
        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }
}
