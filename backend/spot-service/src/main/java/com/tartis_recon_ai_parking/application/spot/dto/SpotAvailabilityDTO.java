package com.tartis_recon_ai_parking.application.spot.dto;

import com.tartis_recon_ai_parking.domain.spot.VehicleType;

public class SpotAvailabilityDTO {

    private final VehicleType type;
    private final long availableCount;
    private final long totalCount;

    public SpotAvailabilityDTO(VehicleType type, long availableCount, long totalCount) {
        this.type = type;
        this.availableCount = availableCount;
        this.totalCount = totalCount;
    }

    public VehicleType getType() {
        return type;
    }

    public boolean isAvailable() {
        return availableCount > 0;
    }

    public long getAvailableCount() {
        return availableCount;
    }

    public long getTotalCount() {
        return totalCount;
    }
}
