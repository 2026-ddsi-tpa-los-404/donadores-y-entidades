package ar.edu.utn.dds.k3003.repositories.entidad;

import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.EntidadBeneficaDTO;
import ar.edu.utn.dds.k3003.model.EntidadBenefica;

public class EntidadesBeneficasDataMapper {

  public EntidadBeneficaDTO toEntidadBeneficaDTO(EntidadBenefica entidad) {
    return new EntidadBeneficaDTO(
            entidad.getId(),
            entidad.getRazonSocial(),
            entidad.getDomicilio(),
            entidad.getTelefono(),
            entidad.getCorreo()
            );
  }

  public EntidadBenefica toEntidadBenefica(EntidadBeneficaDTO entidadDTO) {
    return EntidadBenefica.builder()
            .id(entidadDTO.id())
            .razonSocial(entidadDTO.razonSocial())
            .domicilio(entidadDTO.domicilio())
            .telefono(entidadDTO.telefono())
            .correo(entidadDTO.correo())
            .build();
  }
}
