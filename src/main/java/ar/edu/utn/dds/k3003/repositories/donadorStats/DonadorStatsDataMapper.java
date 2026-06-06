package ar.edu.utn.dds.k3003.repositories.donadorStats;

import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.DonadorDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.DonadorStatsDTO;

import java.util.List;

public class DonadorStatsDataMapper {

  public DonadorStatsDTO toDonadorStatsDTO(
      DonadorDTO donador,
      String misionActualID,
      List<String> insigniasIDs) {

    return new DonadorStatsDTO(
        donador.id(),
        donador.nombre(),
        donador.apellido(),
        donador.edad(),
        donador.estado(),
        donador.categoria(),
        misionActualID,
        insigniasIDs
    );
  }
}
