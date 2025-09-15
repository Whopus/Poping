package com.poping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poping.dto.dataset.DatasetCreateRequest;
import com.poping.dto.dataset.DatasetDetailResponse;
import com.poping.dto.dataset.DatasetFileRequest;
import com.poping.dto.dataset.DatasetFileResponse;
import com.poping.dto.dataset.DatasetListItemResponse;
import com.poping.dto.dataset.DatasetListResponse;
import com.poping.entity.Dataset;
import com.poping.entity.DatasetFile;
import com.poping.entity.User;
import com.poping.repository.DatasetFileRepository;
import com.poping.repository.DatasetRepository;
import com.poping.repository.UserRepository;
import com.poping.service.DatasetProcessingService;
import com.poping.service.DatasetService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * [文件概览]
 * - 目的: 数据集业务实现
 */
@Service
public class DatasetServiceImpl implements DatasetService {

    private static final Logger log = LoggerFactory.getLogger(DatasetServiceImpl.class);

    private final DatasetRepository datasetRepository;
    private final DatasetFileRepository datasetFileRepository;
    private final UserRepository userRepository;
    private final DatasetProcessingService datasetProcessingService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DatasetServiceImpl(DatasetRepository datasetRepository,
                              DatasetFileRepository datasetFileRepository,
                              UserRepository userRepository,
                              DatasetProcessingService datasetProcessingService) {
        this.datasetRepository = datasetRepository;
        this.datasetFileRepository = datasetFileRepository;
        this.userRepository = userRepository;
        this.datasetProcessingService = datasetProcessingService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DatasetDetailResponse createDataset(DatasetCreateRequest request, String ownerUserId) {
        Dataset dataset = new Dataset();
        dataset.setDatasetId(generateDatasetId());
        dataset.setName(request.getName());
        dataset.setDescription(request.getDescription());
        dataset.setDataType(request.getType());
        dataset.setOwnerUserId(ownerUserId);
        dataset.setStatus("processing");
        dataset.setProcessingProgress(0);
        dataset.setTotalSize(request.getFiles().stream().mapToLong(DatasetFileRequest::getSize).sum());
        dataset.setTags(writeTags(request.getTags()));
        dataset.setLastMessage("数据集已创建，等待解析");

        datasetRepository.insert(dataset);
        Dataset persisted = datasetRepository.selectById(dataset.getId());
        if (persisted != null) {
            dataset = persisted;
        }

        List<DatasetFile> datasetFiles;
        for (DatasetFileRequest fileRequest : request.getFiles()) {
            DatasetFile file = new DatasetFile();
            file.setDatasetDbId(dataset.getId());
            file.setDatasetId(dataset.getDatasetId());
            file.setFileId(UUID.randomUUID().toString());
            file.setOriginalName(fileRequest.getOriginalName());
            file.setObjectKey(fileRequest.getObjectKey());
            file.setFileUrl(fileRequest.getFileUrl());
            file.setSize(fileRequest.getSize());
            file.setContentType(fileRequest.getContentType());
            file.setParseStatus("pending");
            datasetFileRepository.insert(file);
        }

        datasetFiles = datasetFileRepository.selectList(new LambdaQueryWrapper<DatasetFile>()
                .eq(DatasetFile::getDatasetId, dataset.getDatasetId()));

        datasetProcessingService.startProcessing(dataset, datasetFiles);

        return buildDetailResponse(dataset, datasetFiles, ownerUserId);
    }

    @Override
    public DatasetListResponse listDatasets(int page, int size, String type, String status, String keyword, String ownerUserId) {
        Page<Dataset> pageRequest = new Page<>(page, size);
        LambdaQueryWrapper<Dataset> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(ownerUserId)) {
            queryWrapper.eq(Dataset::getOwnerUserId, ownerUserId);
        }
        if (StringUtils.isNotBlank(type)) {
            queryWrapper.eq(Dataset::getDataType, type);
        }
        if (StringUtils.isNotBlank(status)) {
            queryWrapper.eq(Dataset::getStatus, status);
        }
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.and(wrapper -> wrapper.like(Dataset::getName, keyword)
                    .or().like(Dataset::getDescription, keyword));
        }
        queryWrapper.orderByDesc(Dataset::getUpdatedAt);

        Page<Dataset> result = datasetRepository.selectPage(pageRequest, queryWrapper);

        List<DatasetListItemResponse> items = result.getRecords().stream()
                .map(dataset -> buildListItem(dataset, findOwner(dataset.getOwnerUserId())))
                .collect(Collectors.toList());

        DatasetListResponse response = new DatasetListResponse();
        response.setItems(items);
        response.setTotal(result.getTotal());
        response.setPage(result.getCurrent());
        response.setSize(result.getSize());
        return response;
    }

    @Override
    public DatasetDetailResponse getDatasetDetail(String datasetId, String ownerUserId) {
        LambdaQueryWrapper<Dataset> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dataset::getDatasetId, datasetId);
        if (StringUtils.isNotBlank(ownerUserId)) {
            queryWrapper.eq(Dataset::getOwnerUserId, ownerUserId);
        }

        Dataset dataset = datasetRepository.selectOne(queryWrapper);
        if (dataset == null) {
            throw new IllegalArgumentException("数据集不存在或没有权限访问");
        }

        List<DatasetFile> files = datasetFileRepository.selectList(new LambdaQueryWrapper<DatasetFile>()
                .eq(DatasetFile::getDatasetId, dataset.getDatasetId())
                .orderByAsc(DatasetFile::getCreatedAt));

        return buildDetailResponse(dataset, files, dataset.getOwnerUserId());
    }

    private DatasetDetailResponse buildDetailResponse(Dataset dataset, List<DatasetFile> files, String ownerUserId) {
        DatasetDetailResponse response = new DatasetDetailResponse();
        response.setId(dataset.getDatasetId());
        response.setName(dataset.getName());
        response.setDescription(dataset.getDescription());
        response.setType(dataset.getDataType());
        response.setStatus(dataset.getStatus());
        response.setTags(readTags(dataset.getTags()));
        response.setTotalSize(dataset.getTotalSize());
        response.setRecordCount(dataset.getRecordCount());
        response.setProcessingProgress(dataset.getProcessingProgress());
        response.setLastMessage(dataset.getLastMessage());
        response.setCreatedAt(dataset.getCreatedAt());
        response.setUpdatedAt(dataset.getUpdatedAt());

        DatasetDetailResponse.DatasetOwnerInfo ownerInfo = buildOwnerInfo(findOwner(ownerUserId));
        response.setOwner(ownerInfo);

        List<DatasetFileResponse> fileResponses = files.stream().map(this::mapToFileResponse).collect(Collectors.toList());
        response.setFiles(fileResponses);
        return response;
    }

    private DatasetListItemResponse buildListItem(Dataset dataset, User owner) {
        DatasetListItemResponse item = new DatasetListItemResponse();
        item.setId(dataset.getDatasetId());
        item.setName(dataset.getName());
        item.setDescription(dataset.getDescription());
        item.setType(dataset.getDataType());
        item.setStatus(dataset.getStatus());
        item.setTags(readTags(dataset.getTags()));
        item.setTotalSize(dataset.getTotalSize());
        item.setRecordCount(dataset.getRecordCount());
        item.setProcessingProgress(dataset.getProcessingProgress());
        item.setCreatedAt(dataset.getCreatedAt());
        item.setUpdatedAt(dataset.getUpdatedAt());
        item.setOwner(buildOwnerInfo(owner));
        return item;
    }

    private DatasetFileResponse mapToFileResponse(DatasetFile file) {
        DatasetFileResponse response = new DatasetFileResponse();
        response.setFileId(file.getFileId());
        response.setOriginalName(file.getOriginalName());
        response.setObjectKey(file.getObjectKey());
        response.setFileUrl(file.getFileUrl());
        response.setSize(file.getSize());
        response.setContentType(file.getContentType());
        response.setParseStatus(file.getParseStatus());
        response.setParseMessage(file.getParseMessage());
        response.setCreatedAt(file.getCreatedAt());
        return response;
    }

    private DatasetDetailResponse.DatasetOwnerInfo buildOwnerInfo(User owner) {
        DatasetDetailResponse.DatasetOwnerInfo info = new DatasetDetailResponse.DatasetOwnerInfo();
        if (owner != null) {
            info.setId(owner.getUserId());
            info.setName(StringUtils.defaultIfBlank(owner.getUsername(), owner.getEmail()));
        } else {
            info.setId("-");
            info.setName("未知用户");
        }
        return info;
    }

    private User findOwner(String ownerUserId) {
        if (StringUtils.isBlank(ownerUserId)) {
            return null;
        }
        return userRepository.findByUserId(ownerUserId);
    }

    private String writeTags(List<String> tags) {
        try {
            return objectMapper.writeValueAsString(tags);
        } catch (JsonProcessingException e) {
            log.warn("序列化标签失败: {}", e.getMessage());
            return "[]";
        }
    }

    private List<String> readTags(String tags) {
        if (StringUtils.isBlank(tags)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(tags, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("解析标签失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private String generateDatasetId() {
        return "ds_" + UUID.randomUUID().toString().replace("-", "");
    }
}
