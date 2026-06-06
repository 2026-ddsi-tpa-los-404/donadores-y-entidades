package ar.edu.utn.dds.k3003.repositories.necesidadMaterial;

import ar.edu.utn.dds.k3003.model.NecesidadMaterial;
import ar.edu.utn.dds.k3003.repositories.Repository;

import java.util.List;

public interface NecesidadMaterialRepository extends Repository<NecesidadMaterial> {
    List<NecesidadMaterial> findAllNecesidadesInsatisfechasByProductId(String productId);
}
