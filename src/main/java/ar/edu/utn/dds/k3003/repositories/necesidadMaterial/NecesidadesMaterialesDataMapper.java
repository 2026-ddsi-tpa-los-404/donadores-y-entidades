package ar.edu.utn.dds.k3003.repositories.necesidadMaterial;

import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.TipoNecesidadMaterialEnum;
import ar.edu.utn.dds.k3003.model.NecesidadMaterial;
import ar.edu.utn.dds.k3003.model.PeriodoNecesidad;

public class NecesidadesMaterialesDataMapper {

  public NecesidadMaterialDTO toNecesidadMaterialDTO(NecesidadMaterial entidad) {
    return new NecesidadMaterialDTO(
            entidad.getId(),
            entidad.getEntidadId(),
            entidad.getNivelDeUrgencia(),
            entidad.getDescripcion(),
            entidad.getCantidadObjetivo(),
            entidad.getProductoSolicitadoId(),
            entidad.getTipo()
            );
  }

  public NecesidadMaterial toNecesidadMaterial(NecesidadMaterialDTO entidadDTO) {
    NecesidadMaterial.NecesidadMaterialBuilder builder = NecesidadMaterial.builder()
            .id(entidadDTO.id())
            .entidadId(entidadDTO.entidadID())
            .nivelDeUrgencia(entidadDTO.nivelDeUrgencia())
            .descripcion(entidadDTO.descripcion())
            .cantidadObjetivo(entidadDTO.cantidadObjetivo())
            .productoSolicitadoId(entidadDTO.productoSolicitadoID())
            .tipo(entidadDTO.tipo());

    if (entidadDTO.tipo() == TipoNecesidadMaterialEnum.RECURRENTE) {
      builder.periodo(PeriodoNecesidad.SEMANAL);
    }

    return builder.build();
  }
}
