package ar.edu.utn.dds.k3003.rest;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.ProductoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

@Component
public class DonacionesClient {

  private static final Logger log = LoggerFactory.getLogger(DonacionesClient.class);

  private final RestClient restClient;

  public DonacionesClient(@Value("${services.donaciones.url}") String baseUrl) {
    this.restClient = RestClient.builder()
        .baseUrl(baseUrl)
        .build();
  }

  public Optional<ProductoDTO> buscarProducto(String productoID) {
    try {
      ProductoDTO producto = restClient.get()
          .uri("/productos/{id}", productoID)
          .retrieve()
          .body(ProductoDTO.class);

      log.info("Producto consultado: {}", producto);

      return Optional.ofNullable(producto);
    } catch (Exception e) {
      log.warn("Error al buscar producto {} en módulo Donaciones: {}", productoID, e.getMessage());
      return Optional.empty();
    }
  }
}
