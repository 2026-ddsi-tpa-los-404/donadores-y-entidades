package ar.edu.utn.dds.k3003.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "entidades_beneficas")
public class EntidadBenefica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String razonSocial;
    String domicilio;
    String telefono;
    String correo;
}
