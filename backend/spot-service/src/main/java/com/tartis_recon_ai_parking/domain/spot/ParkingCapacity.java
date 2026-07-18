package com.tartis_recon_ai_parking.domain.spot;

// PROVISIONAL — He estimado la capacidad
 
public final class ParkingCapacity {

    private ParkingCapacity() {
    }

    public static final int MAX_CAR_SPOTS = 250;
    public static final int MAX_CAR_PMR_SPOTS = 10;
    public static final int MAX_MOTORBIKE_SPOTS = 40;

    public static int getMaxCapacityFor(VehicleType type) {
        switch (type) {
            case CAR: return MAX_CAR_SPOTS;
            case CAR_PMR: return MAX_CAR_PMR_SPOTS;
            case MOTORBIKE: return MAX_MOTORBIKE_SPOTS;
            default: throw new IllegalArgumentException("Tipo de vehículo sin capacidad definida: " + type);
        }
    }
}