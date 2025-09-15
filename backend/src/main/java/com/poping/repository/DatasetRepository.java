package com.poping.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poping.entity.Dataset;
import org.apache.ibatis.annotations.Mapper;

/**
 * [文件概览]
 * - 目的: 数据集持久层接口，提供基础CRUD能力
 * - 数据流: Service → Repository → 数据库
 * - 核心数据: {@link Dataset}
 */
@Mapper
public interface DatasetRepository extends BaseMapper<Dataset> {
}
