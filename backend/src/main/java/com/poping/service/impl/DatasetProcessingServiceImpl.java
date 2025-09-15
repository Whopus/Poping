package com.poping.service.impl;

import com.poping.dto.dataset.DatasetProgressMessage;
import com.poping.entity.Dataset;
import com.poping.entity.DatasetFile;
import com.poping.repository.DatasetFileRepository;
import com.poping.repository.DatasetRepository;
import com.poping.service.DatasetProcessingService;
import com.poping.websocket.DatasetProgressWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * [文件概览]
 * - 目的: 数据集解析流程实现
 */
@Service
public class DatasetProcessingServiceImpl implements DatasetProcessingService {

    private static final Logger log = LoggerFactory.getLogger(DatasetProcessingServiceImpl.class);

    private final DatasetRepository datasetRepository;
    private final DatasetFileRepository datasetFileRepository;
    private final DatasetProgressWebSocketHandler progressWebSocketHandler;

    public DatasetProcessingServiceImpl(DatasetRepository datasetRepository,
                                        DatasetFileRepository datasetFileRepository,
                                        DatasetProgressWebSocketHandler progressWebSocketHandler) {
        this.datasetRepository = datasetRepository;
        this.datasetFileRepository = datasetFileRepository;
        this.progressWebSocketHandler = progressWebSocketHandler;
    }

    @Override
    @Transactional
    public void startProcessing(Dataset dataset, List<DatasetFile> files) {
        dataset.setStatus("processing");
        dataset.setProcessingProgress(0);
        dataset.setLastMessage("数据集已保存，等待解析");
        datasetRepository.updateById(dataset);

        sendProgress(dataset.getDatasetId(), 5, "processing", "解析任务已入队");

        CompletableFuture.runAsync(() -> processDataset(dataset, files));
    }

    private void processDataset(Dataset dataset, List<DatasetFile> files) {
        try {
            sendProgress(dataset.getDatasetId(), 15, "processing", "开始解析文件");

            if (files != null && !files.isEmpty()) {
                int step = files.size() == 0 ? 0 : 70 / files.size();
                int progress = 15;
                for (int index = 0; index < files.size(); index++) {
                    DatasetFile file = files.get(index);
                    markFileProcessing(file);
                    progress += step;
                    sendProgress(dataset.getDatasetId(), Math.min(progress, 85), "processing",
                            String.format("正在解析文件 %d/%d", index + 1, files.size()));

                    // TODO: 替换为真实的文件解析逻辑
                }
            }

            dataset.setStatus("ready");
            dataset.setProcessingProgress(100);
            dataset.setLastMessage("解析完成");
            datasetRepository.updateById(dataset);

            if (files != null) {
                for (DatasetFile file : files) {
                    markFileCompleted(file);
                }
            }

            sendProgress(dataset.getDatasetId(), 100, "ready", "解析完成");
        } catch (Exception ex) {
            log.error("处理数据集失败: {}", ex.getMessage(), ex);
            dataset.setStatus("error");
            dataset.setProcessingProgress(100);
            dataset.setLastMessage("解析失败: " + ex.getMessage());
            datasetRepository.updateById(dataset);

            if (files != null) {
                for (DatasetFile file : files) {
                    markFileFailed(file, ex.getMessage());
                }
            }

            sendProgress(dataset.getDatasetId(), 100, "error", "解析失败: " + ex.getMessage());
        }
    }

    private void markFileProcessing(DatasetFile file) {
        file.setParseStatus("processing");
        datasetFileRepository.updateById(file);
    }

    private void markFileCompleted(DatasetFile file) {
        file.setParseStatus("completed");
        file.setParseMessage("已完成解析");
        datasetFileRepository.updateById(file);
    }

    private void markFileFailed(DatasetFile file, String message) {
        file.setParseStatus("error");
        file.setParseMessage(message);
        datasetFileRepository.updateById(file);
    }

    private void sendProgress(String datasetId, int progress, String status, String message) {
        DatasetProgressMessage progressMessage = new DatasetProgressMessage(datasetId, progress, status, message);
        progressWebSocketHandler.sendProgress(progressMessage);
    }
}
