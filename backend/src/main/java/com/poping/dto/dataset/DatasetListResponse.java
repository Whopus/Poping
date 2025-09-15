package com.poping.dto.dataset;

import java.util.List;

/**
 * [文件概览]
 * - 目的: 数据集列表响应
 */
public class DatasetListResponse {

    private List<DatasetListItemResponse> items;
    private long total;
    private long page;
    private long size;

    public List<DatasetListItemResponse> getItems() {
        return items;
    }

    public void setItems(List<DatasetListItemResponse> items) {
        this.items = items;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getPage() {
        return page;
    }

    public void setPage(long page) {
        this.page = page;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
