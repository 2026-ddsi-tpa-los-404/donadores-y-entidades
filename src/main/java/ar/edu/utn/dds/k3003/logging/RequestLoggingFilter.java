package ar.edu.utn.dds.k3003.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Agrega traceId, instanceId y requestId al MDC de cada request, para poder
 * correlacionar en el log centralizado una operacion que cruza varios
 * modulos (ej. registrar una necesidad, que termina llamando a Donaciones y
 * a Logistica).
 *
 * <p>traceId identifica una cadena de llamadas completa: si el request trae
 * el header {@link #TRACE_ID_HEADER} lo reusa (porque vino de otro modulo
 * que ya lo genero), si no lo genera porque este es el punto de entrada.
 * requestId identifica un unico hop; cada modulo genera el suyo aunque
 * comparta traceId con los demas.
 */
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

  public static final String TRACE_ID_HEADER = "X-Trace-Id";

  private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

  private final InstanceInfo instanceInfo;

  public RequestLoggingFilter(InstanceInfo instanceInfo) {
    this.instanceInfo = instanceInfo;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    // Render pinguea /actuator constantemente para el health check; no aporta
    // nada al log centralizado y gasta cuota/retencion con ruido.
    return request.getRequestURI().startsWith("/actuator");
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String traceId = request.getHeader(TRACE_ID_HEADER);
    if (traceId == null || traceId.isBlank()) {
      traceId = UUID.randomUUID().toString().substring(0, 8);
    }
    String requestId = UUID.randomUUID().toString().substring(0, 8);
    MDC.put("traceId", traceId);
    MDC.put("instanceId", instanceInfo.getInstanceId());
    MDC.put("requestId", requestId);
    long start = System.currentTimeMillis();
    log.info("--> {} {}", request.getMethod(), request.getRequestURI());
    try {
      chain.doFilter(request, response);
    } finally {
      long took = System.currentTimeMillis() - start;
      log.info("<-- {} {} status={} took={}ms", request.getMethod(), request.getRequestURI(), response.getStatus(), took);
      MDC.clear();
    }
  }
}
