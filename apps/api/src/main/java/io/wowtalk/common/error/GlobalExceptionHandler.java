package io.wowtalk.common.error;

import io.wowtalk.common.config.RequestLoggingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(WowTalkException.class)
    public ResponseEntity<ErrorResponse> handleWowTalkException(WowTalkException exception) {
        log.warn(
                "application_exception requestId={} code={} message={}",
                requestId(),
                exception.errorCode().name(),
                exception.getMessage()
        );
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(
                        exception.errorCode().name(),
                        exception.getMessage()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse(ErrorCode.VALIDATION_FAILED.message());

        log.warn(
                "validation_exception requestId={} message={}",
                requestId(),
                message
        );
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(
                        ErrorCode.VALIDATION_FAILED.name(),
                        message
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception) {
        log.error(
                "unhandled_exception requestId={}",
                requestId(),
                exception
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        "INTERNAL_SERVER_ERROR",
                        "서버 내부 오류가 발생했습니다."
                ));
    }

    private String requestId() {
        String requestId = MDC.get(RequestLoggingFilter.REQUEST_ID_MDC_KEY);
        return requestId == null || requestId.isBlank() ? "-" : requestId;
    }
}
