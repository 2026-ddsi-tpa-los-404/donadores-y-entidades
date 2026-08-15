package ar.edu.utn.dds.k3003.rest;

import ar.edu.utn.dds.k3003.catedra.dtos.logistica.DepositoDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.PaqueteDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
public class LogisticaClient {

  private static final Logger log = LoggerFactory.getLogger(LogisticaClient.class);

  private final RestClient restClient;

  public LogisticaClient(@Value("${services.logistica.url}") String baseUrl) {
    this.restClient = RestClient.builder()
        .baseUrl(baseUrl)
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

  public boolean asignarStockANecesidad(String productoID, String necesidadID, Integer cantidad) {
    try {
      restClient.post()
          .uri("/stock/asignar")
          .body(Map.of(
              "productoID", productoID,
              "necesidadID", necesidadID,
              "cantidad", cantidad,
              "origen", "DONADORES_Y_ENTIDADES"
          ))
          .retrieve()
          .body(Void.class);
      return true;
    } catch (Exception e) {
      log.warn("Error al asignar stock para necesidad {}: {}", necesidadID, e.getMessage());
      return false;
    }
  }
}
