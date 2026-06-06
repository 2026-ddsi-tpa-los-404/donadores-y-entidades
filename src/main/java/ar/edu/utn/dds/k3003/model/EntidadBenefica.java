package ar.edu.utn.dds.k3003.model;

import lombok.Builder;
import lombok.Data;

@Data()
@Builder()
public class EntidadBenefica {
    String id;
    String razonSocial;
    String domicilio;
    String telefono;
    String correo;
}
