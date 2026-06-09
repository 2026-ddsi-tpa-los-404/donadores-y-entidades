package ar.edu.utn.dds.k3003.repositories.quejas;

import ar.edu.utn.dds.k3003.model.Queja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuejaJpaRepository extends JpaRepository<Queja, Long> {
    List<Queja> findAllByDonadorId(String donadorId);
}
