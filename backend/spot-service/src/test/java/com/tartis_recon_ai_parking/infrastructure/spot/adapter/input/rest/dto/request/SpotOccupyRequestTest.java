package com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tartis_recon_ai_parking.domain.spot.VehicleType;

@DisplayName("Tests para el DTO SpotOccupyRequest")
class SpotOccupyRequestTest {

    @Test
    @DisplayName("Debe crear correctamente una peticion utilizando getters y setters")
    void shouldCreateSpotOccupyRequest() {
        // QUE HACE:
        // - Crea una instancia vacia del DTO.
        // - Asigna el tipo de vehiculo utilizando el setter.
        SpotOccupyRequest request = new SpotOccupyRequest();
        request.setVehicleType(VehicleType.CAR);

        // QUE DEBERIA HACER:
        // - El valor obtenido a traves del getter debe ser igual al asignado.
        assertThat(request.getVehicleType()).isEqualTo(VehicleType.CAR);
    }

    @Test
    @DisplayName("Debe crear correctamente una peticion utilizando el constructor")
    void shouldCreateSpotOccupyRequestWithConstructor() {
        // QUE HACE:
        // - Crea una instancia del DTO pasando el tipo de vehiculo directamente al constructor.
        SpotOccupyRequest request = new SpotOccupyRequest(VehicleType.MOTORBIKE);

        // QUE DEBERIA HACER:
        // - El valor obtenido a traves del getter debe ser el mismo que se pasó al constructor.
        assertThat(request.getVehicleType()).isEqualTo(VehicleType.MOTORBIKE);
    }
}
