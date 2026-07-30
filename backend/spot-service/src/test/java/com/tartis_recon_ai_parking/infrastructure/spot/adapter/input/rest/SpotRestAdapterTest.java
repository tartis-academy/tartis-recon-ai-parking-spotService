package com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.context.WebApplicationContext;

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

    private static final SimpleGrantedAuthority ADMIN_AUTHORITY = new SimpleGrantedAuthority("ROLE_ADMIN");
    private static final SimpleGrantedAuthority OPERARIO_AUTHORITY = new SimpleGrantedAuthority("ROLE_OPERARIO");
    private static final SimpleGrantedAuthority USER_AUTHORITY = new SimpleGrantedAuthority("ROLE_USER");

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
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

    // =========================================================================
    // PRUEBAS DE FUNCIONALIDAD (ROL: ADMIN)
    // =========================================================================

    @Test
    @DisplayName("POST /v1/spots - debe retornar 201 CREATED y la plaza creada (ADMIN)")
    void create_ShouldReturnCreated_WhenSpotIsCreated() throws Exception {
        UUID spotId = UUID.randomUUID();
        
        SpotDTO createdSpot = new SpotDTO(spotId, VehicleType.CAR, SpotStatus.AVAILABLE);
        SpotResponse mockResponse = new SpotResponse(spotId, VehicleType.CAR, SpotStatus.AVAILABLE);

        when(createSpotUseCase.execute(any(SpotCreateDTO.class))).thenReturn(createdSpot);
        when(spotRestMapper.toResponse(createdSpot)).thenReturn(mockResponse);

        mockMvc.perform(post("/v1/spots")
                .with(jwt().authorities(ADMIN_AUTHORITY))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\":\"CAR\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(spotId.toString()))
                .andExpect(jsonPath("$.type").value("CAR"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    @DisplayName("GET /v1/spots - debe retornar 200 OK y la lista de plazas (ADMIN)")
    void getAll_ShouldReturnOk_WithSpotsList() throws Exception {
        UUID spotId = UUID.randomUUID();
        SpotDTO spotDTO = new SpotDTO(spotId, VehicleType.CAR, SpotStatus.AVAILABLE);
        SpotResponse mockResponse = new SpotResponse(spotId, VehicleType.CAR, SpotStatus.AVAILABLE);

        when(getSpotUseCase.getAll()).thenReturn(List.of(spotDTO));
        when(spotRestMapper.toResponse(spotDTO)).thenReturn(mockResponse);

        mockMvc.perform(get("/v1/spots")
                .with(jwt().authorities(ADMIN_AUTHORITY))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(spotId.toString()))
                .andExpect(jsonPath("$[0].type").value("CAR"))
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"));
    }

    @Test
    @DisplayName("GET /v1/spots/{id} - debe retornar 200 OK y la plaza (ADMIN)")
    void getById_ShouldReturnOk_WhenSpotExists() throws Exception {
        UUID spotId = UUID.randomUUID();
        SpotDTO spotDTO = new SpotDTO(spotId, VehicleType.CAR, SpotStatus.AVAILABLE);
        SpotResponse mockResponse = new SpotResponse(spotId, VehicleType.CAR, SpotStatus.AVAILABLE);

        when(getSpotUseCase.getById(spotId)).thenReturn(spotDTO);
        when(spotRestMapper.toResponse(spotDTO)).thenReturn(mockResponse);

        mockMvc.perform(get("/v1/spots/{id}", spotId)
                .with(jwt().authorities(ADMIN_AUTHORITY))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(spotId.toString()))
                .andExpect(jsonPath("$.type").value("CAR"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    @DisplayName("PUT /v1/spots/{id} - debe retornar 200 OK y la plaza actualizada (ADMIN)")
    void update_ShouldReturnOk_WhenSpotIsUpdated() throws Exception {
        UUID spotId = UUID.randomUUID();
        SpotDTO updatedSpot = new SpotDTO(spotId, VehicleType.MOTORBIKE, SpotStatus.AVAILABLE);
        SpotResponse mockResponse = new SpotResponse(spotId, VehicleType.MOTORBIKE, SpotStatus.AVAILABLE);

        when(updateSpotUseCase.execute(eq(spotId), any(SpotCreateDTO.class))).thenReturn(updatedSpot);
        when(spotRestMapper.toResponse(updatedSpot)).thenReturn(mockResponse);

        mockMvc.perform(put("/v1/spots/{id}", spotId)
                .with(jwt().authorities(ADMIN_AUTHORITY))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\":\"MOTORBIKE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(spotId.toString()))
                .andExpect(jsonPath("$.type").value("MOTORBIKE"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    @DisplayName("POST /v1/spots/occupy - debe retornar 200 OK y la plaza ocupada (ADMIN)")
    void occupy_ShouldReturnOk_WhenSpotIsOccupied() throws Exception {
        UUID spotId = UUID.randomUUID();
        Spot occupiedSpot = Spot.reconstruct(spotId, VehicleType.CAR, SpotStatus.OCCUPIED);
        SpotResponse mockResponse = new SpotResponse(spotId, VehicleType.CAR, SpotStatus.OCCUPIED);

        when(occupySpotUseCase.execute(VehicleType.CAR)).thenReturn(occupiedSpot);
        when(spotRestMapper.toResponse(any(SpotDTO.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/v1/spots/occupy")
                .with(jwt().authorities(ADMIN_AUTHORITY))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"vehicleType\":\"CAR\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(spotId.toString()))
                .andExpect(jsonPath("$.type").value("CAR"))
                .andExpect(jsonPath("$.status").value("OCCUPIED"));
    }

    @Test
    @DisplayName("POST /v1/spots/occupy - debe seguir aceptando el payload antiguo con 'type' (ADMIN)")
    void occupy_ShouldAcceptLegacyTypeKey() throws Exception {
        UUID spotId = UUID.randomUUID();
        Spot occupiedSpot = Spot.reconstruct(spotId, VehicleType.CAR, SpotStatus.OCCUPIED);
        SpotResponse mockResponse = new SpotResponse(spotId, VehicleType.CAR, SpotStatus.OCCUPIED);

        when(occupySpotUseCase.execute(VehicleType.CAR)).thenReturn(occupiedSpot);
        when(spotRestMapper.toResponse(any(SpotDTO.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/v1/spots/occupy")
                .with(jwt().authorities(ADMIN_AUTHORITY))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\":\"CAR\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(spotId.toString()));
    }

    @Test
    @DisplayName("POST /v1/spots/{id}/release deberia retornar 200 OK y la plaza liberada (ADMIN)")
    void release_ShouldReturnOk_WhenSpotIsReleased() throws Exception {
        UUID spotId = UUID.randomUUID();
        
        SpotDTO releasedSpot = new SpotDTO(spotId, VehicleType.CAR, SpotStatus.AVAILABLE);
        SpotResponse mockResponse = new SpotResponse(spotId, VehicleType.CAR, SpotStatus.AVAILABLE);

        when(releaseSpotUseCase.execute(spotId)).thenReturn(releasedSpot);
        when(spotRestMapper.toResponse(releasedSpot)).thenReturn(mockResponse);

        mockMvc.perform(post("/v1/spots/{id}/release", spotId)
                .with(jwt().authorities(ADMIN_AUTHORITY))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(spotId.toString()))
                .andExpect(jsonPath("$.type").value("CAR"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    @DisplayName("PATCH /v1/spots/{id}/status - debe retornar 200 OK y la plaza con el estado actualizado (ADMIN)")
    void updateStatus_ShouldReturnOk_WhenSpotStatusIsUpdated() throws Exception {
        UUID spotId = UUID.randomUUID();

        Spot updatedSpot = Spot.reconstruct(spotId, VehicleType.CAR, SpotStatus.UNAVAILABLE);
        SpotResponse mockResponse = new SpotResponse(spotId, VehicleType.CAR, SpotStatus.UNAVAILABLE);

        when(updateSpotStatusUseCase.execute(spotId, SpotStatus.UNAVAILABLE)).thenReturn(updatedSpot);
        when(spotRestMapper.toResponse(any(SpotDTO.class))).thenReturn(mockResponse);

        mockMvc.perform(patch("/v1/spots/{id}/status", spotId)
                .with(jwt().authorities(ADMIN_AUTHORITY))
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
        UUID spotId = UUID.randomUUID();

        when(updateSpotStatusUseCase.execute(spotId, SpotStatus.UNAVAILABLE))
                .thenThrow(new SpotCannotBeBlockedException("Para poner una plaza en mantenimiento, debe estar DISPONIBLE"));

        mockMvc.perform(patch("/v1/spots/{id}/status", spotId)
                .with(jwt().authorities(ADMIN_AUTHORITY))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"UNAVAILABLE\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /v1/spots/availability - debe retornar 200 OK con los contadores del tipo (ADMIN)")
    void checkAvailability_ShouldReturnOk_WithCounts() throws Exception {
        SpotAvailabilityDTO availability = new SpotAvailabilityDTO(VehicleType.CAR, 7L, 20L);
        AvailabilityResponse mockResponse = new AvailabilityResponse(VehicleType.CAR, true, 7L, 20L);

        when(availableSpotUseCase.execute(VehicleType.CAR)).thenReturn(availability);
        when(spotRestMapper.toResponse(availability)).thenReturn(mockResponse);

        mockMvc.perform(get("/v1/spots/availability").with(jwt().authorities(ADMIN_AUTHORITY)).param("type", "CAR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("CAR"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.availableCount").value(7))
                .andExpect(jsonPath("$.totalCount").value(20));
    }

    // =========================================================================
    // PRUEBAS DE AUTORIZACIÓN FINA SEC-10 (ROLES: OPERARIO, USER, UNAUTHENTICATED)
    // =========================================================================

    @Nested
    @DisplayName("SEC-10: Pruebas de permisos para ROL OPERARIO")
    class OperarioPermissionsTests {

        @Test
        @DisplayName("OPERARIO: Permite listar todas las plazas (SS-01 - 200 OK)")
        void operario_ShouldAllowGetAll() throws Exception {
            UUID spotId = UUID.randomUUID();
            SpotDTO spotDTO = new SpotDTO(spotId, VehicleType.CAR, SpotStatus.AVAILABLE);
            SpotResponse mockResponse = new SpotResponse(spotId, VehicleType.CAR, SpotStatus.AVAILABLE);

            when(getSpotUseCase.getAll()).thenReturn(List.of(spotDTO));
            when(spotRestMapper.toResponse(spotDTO)).thenReturn(mockResponse);

            mockMvc.perform(get("/v1/spots")
                    .with(jwt().authorities(OPERARIO_AUTHORITY)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("OPERARIO: Permite obtener plaza por ID (SS-03 - 200 OK)")
        void operario_ShouldAllowGetById() throws Exception {
            UUID spotId = UUID.randomUUID();
            SpotDTO spotDTO = new SpotDTO(spotId, VehicleType.CAR, SpotStatus.AVAILABLE);
            SpotResponse mockResponse = new SpotResponse(spotId, VehicleType.CAR, SpotStatus.AVAILABLE);

            when(getSpotUseCase.getById(spotId)).thenReturn(spotDTO);
            when(spotRestMapper.toResponse(spotDTO)).thenReturn(mockResponse);

            mockMvc.perform(get("/v1/spots/{id}", spotId)
                    .with(jwt().authorities(OPERARIO_AUTHORITY)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("OPERARIO: Permite consultar disponibilidad (SS-05 - 200 OK)")
        void operario_ShouldAllowCheckAvailability() throws Exception {
            SpotAvailabilityDTO availability = new SpotAvailabilityDTO(VehicleType.CAR, 5L, 10L);
            AvailabilityResponse mockResponse = new AvailabilityResponse(VehicleType.CAR, true, 5L, 10L);

            when(availableSpotUseCase.execute(VehicleType.CAR)).thenReturn(availability);
            when(spotRestMapper.toResponse(availability)).thenReturn(mockResponse);

            mockMvc.perform(get("/v1/spots/availability")
                    .with(jwt().authorities(OPERARIO_AUTHORITY))
                    .param("type", "CAR"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("OPERARIO: Permite cambiar estado de plaza para mantenimiento (SS-08 - 200 OK)")
        void operario_ShouldAllowUpdateStatus() throws Exception {
            UUID spotId = UUID.randomUUID();
            Spot updatedSpot = Spot.reconstruct(spotId, VehicleType.CAR, SpotStatus.UNAVAILABLE);
            SpotResponse mockResponse = new SpotResponse(spotId, VehicleType.CAR, SpotStatus.UNAVAILABLE);

            when(updateSpotStatusUseCase.execute(spotId, SpotStatus.UNAVAILABLE)).thenReturn(updatedSpot);
            when(spotRestMapper.toResponse(any(SpotDTO.class))).thenReturn(mockResponse);

            mockMvc.perform(patch("/v1/spots/{id}/status", spotId)
                    .with(jwt().authorities(OPERARIO_AUTHORITY))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"status\":\"UNAVAILABLE\"}"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("OPERARIO: Deniega crear nuevas plazas (SS-02 - 403 FORBIDDEN)")
        void operario_ShouldDenyCreate() throws Exception {
            mockMvc.perform(post("/v1/spots")
                    .with(jwt().authorities(OPERARIO_AUTHORITY))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"type\":\"CAR\"}"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.message").value("No tiene permisos para realizar esta acción."));

            verify(createSpotUseCase, never()).execute(any());
        }

        @Test
        @DisplayName("OPERARIO: Deniega actualizar definicion de plaza (SS-04 - 403 FORBIDDEN)")
        void operario_ShouldDenyUpdate() throws Exception {
            UUID spotId = UUID.randomUUID();
            mockMvc.perform(put("/v1/spots/{id}", spotId)
                    .with(jwt().authorities(OPERARIO_AUTHORITY))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"type\":\"MOTORBIKE\"}"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403));

            verify(updateSpotUseCase, never()).execute(any(), any());
        }

        @Test
        @DisplayName("OPERARIO: Deniega ocupacion directa de plaza (SS-06 - 403 FORBIDDEN)")
        void operario_ShouldDenyOccupy() throws Exception {
            mockMvc.perform(post("/v1/spots/occupy")
                    .with(jwt().authorities(OPERARIO_AUTHORITY))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"vehicleType\":\"CAR\"}"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403));

            verify(occupySpotUseCase, never()).execute(any());
        }

        @Test
        @DisplayName("OPERARIO: Deniega liberacion directa de plaza (SS-07 - 403 FORBIDDEN)")
        void operario_ShouldDenyRelease() throws Exception {
            UUID spotId = UUID.randomUUID();
            mockMvc.perform(post("/v1/spots/{id}/release", spotId)
                    .with(jwt().authorities(OPERARIO_AUTHORITY)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403));

            verify(releaseSpotUseCase, never()).execute(any());
        }
    }

    @Nested
    @DisplayName("SEC-10: Pruebas de permisos para ROL USER")
    class UserPermissionsTests {

        @Test
        @DisplayName("USER: Deniega listar plazas (SS-01 - 403 FORBIDDEN)")
        void user_ShouldDenyGetAll() throws Exception {
            mockMvc.perform(get("/v1/spots")
                    .with(jwt().authorities(USER_AUTHORITY)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("USER: Deniega crear plaza (SS-02 - 403 FORBIDDEN)")
        void user_ShouldDenyCreate() throws Exception {
            mockMvc.perform(post("/v1/spots")
                    .with(jwt().authorities(USER_AUTHORITY))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"type\":\"CAR\"}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("USER: Deniega obtener plaza por ID (SS-03 - 403 FORBIDDEN)")
        void user_ShouldDenyGetById() throws Exception {
            UUID spotId = UUID.randomUUID();
            mockMvc.perform(get("/v1/spots/{id}", spotId)
                    .with(jwt().authorities(USER_AUTHORITY)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("USER: Deniega actualizar plaza (SS-04 - 403 FORBIDDEN)")
        void user_ShouldDenyUpdate() throws Exception {
            UUID spotId = UUID.randomUUID();
            mockMvc.perform(put("/v1/spots/{id}", spotId)
                    .with(jwt().authorities(USER_AUTHORITY))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"type\":\"CAR\"}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("USER: Deniega consultar disponibilidad de plazas (SS-05 - 403 FORBIDDEN)")
        void user_ShouldDenyCheckAvailability() throws Exception {
            mockMvc.perform(get("/v1/spots/availability")
                    .with(jwt().authorities(USER_AUTHORITY))
                    .param("type", "CAR"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("USER: Deniega ocupar plaza (SS-06 - 403 FORBIDDEN)")
        void user_ShouldDenyOccupy() throws Exception {
            mockMvc.perform(post("/v1/spots/occupy")
                    .with(jwt().authorities(USER_AUTHORITY))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"vehicleType\":\"CAR\"}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("USER: Deniega liberar plaza (SS-07 - 403 FORBIDDEN)")
        void user_ShouldDenyRelease() throws Exception {
            UUID spotId = UUID.randomUUID();
            mockMvc.perform(post("/v1/spots/{id}/release", spotId)
                    .with(jwt().authorities(USER_AUTHORITY)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("USER: Deniega cambiar estado de plaza (SS-08 - 403 FORBIDDEN)")
        void user_ShouldDenyUpdateStatus() throws Exception {
            UUID spotId = UUID.randomUUID();
            mockMvc.perform(patch("/v1/spots/{id}/status", spotId)
                    .with(jwt().authorities(USER_AUTHORITY))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"status\":\"UNAVAILABLE\"}"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("SEC-10: Pruebas de peticiones sin token JWT (UNAUTHENTICATED)")
    class UnauthenticatedPermissionsTests {

        @Test
        @DisplayName("UNAUTHENTICATED: Deniega listar plazas (SS-01 - 401 UNAUTHORIZED)")
        void unauthenticated_ShouldDenyGetAll() throws Exception {
            mockMvc.perform(get("/v1/spots"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("UNAUTHENTICATED: Deniega crear plaza (SS-02 - 401 UNAUTHORIZED)")
        void unauthenticated_ShouldDenyCreate() throws Exception {
            mockMvc.perform(post("/v1/spots")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"type\":\"CAR\"}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("UNAUTHENTICATED: Deniega obtener plaza por ID (SS-03 - 401 UNAUTHORIZED)")
        void unauthenticated_ShouldDenyGetById() throws Exception {
            UUID spotId = UUID.randomUUID();
            mockMvc.perform(get("/v1/spots/{id}", spotId))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("UNAUTHENTICATED: Deniega actualizar plaza (SS-04 - 401 UNAUTHORIZED)")
        void unauthenticated_ShouldDenyUpdate() throws Exception {
            UUID spotId = UUID.randomUUID();
            mockMvc.perform(put("/v1/spots/{id}", spotId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"type\":\"CAR\"}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("UNAUTHENTICATED: Deniega consultar disponibilidad (SS-05 - 401 UNAUTHORIZED)")
        void unauthenticated_ShouldDenyCheckAvailability() throws Exception {
            mockMvc.perform(get("/v1/spots/availability")
                    .param("type", "CAR"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("UNAUTHENTICATED: Deniega ocupar plaza (SS-06 - 401 UNAUTHORIZED)")
        void unauthenticated_ShouldDenyOccupy() throws Exception {
            mockMvc.perform(post("/v1/spots/occupy")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"vehicleType\":\"CAR\"}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("UNAUTHENTICATED: Deniega liberar plaza (SS-07 - 401 UNAUTHORIZED)")
        void unauthenticated_ShouldDenyRelease() throws Exception {
            UUID spotId = UUID.randomUUID();
            mockMvc.perform(post("/v1/spots/{id}/release", spotId))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("UNAUTHENTICATED: Deniega cambiar estado de plaza (SS-08 - 401 UNAUTHORIZED)")
        void unauthenticated_ShouldDenyUpdateStatus() throws Exception {
            UUID spotId = UUID.randomUUID();
            mockMvc.perform(patch("/v1/spots/{id}/status", spotId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"status\":\"UNAVAILABLE\"}"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================================
    // PRUEBAS DE ERRORES Y CASOS LIMITE DE INFRAESTRUCTURA
    // =========================================================================

    @Test
    @DisplayName("GET /v1/spots/availability sin type - debe retornar 400, no 500")
    void checkAvailability_ShouldReturnBadRequest_WhenTypeIsMissing() throws Exception {
        mockMvc.perform(get("/v1/spots/availability").with(jwt().authorities(ADMIN_AUTHORITY)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("Ruta inexistente - debe retornar 404, no 500")
    void unknownPath_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/v1/spotss").with(jwt().authorities(ADMIN_AUTHORITY)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("La ruta antigua /available cae en GET /{id} y da 400, no 500")
    void legacyAvailablePath_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/v1/spots/available").with(jwt().authorities(ADMIN_AUTHORITY)).param("type", "CAR"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("Metodo no soportado - debe retornar 405, no 500")
    void unsupportedMethod_ShouldReturnMethodNotAllowed() throws Exception {
        mockMvc.perform(delete("/v1/spots/{id}", UUID.randomUUID()).with(jwt().authorities(ADMIN_AUTHORITY)))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
    }

    @Test
    @DisplayName("Content-Type no soportado - debe retornar 415, no 500")
    void unsupportedMediaType_ShouldReturnUnsupportedMediaType() throws Exception {
        mockMvc.perform(post("/v1/spots")
                .with(jwt().authorities(ADMIN_AUTHORITY))
                .contentType(MediaType.TEXT_PLAIN)
                .content("type=CAR"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415));
    }

    @Test
    @DisplayName("BD inaccesible - debe retornar 503, no 500")
    void databaseDown_ShouldReturnServiceUnavailable() throws Exception {
        when(occupySpotUseCase.execute(VehicleType.CAR))
                .thenThrow(new CannotCreateTransactionException("Could not open JPA EntityManager"));

        mockMvc.perform(post("/v1/spots/occupy")
                .with(jwt().authorities(ADMIN_AUTHORITY))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"vehicleType\":\"CAR\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.message").value("El servicio de datos no está disponible temporalmente."));
    }
}
