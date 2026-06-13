package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.config.MetricsService;
import ar.edu.utn.dds.k3003.exceptions.DonadorNoEncontradoException;
import ar.edu.utn.dds.k3003.exceptions.DonadorYaExistenteException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;
import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final MetricsService metricsService;

    public GlobalExceptionHandler(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @ExceptionHandler({DonadorNoEncontradoException.class, NoSuchElementException.class})
    public ResponseEntity<Map<String, String>> handleNotFound(RuntimeException ex) {
        metricsService.incrementarError("404");
        return ResponseEntity.status(404)
            .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(DonadorYaExistenteException.class)
    public ResponseEntity<Map<String, String>> handleConflict(DonadorYaExistenteException ex) {
        metricsService.incrementarError("409");
        return ResponseEntity.status(409)
            .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(RuntimeException ex) {
        metricsService.incrementarError("400");
        return ResponseEntity.status(400)
            .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        metricsService.incrementarError("400");
        return ResponseEntity.status(400)
            .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneral(Exception ex) {
        metricsService.incrementarError("500");
        return ResponseEntity.status(500)
            .body(Map.of("message", "Error interno del servidor"));
    }
}
