package ar.edu.utn.dds.k3003.config;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class HttpMetricsFilter implements Filter {

  private static final Logger log = LoggerFactory.getLogger(HttpMetricsFilter.class);
  private final MeterRegistry registry;

  public HttpMetricsFilter(MeterRegistry registry) {
    this.registry = registry;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    long start = System.currentTimeMillis();

    chain.doFilter(request, response);

    long duration = System.currentTimeMillis() - start;

    String method = httpRequest.getMethod();
    String uri = normalizeUri(httpRequest.getRequestURI());
    int status = httpResponse.getStatus();

    // Counter de requests
    registry.counter("dds.http.requests",
        "method", method,
        "uri", uri,
        "status", String.valueOf(status),
        "componente", "donadores-y-entidades"
    ).increment();

    // Timer de duración
    registry.timer("dds.http.duration",
        "method", method,
        "uri", uri,
        "componente", "donadores-y-entidades"
    ).record(java.time.Duration.ofMillis(duration));

    log.info("[METRICA] HTTP {} {} -> {} ({}ms) | dds.http.requests +1", method, uri, status, duration);
  }

  private String normalizeUri(String uri) {
    return uri.replaceAll("/\\d+", "/id");
  }
}
