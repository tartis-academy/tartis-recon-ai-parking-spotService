package com.tartis_recon_ai_parking.infrastructure.customizedexception.adapter.output;


import com.tartis_recon_ai_parking.domain.spot.exception.InvalidSpotException;
import com.tartis_recon_ai_parking.domain.spot.exception.NoAvailableSpotException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotAlreadyOccupiedException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotCannotBeBlockedException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotDomainException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotEventOutdatedException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotNotAvailableException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotNotBlockedException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotNotFoundException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotNotOccupiedException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotTypeChangeNotAllowedException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotValidationException;
import com.tartis_recon_ai_parking.domain.spot.exception.UnsupportedSpotStatusTransitionException;
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
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class CustomizedExceptionAdapter {

    private static final Logger log = LoggerFactory.getLogger(CustomizedExceptionAdapter.class);

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
            MissingServletRequestParameterException.class,
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
        } else if (ex instanceof MissingServletRequestParameterException missingEx) {
            message = "Falta el parámetro obligatorio '" + missingEx.getParameterName() + "'.";
        }
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "El recurso solicitado no existe.", request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {
        return build(HttpStatus.METHOD_NOT_ALLOWED,
                "El metodo " + ex.getMethod() + " no esta permitido para este recurso.", request);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request) {
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "El tipo de contenido de la solicitud no esta soportado.", request);
    }

    @ExceptionHandler({
            SpotAlreadyOccupiedException.class,
            SpotCannotBeBlockedException.class,
            SpotNotAvailableException.class,
            SpotNotOccupiedException.class,
            SpotNotBlockedException.class,
            NoAvailableSpotException.class,
            SpotTypeChangeNotAllowedException.class,
            UnsupportedSpotStatusTransitionException.class,
            SpotEventOutdatedException.class
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

    // CannotCreateTransactionException no es una DataAccessException: extiende
    // TransactionException -> NestedRuntimeException. Sin listarla aqui caia en
    // el catch-all de Exception y la BD caida salia como 500 en vez de 503, que
    // es justo el escenario mas comun: los casos de uso son @Transactional, asi
    // que con Postgres inaccesible Spring falla al ABRIR la transaccion, antes
    // de llegar al repositorio y de que exista nada que traducir.
    @ExceptionHandler({
            DataAccessResourceFailureException.class,
            QueryTimeoutException.class,
            CannotCreateTransactionException.class
    })
    public ResponseEntity<ErrorResponse> handleDatabaseUnavailable(Exception ex, HttpServletRequest request) {
        log.error("Fallo de conexión o timeout de base de datos en {}: {}", request.getRequestURI(), ex.getMessage(),
                ex);
        return build(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de datos no está disponible temporalmente.", request);
    }

    @ExceptionHandler({
            CannotAcquireLockException.class,
            PessimisticLockingFailureException.class,
            OptimisticLockingFailureException.class,
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

    /**
     * HTTP 401 Unauthorized: El token de autenticación está ausente, es inválido o ha caducado.
     * <p>
     * Diagnóstico para el equipo: El problema reside en la forma en que el frontend envía el token de autenticación.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(AuthenticationException ex, HttpServletRequest request) {
        log.warn("Autenticación fallida o token inválido en {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.UNAUTHORIZED, "Token de autenticación ausente, inválido o caducado.", request);
    }

    /**
     * HTTP 403 Forbidden: El token de autenticación es válido pero el usuario no posee el rol necesario.
     * <p>
     * Diagnóstico para el equipo: El problema reside en los roles configurados asignados a la identidad.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Acceso denegado en {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.FORBIDDEN, "No tiene permisos para realizar esta acción.", request);
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