package com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest.dto.response;

import com.tartis_recon_ai_parking.domain.spot.VehicleType;

public class AvailabilityResponse {

    private final VehicleType type;
    private final boolean available;
    private final long availableCount;
    private final long totalCount;

    public AvailabilityResponse(VehicleType type, boolean available, long availableCount, long totalCount) {
        this.type = type;
        this.available = available;
        this.availableCount = availableCount;
        this.totalCount = totalCount;
    }

    public VehicleType getType() {
        return type;
    }

    public boolean isAvailable() {
        return available;
    }

    public long getAvailableCount() {
        return availableCount;
    }

    public long getTotalCount() {
        return totalCount;
    }
}
