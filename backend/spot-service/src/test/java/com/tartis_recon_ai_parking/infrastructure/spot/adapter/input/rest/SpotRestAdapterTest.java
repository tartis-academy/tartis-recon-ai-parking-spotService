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
import org.springframework.web.context.WebApplicationContext;
import org.junit.jupiter.api.BeforeEach;

import com.tartis_recon_ai_parking.application.spot.dto.SpotCreateDTO;
import com.tartis_recon_ai_parking.application.spot.dto.SpotDTO;
import com.tartis_recon_ai_parking.application.spot.usecase.CreateSpotUseCase;
import com.tartis_recon_ai_parking.application.spot.usecase.GetSpotUseCase;
import com.tartis_recon_ai_parking.application.spot.usecase.OccupySpotUseCase;
import com.tartis_recon_ai_parking.application.spot.usecase.ReleaseSpotUseCase;
import com.tartis_recon_ai_parking.application.spot.usecase.UpdateSpotUseCase;
import com.tartis_recon_ai_parking.domain.spot.Spot;
import com.tartis_recon_ai_parking.domain.spot.SpotStatus;
import com.tartis_recon_ai_parking.domain.spot.VehicleType;
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

    // TODO
    @MockitoBean
    private UpdateSpotUseCase updateSpotStatusUseCase;

    @MockitoBean
    private OccupySpotUseCase occupySpotUseCase;

    @MockitoBean
    private ReleaseSpotUseCase releaseSpotUseCase;

    @MockitoBean
    private SpotRestMapper spotRestMapper;

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
                .content("{\"type\":\"CAR\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(spotId.toString()))
                .andExpect(jsonPath("$.type").value("CAR"))
                .andExpect(jsonPath("$.status").value("OCCUPIED"));
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
        // - Envía una petición PATCH a /v1/spots/{id}/status con el nuevo tipo/estado.
        UUID spotId = UUID.randomUUID();
        
        SpotDTO updatedSpot = new SpotDTO(spotId, VehicleType.CAR, SpotStatus.UNAVAILABLE);
        SpotResponse mockResponse = new SpotResponse(spotId, VehicleType.CAR, SpotStatus.UNAVAILABLE);

        when(updateSpotStatusUseCase.execute(eq(spotId), any(SpotCreateDTO.class))).thenReturn(updatedSpot);
        when(spotRestMapper.toResponse(updatedSpot)).thenReturn(mockResponse);

        // QUE DEBERIA HACER:
        // Debe retornar estado 200 OK y la plaza con el estado UNAVAILABLE reflejado.
        mockMvc.perform(patch("/v1/spots/{id}/status", spotId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\":\"CAR\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(spotId.toString()))
                .andExpect(jsonPath("$.type").value("CAR"))
                .andExpect(jsonPath("$.status").value("UNAVAILABLE"));
    }
}
