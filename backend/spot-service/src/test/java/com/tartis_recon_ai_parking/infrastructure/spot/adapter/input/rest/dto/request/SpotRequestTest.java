package com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tartis_recon_ai_parking.domain.spot.VehicleType;

@DisplayName("Tests para el DTO SpotRequest")
class SpotRequestTest {

    @Test
    @DisplayName("Debe crear correctamente una peticion de creacion de plaza utilizando getters y setters")
    void shouldCreateSpotRequest() {
        // QUE HACE:
        // - Crea una instancia vacia del DTO.
        // - Asigna el tipo de vehiculo utilizando el setter correspondiente.
        SpotRequest request = new SpotRequest();
        request.setType(VehicleType.CAR);

        // QUE DEBERIA HACER:
        // - El tipo obtenido al llamar al getter debe coincidir con el valor asignado.
        assertThat(request.getType()).isEqualTo(VehicleType.CAR);
    }

    @Test
    @DisplayName("Debe crear correctamente una peticion de creacion de plaza utilizando el constructor")
    void shouldCreateSpotRequestWithConstructor() {
        // QUE HACE:
        // - Inicializa la peticion de plaza pasandole el tipo de vehiculo al constructor.
        SpotRequest request = new SpotRequest(VehicleType.MOTORBIKE);

        // QUE DEBERIA HACER:
        // - El getter debe retornar el mismo tipo de vehiculo asignado por el constructor.
        assertThat(request.getType()).isEqualTo(VehicleType.MOTORBIKE);
    }
}
