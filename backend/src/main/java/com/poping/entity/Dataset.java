package com.poping.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * [文件概览]
 * - 目的: 数据集实体，描述数据集元信息
 * - 数据流: 数据库 ↔ Entity ↔ Service ↔ Controller
 * - 核心数据: 数据集基础信息、标签、处理进度
 * - 关系: 与 {@link DatasetFile} 一对多关联
 */
@TableName("datasets")
public class Dataset {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 业务主键，外部使用
     */
    @TableField("dataset_id")
    private String datasetId;

    /**
     * 数据集名称
     */
    private String name;

    /**
     * 数据集描述
     */
    private String description;

    /**
     * 数据类型（text/image/...）
     */
    @TableField("data_type")
    private String dataType;

    /**
     * 数据集标签（JSON字符串）
     */
    private String tags;

    /**
     * 数据集状态 processing/ready/error
     */
    private String status;

    /**
     * 数据集总大小（字节）
     */
    @TableField("total_size")
    private Long totalSize;

    /**
     * 记录数，解析完成后更新
     */
    @TableField("record_count")
    private Long recordCount;

    /**
     * 处理进度 0-100
     */
    @TableField("processing_progress")
    private Integer processingProgress;

    /**
     * 创建者业务ID，对应 users.user_id
     */
    @TableField("owner_user_id")
    private String ownerUserId;

    /**
     * 最后一次处理信息
     */
    @TableField("last_message")
    private String lastMessage;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public Dataset() {
        this.status = "processing";
        this.processingProgress = 0;
        this.recordCount = 0L;
        this.totalSize = 0L;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
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

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(String ownerUserId) {
        this.ownerUserId = ownerUserId;
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
}
