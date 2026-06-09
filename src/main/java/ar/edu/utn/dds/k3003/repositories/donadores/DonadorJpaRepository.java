package ar.edu.utn.dds.k3003.repositories.donadores;

import ar.edu.utn.dds.k3003.model.Donador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DonadorJpaRepository extends JpaRepository<Donador, Long> {
}
