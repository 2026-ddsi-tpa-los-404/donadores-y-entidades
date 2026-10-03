package ar.edu.utn.dds.k3003.logging;

import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

/**
 * Reenvia el traceId del MDC actual como header en toda llamada saliente por
 * RestClient/RestTemplate (a Donaciones o a Logistica), para que el log
 * centralizado permita seguir una operacion de punta a punta aunque cruce
 * varios modulos.
 */
@Component
public class TraceIdPropagationInterceptor implements ClientHttpRequestInterceptor {

  @Override
  public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
      throws IOException {
    String traceId = MDC.get("traceId");
    if (traceId != null && !traceId.isBlank()) {
      request.getHeaders().add(RequestLoggingFilter.TRACE_ID_HEADER, traceId);
    }
    return execution.execute(request, body);
  }
}
