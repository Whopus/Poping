package com.poping.dto.dataset;

/**
 * [文件概览]
 * - 目的: WebSocket进度消息
 */
public class DatasetProgressMessage {

    private String datasetId;
    private Integer progress;
    private String status;
    private String message;
    private long timestamp = System.currentTimeMillis();

    public DatasetProgressMessage() {
    }

    public DatasetProgressMessage(String datasetId, Integer progress, String status, String message) {
        this.datasetId = datasetId;
        this.progress = progress;
        this.status = status;
        this.message = message;
    }

    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
