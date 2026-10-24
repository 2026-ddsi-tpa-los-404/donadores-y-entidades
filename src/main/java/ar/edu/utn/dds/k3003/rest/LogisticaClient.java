package ar.edu.utn.dds.k3003.rest;

import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.DepositoDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.PaqueteDTO;
import ar.edu.utn.dds.k3003.logging.TraceIdPropagationInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class LogisticaClient {

  private static final Logger log = LoggerFactory.getLogger(LogisticaClient.class);

  private final RestClient restClient;

  public LogisticaClient(@Value("${services.logistica.url}") String baseUrl,
                         TraceIdPropagationInterceptor traceIdInterceptor) {
    this.restClient = RestClient.builder()
        .baseUrl(baseUrl)
        .requestInterceptor(traceIdInterceptor)
        .build();
  }

  public Integer consultarStockDeProducto(String productoID) {
    try {
      List<DepositoDTO> depositos = restClient.get()
          .uri("/depositos")
          .retrieve()
          .body(new ParameterizedTypeReference<List<DepositoDTO>>() {});

      if (depositos == null) {
        return 0;
      }

      log.info("Depositos consultados: {}", depositos);

      return depositos.stream()
          .filter(d -> d.stockActual() != null)
          .flatMap(d -> d.stockActual().stream())
          .filter(p -> productoID.equals(p.producto()))
          .mapToInt(PaqueteDTO::cantidad)
          .sum();
    } catch (Exception e) {
      log.warn("Error al consultar stock del producto {} en módulo Logística: {}", productoID, e.getMessage());
      return 0;
    }
  }

  // Logística decide cuánto asignar: 201 si creó una asignación, 204 si no había stock aplicable.
  public boolean asignarStockANecesidad(NecesidadMaterialDTO necesidad) {
    try {
      HttpStatusCode status = restClient.post()
          .uri("/asignaciones/stock")
          .body(necesidad)
          .retrieve()
          .toBodilessEntity()
          .getStatusCode();
      return status.value() == HttpStatus.CREATED.value();
    } catch (Exception e) {
      log.warn("Error al asignar stock para necesidad {}: {}", necesidad.id(), e.getMessage());
      return false;
    }
  }
}
