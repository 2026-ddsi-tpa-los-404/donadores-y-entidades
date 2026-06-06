package ar.edu.utn.dds.k3003.repositories.quejas;

import ar.edu.utn.dds.k3003.model.Queja;
import ar.edu.utn.dds.k3003.repositories.Repository;

import java.util.List;
import java.util.Optional;

public interface QuejaRepository extends Repository<Queja> {
    List<Queja> findAllByDonadorId(String donadorId);
}
