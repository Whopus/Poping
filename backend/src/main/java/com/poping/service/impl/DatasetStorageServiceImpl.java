package com.poping.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.poping.config.OssProperties;
import com.poping.dto.dataset.DatasetUploadResponse;
import com.poping.service.DatasetStorageService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

/**
 * [文件概览]
 * - 目的: OSS文件上传实现
 */
@Service
public class DatasetStorageServiceImpl implements DatasetStorageService {

    private static final Logger log = LoggerFactory.getLogger(DatasetStorageServiceImpl.class);

    private final OSS ossClient;
    private final OssProperties ossProperties;

    public DatasetStorageServiceImpl(OSS ossClient, OssProperties ossProperties) {
        this.ossClient = ossClient;
        this.ossProperties = ossProperties;
    }

    @Override
    public DatasetUploadResponse upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isBlank(originalFilename)) {
            originalFilename = file.getName();
        }
        String extension = StringUtils.substringAfterLast(originalFilename, ".");
        String objectKey = buildObjectKey(extension);

        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            if (StringUtils.isNotBlank(file.getContentType())) {
                metadata.setContentType(file.getContentType());
            }

            ossClient.putObject(ossProperties.getBucketName(), objectKey, file.getInputStream(), metadata);

            DatasetUploadResponse response = new DatasetUploadResponse();
            response.setObjectKey(objectKey);
            response.setFileUrl(buildFileUrl(objectKey));
            response.setOriginalName(originalFilename);
            response.setSize(file.getSize());
            response.setContentType(file.getContentType());
            return response;
        } catch (IOException e) {
            log.error("上传文件到OSS失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    private String buildObjectKey(String extension) {
        String dateFolder = LocalDate.now().toString();
        String basePath = StringUtils.defaultIfBlank(ossProperties.getBasePath(), "datasets");
        String uuid = UUID.randomUUID().toString().replace("-", "");
        if (StringUtils.isNotBlank(extension)) {
            return String.format("%s/%s/%s.%s", basePath, dateFolder, uuid, extension);
        }
        return String.format("%s/%s/%s", basePath, dateFolder, uuid);
    }

    private String buildFileUrl(String objectKey) {
        String endpoint = StringUtils.defaultIfBlank(ossProperties.getEndpoint(), "");
        String sanitizedEndpoint = endpoint.replaceFirst("^https?://", "");
        if (StringUtils.isBlank(sanitizedEndpoint)) {
            return objectKey;
        }
        return String.format("https://%s.%s/%s", ossProperties.getBucketName(), sanitizedEndpoint, objectKey);
    }
}
