package com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tartis_recon_ai_parking.domain.spot.SpotStatus;

@DisplayName("Tests para el DTO SpotStatusRequest")
class SpotStatusRequestTest {

    @Test
    @DisplayName("Debe crear correctamente una peticion de cambio de estado utilizando getters y setters")
    void shouldCreateSpotStatusRequest() {
        SpotStatusRequest request = new SpotStatusRequest();
        request.setStatus(SpotStatus.UNAVAILABLE);

        assertThat(request.getStatus()).isEqualTo(SpotStatus.UNAVAILABLE);
    }

    @Test
    @DisplayName("Debe crear correctamente una peticion de cambio de estado utilizando el constructor")
    void shouldCreateSpotStatusRequestWithConstructor() {
        SpotStatusRequest request = new SpotStatusRequest(SpotStatus.AVAILABLE);

        assertThat(request.getStatus()).isEqualTo(SpotStatus.AVAILABLE);
    }
}
