package com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.context.WebApplicationContext;
import org.junit.jupiter.api.BeforeEach;

import com.tartis_recon_ai_parking.application.spot.dto.SpotAvailabilityDTO;
import com.tartis_recon_ai_parking.application.spot.dto.SpotCreateDTO;
import com.tartis_recon_ai_parking.application.spot.dto.SpotDTO;
import com.tartis_recon_ai_parking.application.spot.usecase.AvailableSpotUseCase;
import com.tartis_recon_ai_parking.application.spot.usecase.CreateSpotUseCase;
import com.tartis_recon_ai_parking.application.spot.usecase.GetSpotUseCase;
import com.tartis_recon_ai_parking.application.spot.usecase.OccupySpotUseCase;
import com.tartis_recon_ai_parking.application.spot.usecase.ReleaseSpotUseCase;
import com.tartis_recon_ai_parking.application.spot.usecase.UpdateSpotStatusUseCase;
import com.tartis_recon_ai_parking.application.spot.usecase.UpdateSpotUseCase;
import com.tartis_recon_ai_parking.domain.spot.Spot;
import com.tartis_recon_ai_parking.domain.spot.SpotStatus;
import com.tartis_recon_ai_parking.domain.spot.VehicleType;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotCannotBeBlockedException;
import com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest.dto.response.AvailabilityResponse;
import com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest.dto.response.SpotResponse;

@SpringBootTest
class SpotRestAdapterTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @MockitoBean
    private CreateSpotUseCase createSpotUseCase;

    @MockitoBean
    private GetSpotUseCase getSpotUseCase;

    @MockitoBean
    private UpdateSpotUseCase updateSpotUseCase;

    @MockitoBean
    private UpdateSpotStatusUseCase updateSpotStatusUseCase;

    @MockitoBean
    private OccupySpotUseCase occupySpotUseCase;

    @MockitoBean
    private ReleaseSpotUseCase releaseSpotUseCase;

    @MockitoBean
    private SpotRestMapper spotRestMapper;

    @MockitoBean
    private AvailableSpotUseCase availableSpotUseCase;

    @Test
    @DisplayName("POST /v1/spots - debe retornar 201 CREATED y la plaza creada")
    void create_ShouldReturnCreated_WhenSpotIsCreated() throws Exception {
        // QUE HACE:
        // - Genera mocks para el retorno del caso de uso y el mapper.
        // - Realiza una petición POST a /v1/spots con un JSON válido.
        UUID spotId = UUID.randomUUID();
        
        SpotDTO createdSpot = new SpotDTO(spotId, VehicleType.CAR, SpotStatus.AVAILABLE);
        SpotResponse mockResponse = new SpotResponse(spotId, VehicleType.CAR, SpotStatus.AVAILABLE);

        when(createSpotUseCase.execute(any(SpotCreateDTO.class))).thenReturn(createdSpot);
        when(spotRestMapper.toResponse(createdSpot)).thenReturn(mockResponse);

        // QUE DEBERIA HACER:
        // Debe retornar estado 201 CREATED y el JSON de la plaza creada coincidente.
        mockMvc.perform(post("/v1/spots")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\":\"CAR\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(spotId.toString()))
                .andExpect(jsonPath("$.type").value("CAR"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    @DisplayName("GET /v1/spots - debe retornar 200 OK y la lista de plazas")
    void getAll_ShouldReturnOk_WithSpotsList() throws Exception {
        // QUE HACE:
        // - Configura el caso de uso para devolver una lista de plazas.
        // - Configura el mapper para convertir cada plaza al formato de respuesta.
        // - Ejecuta una petición GET a /v1/spots.
        UUID spotId = UUID.randomUUID();
        SpotDTO spotDTO = new SpotDTO(spotId, VehicleType.CAR, SpotStatus.AVAILABLE);
        SpotResponse mockResponse = new SpotResponse(spotId, VehicleType.CAR, SpotStatus.AVAILABLE);

        when(getSpotUseCase.getAll()).thenReturn(List.of(spotDTO));
        when(spotRestMapper.toResponse(spotDTO)).thenReturn(mockResponse);

        // QUE DEBERIA HACER:
        // Debe retornar estado 200 OK y una lista JSON con las plazas.
        mockMvc.perform(get("/v1/spots")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(spotId.toString()))
                .andExpect(jsonPath("$[0].type").value("CAR"))
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"));
    }

    @Test
    @DisplayName("GET /v1/spots/{id} - debe retornar 200 OK y la plaza")
    void getById_ShouldReturnOk_WhenSpotExists() throws Exception {
        // QUE HACE:
        // - Simula la obtención de una plaza por ID desde el caso de uso.
        // - Ejecuta una petición GET a /v1/spots/{id}.
        UUID spotId = UUID.randomUUID();
        SpotDTO spotDTO = new SpotDTO(spotId, VehicleType.CAR, SpotStatus.AVAILABLE);
        SpotResponse mockResponse = new SpotResponse(spotId, VehicleType.CAR, SpotStatus.AVAILABLE);

        when(getSpotUseCase.getById(spotId)).thenReturn(spotDTO);
        when(spotRestMapper.toResponse(spotDTO)).thenReturn(mockResponse);

        // QUE DEBERIA HACER:
        // Debe retornar estado 200 OK y los datos de la plaza solicitada.
        mockMvc.perform(get("/v1/spots/{id}", spotId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(spotId.toString()))
                .andExpect(jsonPath("$.type").value("CAR"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    @DisplayName("PUT /v1/spots/{id} - debe retornar 200 OK y la plaza actualizada")
    void update_ShouldReturnOk_WhenSpotIsUpdated() throws Exception {
        // QUE HACE:
        // - Configura el mock del caso de uso de actualización para retornar una plaza.
        // - Realiza una petición PUT a /v1/spots/{id} con datos en JSON.
        UUID spotId = UUID.randomUUID();
        SpotDTO updatedSpot = new SpotDTO(spotId, VehicleType.MOTORBIKE, SpotStatus.AVAILABLE);
        SpotResponse mockResponse = new SpotResponse(spotId, VehicleType.MOTORBIKE, SpotStatus.AVAILABLE);

        when(updateSpotUseCase.execute(eq(spotId), any(SpotCreateDTO.class))).thenReturn(updatedSpot);
        when(spotRestMapper.toResponse(updatedSpot)).thenReturn(mockResponse);

        // QUE DEBERIA HACER:
        // Debe retornar estado 200 OK y la información actualizada.
        mockMvc.perform(put("/v1/spots/{id}", spotId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\":\"MOTORBIKE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(spotId.toString()))
                .andExpect(jsonPath("$.type").value("MOTORBIKE"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    @DisplayName("POST /v1/spots/occupy - debe retornar 200 OK y la plaza ocupada")
    void occupy_ShouldReturnOk_WhenSpotIsOccupied() throws Exception {
        // QUE HACE:
        // - Configura el caso de uso de ocupación para devolver una plaza ocupada.
        // - Envía una petición POST a /v1/spots/occupy indicando el tipo de vehículo.
        UUID spotId = UUID.randomUUID();
        Spot occupiedSpot = Spot.reconstruct(spotId, VehicleType.CAR, SpotStatus.OCCUPIED);
        SpotResponse mockResponse = new SpotResponse(spotId, VehicleType.CAR, SpotStatus.OCCUPIED);

        when(occupySpotUseCase.execute(VehicleType.CAR)).thenReturn(occupiedSpot);
        when(spotRestMapper.toResponse(any(SpotDTO.class))).thenReturn(mockResponse);

        // QUE DEBERIA HACER:
        // Debe retornar estado 200 OK y los datos reflejando el estado OCCUPIED.
        mockMvc.perform(post("/v1/spots/occupy")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"vehicleType\":\"CAR\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(spotId.toString()))
                .andExpect(jsonPath("$.type").value("CAR"))
                .andExpect(jsonPath("$.status").value("OCCUPIED"));
    }

    @Test
    @DisplayName("POST /v1/spots/occupy - debe seguir aceptando el payload antiguo con 'type'")
    void occupy_ShouldAcceptLegacyTypeKey() throws Exception {
        // QUE HACE:
        // - Envia el payload que manda stay-service hoy, con la clave 'type'.
        UUID spotId = UUID.randomUUID();
        Spot occupiedSpot = Spot.reconstruct(spotId, VehicleType.CAR, SpotStatus.OCCUPIED);
        SpotResponse mockResponse = new SpotResponse(spotId, VehicleType.CAR, SpotStatus.OCCUPIED);

        when(occupySpotUseCase.execute(VehicleType.CAR)).thenReturn(occupiedSpot);
        when(spotRestMapper.toResponse(any(SpotDTO.class))).thenReturn(mockResponse);

        // QUE DEBERIA HACER:
        // Debe resolverlo igual que vehicleType: el alias es lo que permite
        // desplegar los dos servicios en cualquier orden.
        mockMvc.perform(post("/v1/spots/occupy")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\":\"CAR\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(spotId.toString()));
    }

    @Test
    @DisplayName("POST /v1/spots/{id}/release deberia retornar 200 OK y la plaza liberada")
    void release_ShouldReturnOk_WhenSpotIsReleased() throws Exception {
        // QUE HACE:
        // - Simula la salida del UseCase para liberar una plaza.
        // - Envía una petición POST a /v1/spots/{id}/release.
        UUID spotId = UUID.randomUUID();
        
        SpotDTO releasedSpot = new SpotDTO(spotId, VehicleType.CAR, SpotStatus.AVAILABLE);
        SpotResponse mockResponse = new SpotResponse(spotId, VehicleType.CAR, SpotStatus.AVAILABLE);

        when(releaseSpotUseCase.execute(spotId)).thenReturn(releasedSpot);
        when(spotRestMapper.toResponse(releasedSpot)).thenReturn(mockResponse);

        // QUE DEBERIA HACER:
        // Debe retornar estado 200 OK confirmando que el estado ahora es AVAILABLE.
        mockMvc.perform(post("/v1/spots/{id}/release", spotId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(spotId.toString()))
                .andExpect(jsonPath("$.type").value("CAR"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }
    @Test
    @DisplayName("PATCH /v1/spots/{id}/status - debe retornar 200 OK y la plaza con el estado actualizado")
    void updateStatus_ShouldReturnOk_WhenSpotStatusIsUpdated() throws Exception {
        // QUE HACE:
        // - Simula la actualización del estado de una plaza en el UseCase.
        // - Envía una petición PATCH a /v1/spots/{id}/status con el nuevo estado.
        UUID spotId = UUID.randomUUID();

        Spot updatedSpot = Spot.reconstruct(spotId, VehicleType.CAR, SpotStatus.UNAVAILABLE);
        SpotResponse mockResponse = new SpotResponse(spotId, VehicleType.CAR, SpotStatus.UNAVAILABLE);

        when(updateSpotStatusUseCase.execute(spotId, SpotStatus.UNAVAILABLE)).thenReturn(updatedSpot);
        when(spotRestMapper.toResponse(any(SpotDTO.class))).thenReturn(mockResponse);

        // QUE DEBERIA HACER:
        // Debe retornar estado 200 OK y la plaza con el estado UNAVAILABLE reflejado.
        mockMvc.perform(patch("/v1/spots/{id}/status", spotId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"UNAVAILABLE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(spotId.toString()))
                .andExpect(jsonPath("$.type").value("CAR"))
                .andExpect(jsonPath("$.status").value("UNAVAILABLE"));
    }

    @Test
    @DisplayName("PATCH /v1/spots/{id}/status - debe retornar 409 CONFLICT si la transicion no esta permitida")
    void updateStatus_ShouldReturnConflict_WhenTransitionIsForbidden() throws Exception {
        // QUE HACE:
        // - Configura el UseCase para lanzar SpotCannotBeBlockedException (plaza OCCUPIED o OCCUPIED solicitado).
        // - Envía una petición PATCH a /v1/spots/{id}/status.
        UUID spotId = UUID.randomUUID();

        when(updateSpotStatusUseCase.execute(spotId, SpotStatus.UNAVAILABLE))
                .thenThrow(new SpotCannotBeBlockedException("Para poner una plaza en mantenimiento, debe estar DISPONIBLE"));

        // QUE DEBERIA HACER:
        // Debe retornar estado 409 CONFLICT traducido por CustomizedExceptionAdapter.
        mockMvc.perform(patch("/v1/spots/{id}/status", spotId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"UNAVAILABLE\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /v1/spots/availability - debe retornar 200 OK con los contadores del tipo (HU-03)")
    void checkAvailability_ShouldReturnOk_WithCounts() throws Exception {
        // QUE HACE:
        // - Configura el caso de uso y el mapper para el tipo CAR.
        // - Ejecuta un GET a la ruta del contrato, /v1/spots/availability.
        SpotAvailabilityDTO availability = new SpotAvailabilityDTO(VehicleType.CAR, 7L, 20L);
        AvailabilityResponse mockResponse = new AvailabilityResponse(VehicleType.CAR, true, 7L, 20L);

        when(availableSpotUseCase.execute(VehicleType.CAR)).thenReturn(availability);
        when(spotRestMapper.toResponse(availability)).thenReturn(mockResponse);

        // QUE DEBERIA HACER:
        // Debe responder en la ruta del openapi.yml y con el cuerpo AvailabilityResponse,
        // no un booleano suelto.
        mockMvc.perform(get("/v1/spots/availability").param("type", "CAR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("CAR"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.availableCount").value(7))
                .andExpect(jsonPath("$.totalCount").value(20));
    }

    @Test
    @DisplayName("GET /v1/spots/availability sin type - debe retornar 400, no 500")
    void checkAvailability_ShouldReturnBadRequest_WhenTypeIsMissing() throws Exception {
        mockMvc.perform(get("/v1/spots/availability"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("Ruta inexistente - debe retornar 404, no 500")
    void unknownPath_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/v1/spotss"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("La ruta antigua /available cae en GET /{id} y da 400, no 500")
    void legacyAvailablePath_ShouldReturnBadRequest() throws Exception {
        // /v1/spots/available encaja con el mapping /{id} e intenta parsear
        // "available" como UUID. Antes salia como 500 por el catch-all.
        mockMvc.perform(get("/v1/spots/available").param("type", "CAR"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("Metodo no soportado - debe retornar 405, no 500")
    void unsupportedMethod_ShouldReturnMethodNotAllowed() throws Exception {
        mockMvc.perform(delete("/v1/spots/{id}", UUID.randomUUID()))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
    }

    @Test
    @DisplayName("Content-Type no soportado - debe retornar 415, no 500")
    void unsupportedMediaType_ShouldReturnUnsupportedMediaType() throws Exception {
        mockMvc.perform(post("/v1/spots")
                .contentType(MediaType.TEXT_PLAIN)
                .content("type=CAR"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415));
    }

    @Test
    @DisplayName("BD inaccesible - debe retornar 503, no 500")
    void databaseDown_ShouldReturnServiceUnavailable() throws Exception {
        // QUE HACE:
        // - Simula Postgres caido. Los casos de uso son @Transactional, asi que
        //   Spring falla al ABRIR la transaccion y lanza
        //   CannotCreateTransactionException, que no es una DataAccessException.
        when(occupySpotUseCase.execute(VehicleType.CAR))
                .thenThrow(new CannotCreateTransactionException("Could not open JPA EntityManager"));

        // QUE DEBERIA HACER:
        // Debe salir como 503 con el mensaje sanitizado. Se ejerce via MockMvc a
        // proposito: lo que hay que comprobar no es el cuerpo del handler, sino
        // que Spring resuelva esta excepcion a handleDatabaseUnavailable y no al
        // catch-all de Exception.
        mockMvc.perform(post("/v1/spots/occupy")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"vehicleType\":\"CAR\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.message").value("El servicio de datos no está disponible temporalmente."));
    }
}
