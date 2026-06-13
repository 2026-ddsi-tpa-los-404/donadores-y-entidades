package ar.edu.utn.dds.k3003.rest;

import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.InsigniaDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.MisionDTO;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonaciones;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonadoresYEntidades;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaIncentivos;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.NoSuchElementException;

@Component
public class FachadaIncentivosRemota implements FachadaIncentivos {

  private final RestClient restClient;

  public FachadaIncentivosRemota(
      @Value("${incentivos.url:https://incentivos-nhyc.onrender.com}") String baseUrl) {
    this.restClient = RestClient.builder()
        .baseUrl(baseUrl)
        .build();
  }

  @Override
  public List<InsigniaDTO> getInsigniasDeDonador(String donadorID) throws NoSuchElementException {
    return restClient.get()
        .uri("/donadores/{id}/insignias", donadorID)
        .retrieve()
        .body(new ParameterizedTypeReference<List<InsigniaDTO>>() {});
  }

  @Override
  public MisionDTO getMisionEnCursoDeDonador(String donadorID) throws NoSuchElementException {
    return restClient.get()
        .uri("/donadores/{id}/mision", donadorID)
        .retrieve()
        .body(MisionDTO.class);
  }

  // Métodos no utilizados por este componente

  @Override
  public InsigniaDTO agregarInsignia(InsigniaDTO insignia) {
    throw new UnsupportedOperationException("No implementado en este componente");
  }

  @Override
  public MisionDTO agregarMision(MisionDTO mision) {
    throw new UnsupportedOperationException("No implementado en este componente");
  }

  @Override
  public void asignarMisionADonador(String donadorID, MisionDTO misionDTO) {
    throw new UnsupportedOperationException("No implementado en este componente");
  }

  @Override
  public void asignarInsigniaADonador(String donadorID, InsigniaDTO insigniaDTO) {
    throw new UnsupportedOperationException("No implementado en este componente");
  }

  @Override
  public void procesarDonador(String donadorID) {
    throw new UnsupportedOperationException("No implementado en este componente");
  }

  @Override
  public void setFachadaDonaciones(FachadaDonaciones fachadaDonaciones) {
    // No aplica
  }

  @Override
  public void setFachadaDonadoresYEntidades(FachadaDonadoresYEntidades fachadaDonadoresYEntidades) {
    // No aplica
  }
}
