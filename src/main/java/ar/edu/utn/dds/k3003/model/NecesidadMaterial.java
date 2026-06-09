package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.TipoNecesidadMaterialEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "necesidades_materiales")
public class NecesidadMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String entidadId;
    Integer nivelDeUrgencia;
    String descripcion;
    Integer cantidadObjetivo;
    String productoSolicitadoId;

    @Enumerated(EnumType.STRING)
    TipoNecesidadMaterialEnum tipo;

    @Enumerated(EnumType.STRING)
    PeriodoNecesidad periodo;

    LocalDate fechaUltimaSatisfaccion;

    public boolean estaSatisfechaEnPeriodoActual() {
        if (tipo != TipoNecesidadMaterialEnum.RECURRENTE || fechaUltimaSatisfaccion == null || periodo == null) {
            return false;
        }

        LocalDate hoy = LocalDate.now();
        LocalDate inicioDelPeriodo = periodo.calcularInicioPeriodo(hoy);

        return !fechaUltimaSatisfaccion.isBefore(inicioDelPeriodo);
    }
}
