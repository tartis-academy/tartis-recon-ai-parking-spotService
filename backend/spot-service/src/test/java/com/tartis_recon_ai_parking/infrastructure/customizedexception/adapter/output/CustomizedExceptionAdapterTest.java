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
import com.tartis_recon_ai_parking.domain.spot.exception.SpotNotFoundException;

class CustomizedExceptionAdapterTest {

    private CustomizedExceptionAdapter exceptionAdapter;

    @BeforeEach
    void setUp() {
        exceptionAdapter = new CustomizedExceptionAdapter();
    }

    @Test
    @DisplayName("Debe retornar NOT FOUND (404) cuando se lanza SpotNotFoundException")
    void handleNotFound_ShouldReturnNotFoundStatus() {
        // QUE HACE:
        // - Instancia una excepcion SpotNotFoundException con un mensaje.
        // - Invoca al manejador de la excepcion en el adaptador.
        SpotNotFoundException exception = new SpotNotFoundException("Plaza no encontrada");
        
        ResponseEntity<Map<String, Object>> response = exceptionAdapter.handleNotFound(exception);
        
        // QUE DEBERIA HACER:
        // Debe retornar una respuesta HTTP 404 Not Found con el cuerpo configurado 
        // con los detalles del error correspondientes.
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().get("status"));
        assertEquals("Not Found", response.getBody().get("error"));
        assertEquals("Plaza no encontrada", response.getBody().get("message"));
        assertTrue(response.getBody().containsKey("timestamp"));
    }

    @Test
    @DisplayName("Debe retornar BAD REQUEST (400) cuando se lanza InvalidSpotException")
    void handleInvalid_ShouldReturnBadRequestStatus() {
        // QUE HACE:
        // - Instancia una excepcion InvalidSpotException con un mensaje.
        // - Invoca al manejador de la excepcion en el adaptador.
        InvalidSpotException exception = new InvalidSpotException("Plaza inválida");
        
        ResponseEntity<Map<String, Object>> response = exceptionAdapter.handleInvalid(exception);
        
        // QUE DEBERIA HACER:
        // Debe retornar una respuesta HTTP 400 Bad Request con el cuerpo configurado 
        // con los detalles del error correspondientes.
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().get("status"));
        assertEquals("Bad Request", response.getBody().get("error"));
        assertEquals("Plaza inválida", response.getBody().get("message"));
        assertTrue(response.getBody().containsKey("timestamp"));
    }
}
