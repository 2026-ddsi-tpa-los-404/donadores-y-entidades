package ar.edu.utn.dds.k3003.repositories.quejas;

import ar.edu.utn.dds.k3003.model.Queja;
import lombok.val;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryQuejasRepo implements QuejaRepository{

  private List<Queja> quejas;
  private AtomicLong idSecuencial = new AtomicLong(1);

  public InMemoryQuejasRepo() {
    this.quejas = new ArrayList<>();
  }

  @Override
  public List<Queja> findAll() {
    return new ArrayList<>(this.quejas);
  }

  @Override
  public Optional<Queja> findById(String id) {
    return this.quejas.stream().filter(d -> d.getId().equals(id)).findFirst();
  }

  @Override
  public Queja save(Queja entity) {
    Queja queja = entity;
    queja.setId(String.valueOf(idSecuencial.getAndIncrement()));
    queja.setFecha(LocalDate.now());

    this.quejas.add(queja);
    return this.findById(queja.getId()).get();
  }

  @Override
  public Queja deleteById(String id) {
    val donador = this.findById(id);
    this.quejas.remove(donador.get());
    return donador.get();
  }

  @Override
  public List<Queja> findAllByDonadorId(String donadorId) {
    return this.quejas.stream().filter(q -> q.getDonadorId().equals(donadorId)).toList();
  }

  @Override
  public Queja update(Queja queja) {
    return null;
  }
}
