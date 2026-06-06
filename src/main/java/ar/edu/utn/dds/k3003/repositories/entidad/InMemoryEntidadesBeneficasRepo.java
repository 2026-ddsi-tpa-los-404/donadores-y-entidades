package ar.edu.utn.dds.k3003.repositories.entidad;

import ar.edu.utn.dds.k3003.model.EntidadBenefica;
import ar.edu.utn.dds.k3003.model.Queja;
import ar.edu.utn.dds.k3003.repositories.Repository;
import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryEntidadesBeneficasRepo implements Repository<EntidadBenefica> {

  private List<EntidadBenefica> entidadesBeneficas;
  private AtomicLong idSecuencial = new AtomicLong(1);

  public InMemoryEntidadesBeneficasRepo() {
    this.entidadesBeneficas = new ArrayList<>();
  }

  @Override
  public Optional<EntidadBenefica> findById(String id) {
    return this.entidadesBeneficas.stream().filter(d -> d.getId().equals(id)).findFirst();
  }

  @Override
  public EntidadBenefica save(EntidadBenefica entity) {
    EntidadBenefica entidadBenefica = entity;
    entidadBenefica.setId(String.valueOf(idSecuencial.getAndIncrement()));

    this.entidadesBeneficas.add(entidadBenefica);
    return this.findById(entidadBenefica.getId()).get();
  }

  @Override
  public EntidadBenefica deleteById(String id) {
    Optional<EntidadBenefica> entidadBenefica = this.findById(id);
    this.entidadesBeneficas.remove(entidadBenefica.get());
    return entidadBenefica.get();
  }

  @Override
  public EntidadBenefica update(EntidadBenefica entidadBenefica) {
    return null;
  }

  public List<EntidadBenefica> findAll() {
    return new ArrayList<>(this.entidadesBeneficas);
  }
}
