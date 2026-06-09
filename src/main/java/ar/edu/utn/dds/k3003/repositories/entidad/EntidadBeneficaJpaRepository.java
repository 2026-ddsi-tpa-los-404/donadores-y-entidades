package ar.edu.utn.dds.k3003.repositories.entidad;

import ar.edu.utn.dds.k3003.model.EntidadBenefica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EntidadBeneficaJpaRepository extends JpaRepository<EntidadBenefica, Long> {
}
