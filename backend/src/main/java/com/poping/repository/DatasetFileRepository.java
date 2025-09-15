package com.poping.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poping.entity.DatasetFile;
import org.apache.ibatis.annotations.Mapper;

/**
 * [文件概览]
 * - 目的: 数据集文件持久层接口
 * - 数据流: Service → Repository → 数据库
 * - 核心数据: {@link DatasetFile}
 */
@Mapper
public interface DatasetFileRepository extends BaseMapper<DatasetFile> {
}
