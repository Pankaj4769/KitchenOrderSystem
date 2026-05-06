package com.kos.dto;

import java.util.List;

public class BulkStatusRequest {

    private List<Long> tableIds;
    private String status;

    public List<Long> getTableIds() { return tableIds; }
    public void setTableIds(List<Long> tableIds) { this.tableIds = tableIds; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
