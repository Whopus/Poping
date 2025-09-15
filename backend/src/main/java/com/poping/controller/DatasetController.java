package com.poping.controller;

import com.poping.dto.dataset.DatasetCreateRequest;
import com.poping.dto.dataset.DatasetDetailResponse;
import com.poping.dto.dataset.DatasetListResponse;
import com.poping.dto.dataset.DatasetUploadResponse;
import com.poping.service.DatasetService;
import com.poping.service.DatasetStorageService;
import com.poping.util.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

/**
 * [文件概览]
 * - 目的: 数据集管理接口
 */
@RestController
@RequestMapping("/api/v1/datasets")
@Validated
@CrossOrigin(origins = "*")
public class DatasetController {

    private static final Logger log = LoggerFactory.getLogger(DatasetController.class);

    private final DatasetService datasetService;
    private final DatasetStorageService datasetStorageService;

    public DatasetController(DatasetService datasetService, DatasetStorageService datasetStorageService) {
        this.datasetService = datasetService;
        this.datasetStorageService = datasetStorageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<DatasetUploadResponse>> uploadFile(@RequestPart("file") MultipartFile file) {
        try {
            DatasetUploadResponse response = datasetStorageService.upload(file);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception ex) {
            log.error("上传文件失败: {}", ex.getMessage(), ex);
            return ResponseEntity.badRequest().body(ApiResponse.error("文件上传失败: " + ex.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DatasetDetailResponse>> createDataset(
            @Valid @RequestBody DatasetCreateRequest request,
            @RequestHeader(value = "User-Id", required = false) String userId) {
        try {
            DatasetDetailResponse response = datasetService.createDataset(request, userId);
            return ResponseEntity.ok(ApiResponse.success("数据集创建成功", response));
        } catch (Exception ex) {
            log.error("创建数据集失败: {}", ex.getMessage(), ex);
            return ResponseEntity.badRequest().body(ApiResponse.error("创建数据集失败: " + ex.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<DatasetListResponse>> listDatasets(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestHeader(value = "User-Id", required = false) String userId) {
        try {
            DatasetListResponse response = datasetService.listDatasets(page, size, type, status, keyword, userId);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception ex) {
            log.error("查询数据集失败: {}", ex.getMessage(), ex);
            return ResponseEntity.badRequest().body(ApiResponse.error("查询数据集失败: " + ex.getMessage()));
        }
    }

    @GetMapping("/{datasetId}")
    public ResponseEntity<ApiResponse<DatasetDetailResponse>> getDatasetDetail(
            @PathVariable String datasetId,
            @RequestHeader(value = "User-Id", required = false) String userId) {
        try {
            DatasetDetailResponse response = datasetService.getDatasetDetail(datasetId, userId);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception ex) {
            log.error("获取数据集详情失败: {}", ex.getMessage(), ex);
            return ResponseEntity.badRequest().body(ApiResponse.error("获取数据集详情失败: " + ex.getMessage()));
        }
    }
}
