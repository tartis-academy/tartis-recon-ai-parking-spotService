package com.tartis_recon_ai_parking.infrastructure.config;

import com.tartis_recon_ai_parking.application.spot.usecase.*;
import com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest.SpotRestMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SEC-11: verifica que el exceptionHandling de SecurityConfig enruta correctamente
 * los errores 401 (sin token) y 403 (rol insuficiente) a través del
 * HandlerExceptionResolver → CustomizedExceptionAdapter, produciendo un
 * ErrorResponse con estructura definida en lugar de la respuesta por defecto de Spring.
 */
@SpringBootTest
class SecurityConfigExceptionHandlingTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean private CreateSpotUseCase createSpotUseCase;
    @MockitoBean private GetSpotUseCase getSpotUseCase;
    @MockitoBean private UpdateSpotUseCase updateSpotUseCase;
    @MockitoBean private UpdateSpotStatusUseCase updateSpotStatusUseCase;
    @MockitoBean private OccupySpotUseCase occupySpotUseCase;
    @MockitoBean private ReleaseSpotUseCase releaseSpotUseCase;
    @MockitoBean private SpotRestMapper spotRestMapper;
    @MockitoBean private AvailableSpotUseCase availableSpotUseCase;
    @MockitoBean private JwtDecoder jwtDecoder;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("SEC-11: Sin token, el authenticationEntryPoint enruta a CustomizedExceptionAdapter → 401 con ErrorResponse y cabecera WWW-Authenticate")
    void shouldReturn401WithErrorResponseWhenNoToken() throws Exception {
        mockMvc.perform(get("/v1/spots"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, containsString("Bearer")))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Token de autenticación ausente, inválido o caducado."))
                .andExpect(jsonPath("$.path").value("/v1/spots"));
    }

    @Test
    @DisplayName("SEC-11: Con JWT sintácticamente inválido, el resource server enruta a CustomizedExceptionAdapter → 401 con ErrorResponse y cabecera WWW-Authenticate")
    void shouldReturn401WithErrorResponseWhenMalformedJwt() throws Exception {
        when(jwtDecoder.decode(anyString())).thenThrow(new BadJwtException("Invalid or expired JWT"));

        mockMvc.perform(get("/v1/spots")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, containsString("Bearer")))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Token de autenticación ausente, inválido o caducado."))
                .andExpect(jsonPath("$.path").value("/v1/spots"));
    }

    @Test
    @DisplayName("SEC-11: Con rol insuficiente, @PreAuthorize enruta a CustomizedExceptionAdapter → 403 con ErrorResponse")
    void shouldReturn403WithErrorResponseWhenInsufficientRole() throws Exception {
        mockMvc.perform(get("/v1/spots")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("No tiene permisos para realizar esta acción."))
                .andExpect(jsonPath("$.path").value("/v1/spots"));
    }
}
