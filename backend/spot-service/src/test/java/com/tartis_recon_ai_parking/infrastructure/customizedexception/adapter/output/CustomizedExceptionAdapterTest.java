package com.tartis_recon_ai_parking.infrastructure.customizedexception.adapter.output;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.QueryTimeoutException;
import com.tartis_recon_ai_parking.domain.spot.VehicleType;
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
    @DisplayName("Debe retornar BAD REQUEST (400) para validación y fallos de formato de Spring MVC")
    void handleBadRequest_OtherExceptions_ShouldReturnBadRequestStatus() {
        SpotValidationException valEx = new SpotValidationException("Campo nulo");
        HttpMessageNotReadableException parseEx =
                new HttpMessageNotReadableException("Invalid JSON", (HttpInputMessage) null);

        assertEquals(HttpStatus.BAD_REQUEST, exceptionAdapter.handleBadRequest(valEx, request).getStatusCode());

        ResponseEntity<ErrorResponse> responseParse = exceptionAdapter.handleBadRequest(parseEx, request);
        assertEquals(HttpStatus.BAD_REQUEST, responseParse.getStatusCode());
        assertEquals("El cuerpo de la solicitud no es válido o contiene un formato incorrecto.", responseParse.getBody().message());
    }

    @Test
    @DisplayName("Debe retornar BAD REQUEST (400) para MethodArgumentTypeMismatchException y MethodArgumentNotValidException con error de campo")
    void handleBadRequest_TypeMismatchAndFieldValidation_ShouldReturnBadRequestStatus() throws NoSuchMethodException {
        MethodArgumentTypeMismatchException typeMismatchEx =
                new MethodArgumentTypeMismatchException("CARRO", VehicleType.class, "type", null, new IllegalArgumentException());

        ResponseEntity<ErrorResponse> typeMismatchResponse = exceptionAdapter.handleBadRequest(typeMismatchEx, request);
        assertEquals(HttpStatus.BAD_REQUEST, typeMismatchResponse.getStatusCode());
        assertEquals("El parámetro 'type' tiene un formato no válido.", typeMismatchResponse.getBody().message());

        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "spotRequest");
        bindingResult.addError(new FieldError("spotRequest", "type", "El tipo es obligatorio"));
        MethodParameter methodParameter = new MethodParameter(
                getClass().getDeclaredMethod("handleBadRequest_TypeMismatchAndFieldValidation_ShouldReturnBadRequestStatus"), -1);
        MethodArgumentNotValidException notValidEx = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<ErrorResponse> notValidResponse = exceptionAdapter.handleBadRequest(notValidEx, request);
        assertEquals(HttpStatus.BAD_REQUEST, notValidResponse.getStatusCode());
        assertEquals("El tipo es obligatorio", notValidResponse.getBody().message());
    }

    @Test
    @DisplayName("Debe retornar un mensaje generico (no el interno de Spring) cuando MethodArgumentNotValidException no trae field error")
    void handleBadRequest_ValidationWithoutFieldError_ShouldReturnGenericMessage() throws NoSuchMethodException {
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "spotRequest");
        bindingResult.addError(new ObjectError("spotRequest", "Error a nivel de objeto"));
        MethodParameter methodParameter = new MethodParameter(
                getClass().getDeclaredMethod("handleBadRequest_ValidationWithoutFieldError_ShouldReturnGenericMessage"), -1);
        MethodArgumentNotValidException notValidEx = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<ErrorResponse> response = exceptionAdapter.handleBadRequest(notValidEx, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("La solicitud no cumple las validaciones requeridas.", response.getBody().message());
    }

    @Test
    @DisplayName("Debe retornar CONFLICT (409) para conflictos de estado de la plaza")
    void handleConflict_ShouldReturnConflictStatus() {
        SpotAlreadyOccupiedException occEx = new SpotAlreadyOccupiedException("Plaza ya ocupada");
        SpotCannotBeBlockedException blockEx = new SpotCannotBeBlockedException("Plaza ocupada no bloqueable");
        SpotNotAvailableException notAvailableEx = new SpotNotAvailableException("Plaza no disponible");
        SpotNotOccupiedException notOccEx = new SpotNotOccupiedException("Plaza no ocupada");
        SpotNotBlockedException notBlockEx = new SpotNotBlockedException("Plaza no bloqueada");
        NoAvailableSpotException noAvailableEx = new NoAvailableSpotException("Sin plazas disponibles");

        assertEquals(HttpStatus.CONFLICT, exceptionAdapter.handleConflict(occEx, request).getStatusCode());
        assertEquals(HttpStatus.CONFLICT, exceptionAdapter.handleConflict(blockEx, request).getStatusCode());
        assertEquals(HttpStatus.CONFLICT, exceptionAdapter.handleConflict(notAvailableEx, request).getStatusCode());
        assertEquals(HttpStatus.CONFLICT, exceptionAdapter.handleConflict(notOccEx, request).getStatusCode());
        assertEquals(HttpStatus.CONFLICT, exceptionAdapter.handleConflict(notBlockEx, request).getStatusCode());
        assertEquals(HttpStatus.CONFLICT, exceptionAdapter.handleConflict(noAvailableEx, request).getStatusCode());
    }

    @Test
    @DisplayName("Debe retornar CONFLICT (409) para violaciones de integridad de datos en BD")
    void handleDataIntegrityViolation_ShouldReturnConflictStatus() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Unique index violation: spot_number_uk");

        ResponseEntity<ErrorResponse> response = exceptionAdapter.handleDataIntegrityViolation(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().status());
        assertEquals("Conflict", response.getBody().error());
        assertEquals("Violación de integridad de datos en la persistencia.", response.getBody().message());
        assertEquals("/v1/spots/test", response.getBody().path());
    }

    @Test
    @DisplayName("Debe retornar SERVICE UNAVAILABLE (503) para timeouts y fallos de conexión a la BD")
    void handleDatabaseUnavailable_ShouldReturnServiceUnavailableStatus() {
        DataAccessResourceFailureException resourceEx = new DataAccessResourceFailureException("Connection lost");
        QueryTimeoutException timeoutEx = new QueryTimeoutException("Query execution timed out");

        ResponseEntity<ErrorResponse> resResource = exceptionAdapter.handleDatabaseUnavailable(resourceEx, request);
        ResponseEntity<ErrorResponse> resTimeout = exceptionAdapter.handleDatabaseUnavailable(timeoutEx, request);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, resResource.getStatusCode());
        assertEquals(503, resResource.getBody().status());
        assertEquals("Service Unavailable", resResource.getBody().error());
        assertEquals("El servicio de datos no está disponible temporalmente.", resResource.getBody().message());

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, resTimeout.getStatusCode());
        assertEquals(503, resTimeout.getBody().status());
    }

    @Test
    @DisplayName("Debe retornar CONFLICT (409) para deadlocks y fallos de bloqueo/concurrencia en BD")
    void handleConcurrency_ShouldReturnConflictStatus() {
        CannotAcquireLockException lockEx = new CannotAcquireLockException("Deadlock detected");

        ResponseEntity<ErrorResponse> response = exceptionAdapter.handleConcurrency(lockEx, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().status());
        assertEquals("Conflict", response.getBody().error());
        assertEquals("El recurso está siendo modificado por otra transacción. Reintente.", response.getBody().message());
    }

    @Test
    @DisplayName("Debe retornar INTERNAL SERVER ERROR (500) para errores genéricos de acceso a datos sin exponer el stacktrace")
    void handleDataAccessException_ShouldReturnInternalServerError() {
        DataAccessException dataEx = new DataAccessException("Fatal SQL Syntax Error") {};

        ResponseEntity<ErrorResponse> response = exceptionAdapter.handleDataAccessException(dataEx, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().status());
        assertEquals("Internal Server Error", response.getBody().error());
        assertEquals("Ha ocurrido un error en la capa de datos de persistencia.", response.getBody().message());
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
