package ar.edu.utn.dds.k3003.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data()
@Builder()
public class Queja {
    String id;
    String donacionId;
    String donadorId;
    LocalDate fecha;
    String descripcion;
}
