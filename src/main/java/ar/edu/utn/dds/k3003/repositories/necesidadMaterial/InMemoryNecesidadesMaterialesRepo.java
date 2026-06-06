package ar.edu.utn.dds.k3003.repositories.necesidadMaterial;

import ar.edu.utn.dds.k3003.model.NecesidadMaterial;
import ar.edu.utn.dds.k3003.repositories.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryNecesidadesMaterialesRepo implements NecesidadMaterialRepository {

  private List<NecesidadMaterial> necesidadesMateriales;
  private AtomicLong idSecuencial = new AtomicLong(1);

  public InMemoryNecesidadesMaterialesRepo() {
    this.necesidadesMateriales = new ArrayList<>();
  }

  @Override
  public Optional<NecesidadMaterial> findById(String id) {
    return this.necesidadesMateriales.stream().filter(d -> d.getId().equals(id)).findFirst();
  }

  @Override
  public NecesidadMaterial save(NecesidadMaterial entity) {
    NecesidadMaterial necesidadMaterial = entity;
    necesidadMaterial.setId(String.valueOf(idSecuencial.getAndIncrement()));

    this.necesidadesMateriales.add(necesidadMaterial);
    return this.findById(necesidadMaterial.getId()).get();
  }

  @Override
  public NecesidadMaterial deleteById(String id) {
    Optional<NecesidadMaterial> necesidadMaterial = this.findById(id);
    this.necesidadesMateriales.remove(necesidadMaterial.get());
    return necesidadMaterial.get();
  }

  @Override
  public List<NecesidadMaterial> findAllNecesidadesInsatisfechasByProductId(String productId) {
    return this.necesidadesMateriales.stream().filter(d -> d.getProductoSolicitadoId().equals(productId) && d.getCantidadObjetivo() > 0).toList();
  }

  public List<NecesidadMaterial> findAll() {
    return new ArrayList<>(this.necesidadesMateriales);
  }

  @Override
  public NecesidadMaterial update(NecesidadMaterial entity) {
    Optional<NecesidadMaterial> necesidadMaterialExistente = this.findById(entity.getId());
    if (necesidadMaterialExistente.isEmpty()) {
      throw new RuntimeException("No se encontró la necesidad material con ID: " + entity.getId());
    }

    this.necesidadesMateriales.remove(necesidadMaterialExistente.get());
    this.necesidadesMateriales.add(entity);
    return this.findById(entity.getId()).get();
  }
}
