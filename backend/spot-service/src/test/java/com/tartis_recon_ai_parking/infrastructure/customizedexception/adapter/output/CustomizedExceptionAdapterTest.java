package com.tartis_recon_ai_parking.infrastructure.customizedexception.adapter.output;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import com.tartis_recon_ai_parking.domain.spot.exception.InvalidSpotException;
import com.tartis_recon_ai_parking.domain.spot.exception.NoAvailableSpotException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotAlreadyOccupiedException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotCannotBeBlockedException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotNotAvailableException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotNotBlockedException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotNotFoundException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotNotOccupiedException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotValidationException;
import com.tartis_recon_ai_parking.infrastructure.customizedexception.adapter.output.dto.ErrorResponse;

class CustomizedExceptionAdapterTest {

    private CustomizedExceptionAdapter exceptionAdapter;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionAdapter = new CustomizedExceptionAdapter();
        request = new MockHttpServletRequest();
        request.setRequestURI("/v1/spots/test");
    }

    @Test
    @DisplayName("Debe retornar NOT FOUND (404) cuando se lanza SpotNotFoundException")
    void handleNotFound_ShouldReturnNotFoundStatus() {
        SpotNotFoundException exception = new SpotNotFoundException("Plaza no encontrada");

        ResponseEntity<ErrorResponse> response = exceptionAdapter.handleNotFound(exception, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().status());
        assertEquals("Not Found", response.getBody().error());
        assertEquals("Plaza no encontrada", response.getBody().message());
        assertEquals("/v1/spots/test", response.getBody().path());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    @DisplayName("Debe retornar CONFLICT (409) cuando se lanza NoAvailableSpotException")
    void handleNoAvailableSpot_ShouldReturnConflictStatus() {
        NoAvailableSpotException exception = new NoAvailableSpotException("Sin plazas disponibles");

        ResponseEntity<ErrorResponse> response = exceptionAdapter.handleNoAvailableSpot(exception, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().status());
        assertEquals("Conflict", response.getBody().error());
        assertEquals("Sin plazas disponibles", response.getBody().message());
        assertEquals("/v1/spots/test", response.getBody().path());
    }

    @Test
    @DisplayName("Debe retornar BAD REQUEST (400) cuando se lanza InvalidSpotException")
    void handleBadRequest_ShouldReturnBadRequestStatus() {
        InvalidSpotException exception = new InvalidSpotException("Plaza inválida");

        ResponseEntity<ErrorResponse> response = exceptionAdapter.handleBadRequest(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("Bad Request", response.getBody().error());
        assertEquals("Plaza inválida", response.getBody().message());
        assertEquals("/v1/spots/test", response.getBody().path());
    }

    @Test
    @DisplayName("Debe retornar BAD REQUEST (400) para validación, estados no permitidos y fallos de formato de Spring MVC")
    void handleBadRequest_OtherExceptions_ShouldReturnBadRequestStatus() {
        SpotValidationException valEx = new SpotValidationException("Campo nulo");
        SpotNotOccupiedException notOccEx = new SpotNotOccupiedException("Plaza no ocupada");
        SpotNotBlockedException notBlockEx = new SpotNotBlockedException("Plaza no bloqueada");
        HttpMessageNotReadableException parseEx =
                new HttpMessageNotReadableException("Invalid JSON", (HttpInputMessage) null);

        assertEquals(HttpStatus.BAD_REQUEST, exceptionAdapter.handleBadRequest(valEx, request).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, exceptionAdapter.handleBadRequest(notOccEx, request).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, exceptionAdapter.handleBadRequest(notBlockEx, request).getStatusCode());

        ResponseEntity<ErrorResponse> responseParse = exceptionAdapter.handleBadRequest(parseEx, request);
        assertEquals(HttpStatus.BAD_REQUEST, responseParse.getStatusCode());
        assertEquals("El cuerpo de la solicitud no es válido o contiene un formato incorrecto.", responseParse.getBody().message());
    }

    @Test
    @DisplayName("Debe retornar CONFLICT (409) para conflictos de estado de la plaza")
    void handleConflict_ShouldReturnConflictStatus() {
        SpotAlreadyOccupiedException occEx = new SpotAlreadyOccupiedException("Plaza ya ocupada");
        SpotCannotBeBlockedException blockEx = new SpotCannotBeBlockedException("Plaza ocupada no bloqueable");
        SpotNotAvailableException notAvailableEx = new SpotNotAvailableException("Plaza no disponible");

        assertEquals(HttpStatus.CONFLICT, exceptionAdapter.handleConflict(occEx, request).getStatusCode());
        assertEquals(HttpStatus.CONFLICT, exceptionAdapter.handleConflict(blockEx, request).getStatusCode());
        assertEquals(HttpStatus.CONFLICT, exceptionAdapter.handleConflict(notAvailableEx, request).getStatusCode());
    }

    @Test
    @DisplayName("Debe retornar INTERNAL SERVER ERROR (500) para excepciones no controladas")
    void handleGenericException_ShouldReturnInternalServerError() {
        RuntimeException unhandledEx = new RuntimeException("Error inesperado en BD");

        ResponseEntity<ErrorResponse> response = exceptionAdapter.handleGenericException(unhandledEx, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().status());
        assertEquals("Internal Server Error", response.getBody().error());
        assertEquals("Ha ocurrido un error interno en el servidor", response.getBody().message());
        assertEquals("/v1/spots/test", response.getBody().path());
    }
}
