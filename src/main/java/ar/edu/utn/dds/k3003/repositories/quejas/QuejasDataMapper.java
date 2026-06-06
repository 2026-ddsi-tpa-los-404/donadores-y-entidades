package ar.edu.utn.dds.k3003.repositories.quejas;

import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.QuejaDTO;
import ar.edu.utn.dds.k3003.model.Queja;

public class QuejasDataMapper {

  public QuejaDTO toQuejaDTO(Queja queja) {
    return new QuejaDTO(
            queja.getId(),
            queja.getDonacionId(),
            queja.getDonadorId(),
            queja.getFecha(),
            queja.getDescripcion());
  }

  public Queja toQueja(QuejaDTO entidadDTO) {
    return Queja.builder()
            .id(entidadDTO.id())
            .donacionId(entidadDTO.donacionID())
            .donadorId(entidadDTO.donadorID())
            .fecha(entidadDTO.fecha())
            .descripcion(entidadDTO.descripcion())
            .build();
  }
}
