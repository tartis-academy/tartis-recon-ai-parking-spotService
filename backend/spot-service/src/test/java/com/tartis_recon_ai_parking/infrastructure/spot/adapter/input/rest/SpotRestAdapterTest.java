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

    @MockitoBean
    private OccupySpotUseCase occupySpotUseCase;

    @MockitoBean
    private ReleaseSpotUseCase releaseSpotUseCase;

    @MockitoBean
    private SpotRestMapper spotRestMapper;

    @Test
    @DisplayName("POST /v1/spots - debe retornar 201 CREATED y la plaza creada")
    void create_ShouldReturnCreated_WhenSpotIsCreated() throws Exception {
        UUID spotId = UUID.randomUUID();
        
        SpotDTO createdSpot = new SpotDTO(spotId, VehicleType.CAR, SpotStatus.AVAILABLE);
        SpotResponse mockResponse = new SpotResponse(spotId, VehicleType.CAR, SpotStatus.AVAILABLE);

        when(createSpotUseCase.execute(any(SpotCreateDTO.class))).thenReturn(createdSpot);
        when(spotRestMapper.toResponse(createdSpot)).thenReturn(mockResponse);

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
        UUID spotId = UUID.randomUUID();
        SpotDTO spotDTO = new SpotDTO(spotId, VehicleType.CAR, SpotStatus.AVAILABLE);
        SpotResponse mockResponse = new SpotResponse(spotId, VehicleType.CAR, SpotStatus.AVAILABLE);

        when(getSpotUseCase.getAll()).thenReturn(List.of(spotDTO));
        when(spotRestMapper.toResponse(spotDTO)).thenReturn(mockResponse);

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
        UUID spotId = UUID.randomUUID();
        SpotDTO spotDTO = new SpotDTO(spotId, VehicleType.CAR, SpotStatus.AVAILABLE);
        SpotResponse mockResponse = new SpotResponse(spotId, VehicleType.CAR, SpotStatus.AVAILABLE);

        when(getSpotUseCase.getById(spotId)).thenReturn(spotDTO);
        when(spotRestMapper.toResponse(spotDTO)).thenReturn(mockResponse);

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
        UUID spotId = UUID.randomUUID();
        SpotDTO updatedSpot = new SpotDTO(spotId, VehicleType.MOTORBIKE, SpotStatus.AVAILABLE);
        SpotResponse mockResponse = new SpotResponse(spotId, VehicleType.MOTORBIKE, SpotStatus.AVAILABLE);

        when(updateSpotUseCase.execute(eq(spotId), any(SpotCreateDTO.class))).thenReturn(updatedSpot);
        when(spotRestMapper.toResponse(updatedSpot)).thenReturn(mockResponse);

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
        UUID spotId = UUID.randomUUID();
        Spot occupiedSpot = Spot.reconstruct(spotId, VehicleType.CAR, SpotStatus.OCCUPIED);
        SpotResponse mockResponse = new SpotResponse(spotId, VehicleType.CAR, SpotStatus.OCCUPIED);

        when(occupySpotUseCase.execute(VehicleType.CAR)).thenReturn(occupiedSpot);
        when(spotRestMapper.toResponse(any(SpotDTO.class))).thenReturn(mockResponse);

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
        UUID spotId = UUID.randomUUID();
        
        // Simular salida del UseCase
        SpotDTO releasedSpot = new SpotDTO(spotId, VehicleType.CAR, SpotStatus.AVAILABLE);
        
        // Simular salida del Mapper
        SpotResponse mockResponse = new SpotResponse(spotId, VehicleType.CAR, SpotStatus.AVAILABLE);

        when(releaseSpotUseCase.execute(spotId)).thenReturn(releasedSpot);
        when(spotRestMapper.toResponse(releasedSpot)).thenReturn(mockResponse);

        mockMvc.perform(post("/v1/spots/{id}/release", spotId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(spotId.toString()))
                .andExpect(jsonPath("$.type").value("CAR"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }
}
