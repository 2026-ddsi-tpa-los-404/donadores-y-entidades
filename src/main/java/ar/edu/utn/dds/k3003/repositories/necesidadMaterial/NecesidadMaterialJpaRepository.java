package ar.edu.utn.dds.k3003.repositories.necesidadMaterial;

import ar.edu.utn.dds.k3003.model.NecesidadMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NecesidadMaterialJpaRepository extends JpaRepository<NecesidadMaterial, Long> {
    List<NecesidadMaterial> findAllByProductoSolicitadoIdAndCantidadObjetivoGreaterThan(String productoSolicitadoId, Integer cantidadObjetivo);
}
