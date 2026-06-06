package ar.edu.utn.dds.k3003.repositories.donadores;

import ar.edu.utn.dds.k3003.model.Donador;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import ar.edu.utn.dds.k3003.repositories.Repository;
import lombok.val;

public class InMemoryDonadoresRepo implements Repository<Donador> {

  private List<Donador> donadores;
  private AtomicLong idSecuencial = new AtomicLong(1);

  public InMemoryDonadoresRepo() {
    this.donadores = new ArrayList<>();
  }

  @Override
  public Optional<Donador> findById(String id) {
    return this.donadores.stream().filter(d -> d.getId().equals(id)).findFirst();
  }

  @Override
  public Donador save(Donador donador) {
    Donador donadorConID = donador;
    donadorConID.setId(String.valueOf(idSecuencial.getAndIncrement()));

    this.donadores.add(donadorConID);
    return this.findById(donadorConID.getId()).get();
  }

  @Override
  public Donador deleteById(String id) {
    val donador = this.findById(id);
    this.donadores.remove(donador.get());
    return donador.get();
  }

  @Override
  public Donador update(Donador donador) {
    val donadorExistente = this.findById(donador.getId());
    if (donadorExistente.isEmpty()) {
      throw new RuntimeException("No se encontró el donador con ID: " + donador.getId());
    }

    this.donadores.remove(donadorExistente.get());
    this.donadores.add(donador);
    return this.findById(donador.getId()).get();
  }

  public List<Donador> findAll() {
    return new ArrayList<>(this.donadores);
  }
}
