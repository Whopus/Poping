package com.poping.dto.dataset;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * [文件概览]
 * - 目的: 创建数据集请求体
 * - 核心数据: 基础信息、标签、文件列表
 */
public class DatasetCreateRequest {

    @NotBlank(message = "数据集标题不能为空")
    private String name;

    @Size(max = 2000, message = "描述长度不能超过2000字符")
    private String description;

    @NotBlank(message = "数据类型不能为空")
    private String type;

    @NotNull
    @Size(max = 3, message = "最多选择3个标签")
    private List<@NotBlank String> tags;

    @NotEmpty(message = "请至少上传一个文件")
    private List<@Valid DatasetFileRequest> files;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<DatasetFileRequest> getFiles() {
        return files;
    }

    public void setFiles(List<DatasetFileRequest> files) {
        this.files = files;
    }
}
