package com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest.dto.request;

import com.tartis_recon_ai_parking.domain.spot.SpotStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SpotRequest {

    @Min(value = 1, message = "El número de plaza debe ser mayor a 0")
    private int numSpot;

    @NotBlank(message = "El tipo de plaza es obligatorio (ej. COCHE o MOTO)")
    private String type; 

    // Mantenemos @NotNull porque @NotBlank es solo para Strings
    @NotNull(message = "El estado de la plaza es obligatorio")
    private SpotStatus status;

    // 1. Constructor vacío (obligatorio)
    public SpotRequest() {
    }

    // 2. Constructor con parámetros actualizados
    public SpotRequest(int numSpot, String type, SpotStatus status) {
        this.numSpot = numSpot;
        this.type = type;
        this.status = status;
    }

    // 3. Getters y Setters manuales
    public int getNumSpot() {
        return numSpot;
    }

    public void setNumSpot(int numSpot) {
        this.numSpot = numSpot;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    // Actualizados para devolver y recibir SpotStatus
    public SpotStatus getStatus() {
        return status;
    }

    public void setStatus(SpotStatus status) {
        this.status = status;
    }
}