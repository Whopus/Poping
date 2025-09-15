package com.poping.dto.dataset;

import java.time.LocalDateTime;
import java.util.List;

/**
 * [文件概览]
 * - 目的: 数据集详情响应
 */
public class DatasetDetailResponse {

    private String id;
    private String name;
    private String description;
    private String type;
    private String status;
    private List<String> tags;
    private Long totalSize;
    private Long recordCount;
    private Integer processingProgress;
    private String lastMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private DatasetOwnerInfo owner;
    private List<DatasetFileResponse> files;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(Long totalSize) {
        this.totalSize = totalSize;
    }

    public Long getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(Long recordCount) {
        this.recordCount = recordCount;
    }

    public Integer getProcessingProgress() {
        return processingProgress;
    }

    public void setProcessingProgress(Integer processingProgress) {
        this.processingProgress = processingProgress;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public DatasetOwnerInfo getOwner() {
        return owner;
    }

    public void setOwner(DatasetOwnerInfo owner) {
        this.owner = owner;
    }

    public List<DatasetFileResponse> getFiles() {
        return files;
    }

    public void setFiles(List<DatasetFileResponse> files) {
        this.files = files;
    }

    public static class DatasetOwnerInfo {
        private String id;
        private String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
