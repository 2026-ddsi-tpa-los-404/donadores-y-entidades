package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.TipoNecesidadMaterialEnum;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data()
@Builder()
public class NecesidadMaterial {
    String id;
    String entidadId;
    Integer nivelDeUrgencia;
    String descripcion;
    Integer cantidadObjetivo;
    String productoSolicitadoId;
    TipoNecesidadMaterialEnum tipo;
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
