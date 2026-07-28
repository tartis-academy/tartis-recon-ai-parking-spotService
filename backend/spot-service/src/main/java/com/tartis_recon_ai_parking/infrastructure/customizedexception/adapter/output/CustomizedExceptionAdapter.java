package com.tartis_recon_ai_parking.infrastructure.customizedexception.adapter.output;


import com.tartis_recon_ai_parking.domain.spot.exception.InvalidSpotException;
import com.tartis_recon_ai_parking.domain.spot.exception.NoAvailableSpotException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotAlreadyOccupiedException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotCannotBeBlockedException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotDomainException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotNotAvailableException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotNotBlockedException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotNotFoundException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotNotOccupiedException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotValidationException;
import com.tartis_recon_ai_parking.infrastructure.customizedexception.adapter.output.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class CustomizedExceptionAdapter {

    private static final Logger log = LoggerFactory.getLogger(CustomizedExceptionAdapter.class);

    // ==========================================
    // EXCEPCIONES DE DOMINIO Y NAVEGACIÓN
    // ==========================================

    @ExceptionHandler(SpotNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(SpotNotFoundException ex, HttpServletRequest request) {
        log.warn("Recurso no encontrado en {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler({
            InvalidSpotException.class,
            SpotValidationException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MethodArgumentNotValidException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex, HttpServletRequest request) {
        log.warn("Solicitud incorrecta en {}: {}", request.getRequestURI(), ex.getMessage());
        String message = ex.getMessage();
        if (ex instanceof MethodArgumentNotValidException navEx) {
            var fieldError = navEx.getBindingResult().getFieldError();
            message = fieldError != null
                    ? fieldError.getDefaultMessage()
                    : "La solicitud no cumple las validaciones requeridas.";
        } else if (ex instanceof HttpMessageNotReadableException) {
            message = "El cuerpo de la solicitud no es válido o contiene un formato incorrecto.";
        } else if (ex instanceof MethodArgumentTypeMismatchException typeEx) {
            message = "El parámetro '" + typeEx.getName() + "' tiene un formato no válido.";
        }
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler({
            SpotAlreadyOccupiedException.class,
            SpotCannotBeBlockedException.class,
            SpotNotAvailableException.class,
            SpotNotOccupiedException.class,
            SpotNotBlockedException.class,
            NoAvailableSpotException.class
    })
    public ResponseEntity<ErrorResponse> handleConflict(SpotDomainException ex, HttpServletRequest request) {
        log.warn("Conflicto de negocio en {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex,
            HttpServletRequest request) {
        log.error("Violación de integridad de datos en {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return build(HttpStatus.CONFLICT, "Violación de integridad de datos en la persistencia.", request);
    }

    @ExceptionHandler({
            DataAccessResourceFailureException.class,
            QueryTimeoutException.class
    })
    public ResponseEntity<ErrorResponse> handleDatabaseUnavailable(Exception ex, HttpServletRequest request) {
        log.error("Fallo de conexión o timeout de base de datos en {}: {}", request.getRequestURI(), ex.getMessage(),
                ex);
        return build(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de datos no está disponible temporalmente.", request);
    }

    @ExceptionHandler({
            CannotAcquireLockException.class,
            PessimisticLockingFailureException.class,
            ObjectOptimisticLockingFailureException.class,
            ConcurrencyFailureException.class
    })
    public ResponseEntity<ErrorResponse> handleConcurrency(Exception ex, HttpServletRequest request) {
        log.error("Conflicto de bloqueo / deadlock en {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return build(HttpStatus.CONFLICT, "El recurso está siendo modificado por otra transacción. Reintente.",
                request);
    }

    @ExceptionHandler({
            DataAccessException.class,
            SQLException.class
    })
    public ResponseEntity<ErrorResponse> handleDataAccessException(Exception ex, HttpServletRequest request) {
        log.error("Fallo catastrófico de base de datos en {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Ha ocurrido un error en la capa de datos de persistencia.",
                request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Excepción no controlada en servidor en {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Ha ocurrido un error interno en el servidor", request);
    }


     private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                java.time.Instant.now().toString(), // 1. String (Timestamp)
                status.value(),                     // 2. int (Estado, ej: 404)
                status.getReasonPhrase(),           // 3. String (Título, ej: "Not Found")
                message,                            // 4. String (Detalle del error)
                request != null ? request.getRequestURI() : "" // 5. String (Ruta)
        );
        return ResponseEntity.status(status).body(body);
    }
}
