package com.poping.service;

import com.poping.dto.dataset.DatasetCreateRequest;
import com.poping.dto.dataset.DatasetDetailResponse;
import com.poping.dto.dataset.DatasetListResponse;

/**
 * [文件概览]
 * - 目的: 数据集业务接口
 */
public interface DatasetService {

    DatasetDetailResponse createDataset(DatasetCreateRequest request, String ownerUserId);

    DatasetListResponse listDatasets(int page, int size, String type, String status, String keyword, String ownerUserId);

    DatasetDetailResponse getDatasetDetail(String datasetId, String ownerUserId);
}
