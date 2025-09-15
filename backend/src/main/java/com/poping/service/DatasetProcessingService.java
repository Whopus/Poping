package com.poping.service;

import com.poping.entity.Dataset;
import com.poping.entity.DatasetFile;

import java.util.List;

/**
 * [文件概览]
 * - 目的: 数据集解析流程接口
 */
public interface DatasetProcessingService {

    void startProcessing(Dataset dataset, List<DatasetFile> files);
}
