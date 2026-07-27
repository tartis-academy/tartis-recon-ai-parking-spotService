package com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tartis_recon_ai_parking.application.spot.dto.SpotAvailabilityDTO;
import com.tartis_recon_ai_parking.application.spot.dto.SpotDTO;
import com.tartis_recon_ai_parking.domain.spot.SpotStatus;
import com.tartis_recon_ai_parking.domain.spot.VehicleType;
import com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest.dto.response.AvailabilityResponse;
import com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest.dto.response.SpotResponse;

@DisplayName("Tests para el mapper SpotRestMapper")
class SpotRestMapperTest {

    // Instancia del mapper bajo prueba
    private SpotRestMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new SpotRestMapper();
    }

    @Test
    @DisplayName("Debe devolver null si el SpotDTO de entrada es nulo")
    void shouldReturnNullWhenSpotDtoIsNull() {
        // QUE HACE:
        // Pasa un valor nulo al metodo toResponse del mapper.
        SpotResponse response = mapper.toResponse((SpotDTO) null);

        // QUE DEBERIA HACER:
        // El mapper debe identificar el valor nulo y devolver null directamente sin lanzar NullPointerException.
        assertThat(response).isNull();
    }

    @Test
    @DisplayName("Debe mapear un SpotDTO correctamente a un SpotResponse")
    void shouldMapSpotDtoToSpotResponse() {
        // QUE HACE:
        // Genera un DTO de entrada con datos simulados validos y llama al metodo toResponse.
        UUID id = UUID.randomUUID();
        SpotDTO dto = new SpotDTO(id, VehicleType.CAR, SpotStatus.AVAILABLE);

        SpotResponse response = mapper.toResponse(dto);

        // QUE DEBERIA HACER:
        // La respuesta devuelta no debe ser nula y sus campos deben coincidir exactamente con los del DTO de origen.
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getType()).isEqualTo(VehicleType.CAR);
        assertThat(response.getStatus()).isEqualTo(SpotStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Debe devolver null si el SpotAvailabilityDTO de entrada es nulo")
    void shouldReturnNullWhenAvailabilityDtoIsNull() {
        AvailabilityResponse response = mapper.toResponse((SpotAvailabilityDTO) null);

        assertThat(response).isNull();
    }

    @Test
    @DisplayName("Debe mapear un SpotAvailabilityDTO correctamente a un AvailabilityResponse")
    void shouldMapAvailabilityDtoToAvailabilityResponse() {
        // QUE HACE:
        // Genera un DTO de disponibilidad con 7 libres de 20 y lo mapea.
        SpotAvailabilityDTO dto = new SpotAvailabilityDTO(VehicleType.CAR, 7L, 20L);

        AvailabilityResponse response = mapper.toResponse(dto);

        // QUE DEBERIA HACER:
        // Debe trasladar los contadores y derivar available a partir de availableCount.
        assertThat(response).isNotNull();
        assertThat(response.getType()).isEqualTo(VehicleType.CAR);
        assertThat(response.isAvailable()).isTrue();
        assertThat(response.getAvailableCount()).isEqualTo(7L);
        assertThat(response.getTotalCount()).isEqualTo(20L);
    }

    @Test
    @DisplayName("Debe marcar available a false cuando no queda ninguna plaza libre")
    void shouldMapAvailabilityDtoWithNoFreeSpots() {
        AvailabilityResponse response = mapper.toResponse(new SpotAvailabilityDTO(VehicleType.MOTORBIKE, 0L, 3L));

        assertThat(response.isAvailable()).isFalse();
        assertThat(response.getAvailableCount()).isZero();
        assertThat(response.getTotalCount()).isEqualTo(3L);
    }
}
