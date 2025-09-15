package com.poping.dto.dataset;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * [文件概览]
 * - 目的: 创建数据集时的文件信息
 * - 核心数据: OSS对象key、URL等
 */
public class DatasetFileRequest {

    @NotBlank(message = "文件对象Key不能为空")
    private String objectKey;

    @NotBlank(message = "文件URL不能为空")
    private String fileUrl;

    @NotBlank(message = "原始文件名不能为空")
    private String originalName;

    @NotNull(message = "文件大小不能为空")
    private Long size;

    private String contentType;

    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
