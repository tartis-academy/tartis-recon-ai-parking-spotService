package com.tartis_recon_ai_parking.spot_service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.tartis_recon_ai_parking.application.spot.dto.SpotCreateDTO;
import com.tartis_recon_ai_parking.application.spot.dto.SpotDTO;
import com.tartis_recon_ai_parking.application.spot.usecase.UpdateSpotUseCase;
import com.tartis_recon_ai_parking.domain.spot.SpotStatus;
import com.tartis_recon_ai_parking.domain.spot.VehicleType;
import com.tartis_recon_ai_parking.domain.spot.exception.InvalidSpotException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotNotFoundException;
import com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest.SpotRestAdapter;
import com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest.SpotRestMapper;
import com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest.dto.request.SpotRequest;
import com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest.dto.response.SpotResponse;


class SpotRestAdapterTest {

    private UpdateSpotUseCase updateSpotStatusUseCase;
    private SpotRestAdapter spotRestAdapter;

    // Antes de cada test se crea un mock del caso de uso (para controlar su comportamiento
    // sin depender de la base de datos) y una instancia real del mapper (lógica pura, sin
    // dependencias, no hace falta mockearla). Con ambos se construye el adapter REST tal
    // cual se usaría en producción.
    @BeforeEach
    void setUp() {
        updateSpotStatusUseCase = mock(UpdateSpotUseCase.class);
        SpotRestMapper spotRestMapper = new SpotRestMapper();
        spotRestAdapter = new SpotRestAdapter(updateSpotStatusUseCase, spotRestMapper);
    }

    @Nested
    @DisplayName("Caso feliz")
    class WhenRequestIsValid {

        // Qué hace: simula que el caso de uso encuentra la plaza y la actualiza correctamente,
        // devolviendo un SpotDTO con estado AVAILABLE. Llama al endpoint con un id y un request
        // válidos.
        // Qué se espera: la respuesta HTTP debe ser 200 OK, y el body (SpotResponse) debe
        // contener exactamente los mismos datos que devolvió el caso de uso (id, type, numSpot
        // y status), confirmando que SpotRestMapper traduce bien el SpotDTO a SpotResponse.
        @Test
        @DisplayName("Debe devolver 200 y el spot actualizado cuando el id existe")
        void shouldReturnOkWithUpdatedSpot() {
            UUID spotId = UUID.randomUUID();
            SpotRequest request = new SpotRequest(VehicleType.CAR, 15);
            SpotDTO updatedSpot = new SpotDTO(spotId, VehicleType.CAR, 15, SpotStatus.AVAILABLE);

            when(updateSpotStatusUseCase.execute(eq(spotId), any(SpotCreateDTO.class)))
                    .thenReturn(updatedSpot);

            ResponseEntity<SpotResponse> response = spotRestAdapter.updateStatus(spotId, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            SpotResponse body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getId()).isEqualTo(spotId);
            assertThat(body.getType()).isEqualTo(VehicleType.CAR);
            assertThat(body.getNumSpot()).isEqualTo(15);
            assertThat(body.getStatus()).isEqualTo(SpotStatus.AVAILABLE);
        }

        // Qué hace: no se centra en la respuesta, sino en verificar que el controlador
        // construye correctamente el SpotCreateDTO a partir del SpotRequest recibido, y que
        // se lo pasa al caso de uso junto con el id de la ruta (no un id inventado ni nulo).
        // Se usa un ArgumentCaptor para "capturar" exactamente qué objeto llegó al mock.
        // Qué se espera: el UpdateSpotUseCase debe haber sido invocado una vez con el spotId
        // de la ruta y un SpotCreateDTO cuyo type y numSpot coincidan exactamente con los del
        // SpotRequest de entrada (MOTORBIKE y 42).
        @Test
        @DisplayName("Debe invocar el caso de uso con el id de la ruta y los datos del body correctos")
        void shouldCallUseCaseWithCorrectArguments() {
            UUID spotId = UUID.randomUUID();
            SpotRequest request = new SpotRequest(VehicleType.MOTORBIKE, 42);

            when(updateSpotStatusUseCase.execute(eq(spotId), any(SpotCreateDTO.class)))
                    .thenReturn(new SpotDTO(spotId, VehicleType.MOTORBIKE, 42, SpotStatus.UNAVAILABLE));

            spotRestAdapter.updateStatus(spotId, request);

            ArgumentCaptor<SpotCreateDTO> captor = ArgumentCaptor.forClass(SpotCreateDTO.class);
            verify(updateSpotStatusUseCase).execute(eq(spotId), captor.capture());

            SpotCreateDTO usedDTO = captor.getValue();
            assertThat(usedDTO.getType()).isEqualTo(VehicleType.MOTORBIKE);
            assertThat(usedDTO.getNumSpot()).isEqualTo(42);
        }

        // Qué hace: mismo flujo que el primer test, pero comprobando el otro extremo del
        // enum SpotStatus (UNAVAILABLE) y otro VehicleType (CAR_PMR), para asegurarse de que
        // el controlador no está "hardcodeado" a un único valor y realmente traslada tal cual
        // lo que le devuelve el caso de uso.
        // Qué se espera: la respuesta sigue siendo 200 OK, y el body debe reflejar status
        // UNAVAILABLE y type CAR_PMR, es decir, exactamente lo que devolvió el mock.
        @Test
        @DisplayName("Debe reflejar en la respuesta el estado UNAVAILABLE devuelto por el caso de uso")
        void shouldReturnUnavailableStatus() {
            UUID spotId = UUID.randomUUID();
            SpotRequest request = new SpotRequest(VehicleType.CAR_PMR, 7);

            when(updateSpotStatusUseCase.execute(eq(spotId), any(SpotCreateDTO.class)))
                    .thenReturn(new SpotDTO(spotId, VehicleType.CAR_PMR, 7, SpotStatus.UNAVAILABLE));

            ResponseEntity<SpotResponse> response = spotRestAdapter.updateStatus(spotId, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(SpotStatus.UNAVAILABLE);
            assertThat(response.getBody().getType()).isEqualTo(VehicleType.CAR_PMR);
        }
    }

    @Nested
    @DisplayName("Casos de error propagados desde el caso de uso")
    class WhenUseCaseThrows {

        // Qué hace: simula que el caso de uso no encuentra ninguna plaza con el id indicado
        // y lanza SpotNotFoundException (tal como hace UpdateSpotUseCase.execute() cuando
        // spotPersistence.findById(id) devuelve un Optional vacío).
        // Qué se espera: el controlador NO debe capturar la excepción (no tiene try/catch),
        // así que debe propagarse tal cual hacia arriba. En producción es el
        // @RestControllerAdvice (CustomizedExceptionAdapter) quien la traduciría a un 404;
        // aquí solo comprobamos que llega intacta, con el mismo mensaje, hasta ese punto.
        @Test
        @DisplayName("Debe propagar SpotNotFoundException cuando la plaza no existe")
        void shouldPropagateSpotNotFoundException() {
            UUID spotId = UUID.randomUUID();
            SpotRequest request = new SpotRequest(VehicleType.CAR, 1);

            when(updateSpotStatusUseCase.execute(eq(spotId), any(SpotCreateDTO.class)))
                    .thenThrow(new SpotNotFoundException("No existe una plaza con id " + spotId));

            SpotNotFoundException thrown = assertThrows(SpotNotFoundException.class,
                    () -> spotRestAdapter.updateStatus(spotId, request));

            assertThat(thrown.getMessage()).isEqualTo("No existe una plaza con id " + spotId);
        }

        // Qué hace: simula que el caso de uso sí encuentra la plaza pero los datos nuevos son
        // inválidos (por ejemplo, numSpot negativo), lo que hace que el dominio (Spot) lance
        // InvalidSpotException al construirse.
        // Qué se espera: igual que en el caso anterior, el controlador debe dejar pasar la
        // excepción sin capturarla, para que sea CustomizedExceptionAdapter quien la convierta
        // en un 400 Bad Request en producción. Aquí se verifica que la excepción y su mensaje
        // llegan sin alterar hasta el llamador.
        @Test
        @DisplayName("Debe propagar InvalidSpotException cuando los datos actualizados son inválidos")
        void shouldPropagateInvalidSpotException() {
            UUID spotId = UUID.randomUUID();
            SpotRequest request = new SpotRequest(VehicleType.CAR, -5);

            when(updateSpotStatusUseCase.execute(eq(spotId), any(SpotCreateDTO.class)))
                    .thenThrow(new InvalidSpotException("El número de plaza debe ser mayor que 0"));

            InvalidSpotException thrown = assertThrows(InvalidSpotException.class,
                    () -> spotRestAdapter.updateStatus(spotId, request));

            assertThat(thrown.getMessage()).isEqualTo("El número de plaza debe ser mayor que 0");
        }
    }
}