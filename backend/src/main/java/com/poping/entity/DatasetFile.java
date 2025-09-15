package com.poping.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * [文件概览]
 * - 目的: 数据集文件实体，记录上传文件的元信息
 * - 数据流: 数据库 ↔ Entity ↔ Service ↔ Controller
 * - 核心数据: 文件路径、大小、状态
 * - 关系: 多个 DatasetFile 对应一个 {@link Dataset}
 */
@TableName("dataset_files")
public class DatasetFile {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 业务主键
     */
    @TableField("file_id")
    private String fileId;

    /**
     * 关联数据集数据库ID
     */
    @TableField("dataset_db_id")
    private Long datasetDbId;

    /**
     * 关联数据集业务ID
     */
    @TableField("dataset_id")
    private String datasetId;

    /**
     * 原始文件名
     */
    @TableField("original_name")
    private String originalName;

    /**
     * OSS对象Key
     */
    @TableField("object_key")
    private String objectKey;

    /**
     * 文件URL
     */
    @TableField("file_url")
    private String fileUrl;

    /**
     * 文件大小
     */
    private Long size;

    /**
     * 文件类型
     */
    @TableField("content_type")
    private String contentType;

    /**
     * 解析状态
     */
    @TableField("parse_status")
    private String parseStatus;

    /**
     * 解析信息
     */
    @TableField("parse_message")
    private String parseMessage;

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

    public DatasetFile() {
        this.parseStatus = "pending";
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public Long getDatasetDbId() {
        return datasetDbId;
    }

    public void setDatasetDbId(Long datasetDbId) {
        this.datasetDbId = datasetDbId;
    }

    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

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

    public String getParseStatus() {
        return parseStatus;
    }

    public void setParseStatus(String parseStatus) {
        this.parseStatus = parseStatus;
    }

    public String getParseMessage() {
        return parseMessage;
    }

    public void setParseMessage(String parseMessage) {
        this.parseMessage = parseMessage;
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
