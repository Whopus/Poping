package com.poping.service;

import com.poping.dto.dataset.DatasetUploadResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * [文件概览]
 * - 目的: 文件上传存储接口
 */
public interface DatasetStorageService {

    DatasetUploadResponse upload(MultipartFile file);
}
