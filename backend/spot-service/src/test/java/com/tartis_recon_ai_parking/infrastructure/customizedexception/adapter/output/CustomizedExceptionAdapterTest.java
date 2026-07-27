package com.tartis_recon_ai_parking.infrastructure.customizedexception.adapter.output;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.tartis_recon_ai_parking.domain.spot.exception.InvalidSpotException;
import com.tartis_recon_ai_parking.domain.spot.exception.NoAvailableSpotException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotAlreadyOccupiedException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotCannotBeBlockedException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotNotAvailableException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotNotBlockedException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotNotFoundException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotNotOccupiedException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotValidationException;

class CustomizedExceptionAdapterTest {

    private CustomizedExceptionAdapter exceptionAdapter;

    @BeforeEach
    void setUp() {
        exceptionAdapter = new CustomizedExceptionAdapter();
    }

    @Test
    @DisplayName("Debe retornar NOT FOUND (404) cuando se lanza SpotNotFoundException")
    void handleNotFound_ShouldReturnNotFoundStatus() {
        SpotNotFoundException exception = new SpotNotFoundException("Plaza no encontrada");

        ResponseEntity<Map<String, Object>> response = exceptionAdapter.handleNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().get("status"));
        assertEquals("Not Found", response.getBody().get("error"));
        assertEquals("Plaza no encontrada", response.getBody().get("message"));
        assertTrue(response.getBody().containsKey("timestamp"));
    }

    @Test
    @DisplayName("Debe retornar BAD REQUEST (400) cuando se lanza InvalidSpotException")
    void handleBadRequest_ShouldReturnBadRequestStatus() {
        InvalidSpotException exception = new InvalidSpotException("Plaza inválida");

        ResponseEntity<Map<String, Object>> response = exceptionAdapter.handleBadRequest(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().get("status"));
        assertEquals("Bad Request", response.getBody().get("error"));
        assertEquals("Plaza inválida", response.getBody().get("message"));
        assertTrue(response.getBody().containsKey("timestamp"));
    }

    @Test
    @DisplayName("Debe retornar BAD REQUEST (400) para validación y estados no permitidos")
    void handleBadRequest_OtherExceptions_ShouldReturnBadRequestStatus() {
        SpotValidationException valEx = new SpotValidationException("Campo nulo");
        SpotNotOccupiedException notOccEx = new SpotNotOccupiedException("Plaza no ocupada");
        SpotNotBlockedException notBlockEx = new SpotNotBlockedException("Plaza no bloqueada");

        assertEquals(HttpStatus.BAD_REQUEST, exceptionAdapter.handleBadRequest(valEx).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, exceptionAdapter.handleBadRequest(notOccEx).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, exceptionAdapter.handleBadRequest(notBlockEx).getStatusCode());
    }

    @Test
    @DisplayName("Debe retornar CONFLICT (409) para falta de plazas o conflictos de estado")
    void handleConflict_ShouldReturnConflictStatus() {
        NoAvailableSpotException noAvailEx = new NoAvailableSpotException("Sin plazas disponibles");
        SpotAlreadyOccupiedException occEx = new SpotAlreadyOccupiedException("Plaza ya ocupada");
        SpotCannotBeBlockedException blockEx = new SpotCannotBeBlockedException("Plaza ocupada no bloqueable");
        SpotNotAvailableException notAvailableEx = new SpotNotAvailableException("Plaza no disponible");

        ResponseEntity<Map<String, Object>> response = exceptionAdapter.handleConflict(noAvailEx);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().get("status"));
        assertEquals("Conflict", response.getBody().get("error"));
        assertEquals("Sin plazas disponibles", response.getBody().get("message"));

        assertEquals(HttpStatus.CONFLICT, exceptionAdapter.handleConflict(occEx).getStatusCode());
        assertEquals(HttpStatus.CONFLICT, exceptionAdapter.handleConflict(blockEx).getStatusCode());
        assertEquals(HttpStatus.CONFLICT, exceptionAdapter.handleConflict(notAvailableEx).getStatusCode());
    }
}
