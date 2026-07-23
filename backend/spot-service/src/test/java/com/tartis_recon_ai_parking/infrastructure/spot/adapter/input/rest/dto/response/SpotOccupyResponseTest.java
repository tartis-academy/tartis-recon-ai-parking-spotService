package com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tartis_recon_ai_parking.domain.spot.SpotStatus;

@DisplayName("Tests para el DTO SpotOccupyResponse")
class SpotOccupyResponseTest {

    @Test
    @DisplayName("Debe instanciar correctamente la respuesta de ocupacion de plaza")
    void shouldCreateSpotOccupyResponse() {
        // QUE HACE:
        // - Genera unos datos aleatorios y validos (UUID, numero de plaza, estado).
        // - Instancia el DTO de respuesta usando dichos datos en el constructor.
        UUID id = UUID.randomUUID();
        SpotOccupyResponse response = new SpotOccupyResponse(id, 12, SpotStatus.OCCUPIED);

        // QUE DEBERIA HACER:
        // - Validar que cada getter devuelve exactamente la informacion introducida.
        assertThat(response.getSpotId()).isEqualTo(id);
        assertThat(response.getNumSpot()).isEqualTo(12);
        assertThat(response.getStatus()).isEqualTo(SpotStatus.OCCUPIED);
    }
}
