package com.example.partner_ftask.data.model;

import java.util.List;

public class UpdateDistrictsRequest {
    private List<Long> districtIds;

    public UpdateDistrictsRequest(List<Long> districtIds) {
        this.districtIds = districtIds;
    }

    public List<Long> getDistrictIds() {
        return districtIds;
    }

    public void setDistrictIds(List<Long> districtIds) {
        this.districtIds = districtIds;
    }
}

