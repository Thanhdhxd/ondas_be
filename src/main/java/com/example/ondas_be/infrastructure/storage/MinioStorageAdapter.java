package com.example.ondas_be.infrastructure.storage;

import com.example.ondas_be.application.service.port.StoragePort;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.SetBucketPolicyArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Component
@Profile("!e2e")
@RequiredArgsConstructor
public class MinioStorageAdapter implements StoragePort {

    private final MinioClient minioClient;
    private final StorageProperties properties;

    @Override
    public String upload(String bucket, String objectName, InputStream inputStream, long size, String contentType) {
        ensureBucketExists(bucket);
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build()
            );
            return getPublicUrl(bucket, objectName);
        } catch (IOException | InvalidKeyException | InvalidResponseException | InsufficientDataException
                 | NoSuchAlgorithmException | ServerException | XmlParserException | ErrorResponseException
                 | InternalException ex) {
            throw new IllegalStateException("Upload to storage failed", ex);
        }
    }

    @Override
    public void delete(String bucket, String objectName) {
        if (objectName == null || objectName.isBlank()) {
            return;
        }
        try {
            minioClient.statObject(StatObjectArgs.builder().bucket(bucket).object(objectName).build());
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectName).build());
        } catch (ErrorResponseException ex) {
            if ("NoSuchKey".equalsIgnoreCase(ex.errorResponse().code())) {
                return;
            }
            throw new IllegalStateException("Delete from storage failed", ex);
        } catch (IOException | InvalidKeyException | InvalidResponseException | InsufficientDataException
                 | NoSuchAlgorithmException | ServerException | XmlParserException | InternalException ex) {
            throw new IllegalStateException("Delete from storage failed", ex);
        }
    }

    @Override
    public String getPublicUrl(String bucket, String objectName) {
        String baseUrl = properties.getEndpoint();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl + "/" + bucket + "/" + objectName;
    }

    @Override
    public String extractObjectName(String bucket, String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        try {
            URI uri = new URI(url);
            String path = uri.getPath();
            if (path == null) {
                return null;
            }
            String prefix = "/" + bucket + "/";
            int idx = path.indexOf(prefix);
            if (idx >= 0) {
                return path.substring(idx + prefix.length());
            }
            return path.startsWith("/") ? path.substring(1) : path;
        } catch (URISyntaxException ex) {
            return null;
        }
    }

    @Override
    public InputStream getObjectStream(String bucket, String objectName, long offset, long length) {
        try {
            GetObjectArgs.Builder builder = GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .offset(offset);
            if (length > 0) {
                builder.length(length);
            }
            return minioClient.getObject(builder.build());
        } catch (IOException | InvalidKeyException | InvalidResponseException | InsufficientDataException
                 | NoSuchAlgorithmException | ServerException | XmlParserException | ErrorResponseException
                 | InternalException ex) {
            throw new IllegalStateException("Failed to stream object from storage", ex);
        }
    }

    private void ensureBucketExists(String bucket) {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                String publicReadPolicy = """
                        {
                          "Version": "2012-10-17",
                          "Statement": [{
                            "Effect": "Allow",
                            "Principal": {"AWS": ["*"]},
                            "Action": ["s3:GetObject"],
                            "Resource": ["arn:aws:s3:::%s/*"]
                          }]
                        }""".formatted(bucket);
                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder().bucket(bucket).config(publicReadPolicy).build()
                );
            }
        } catch (IOException | InvalidKeyException | InvalidResponseException | InsufficientDataException
                 | NoSuchAlgorithmException | ServerException | XmlParserException | ErrorResponseException
                 | InternalException ex) {
            throw new IllegalStateException("Storage bucket check failed", ex);
        }
    }
}
