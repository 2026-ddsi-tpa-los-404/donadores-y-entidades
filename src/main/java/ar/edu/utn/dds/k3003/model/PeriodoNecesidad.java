package ar.edu.utn.dds.k3003.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public enum PeriodoNecesidad {
    SEMANAL,
    MENSUAL;

    public LocalDate calcularInicioPeriodo(LocalDate fecha) {
        return switch (this) {
            case SEMANAL -> fecha.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MENSUAL -> fecha.withDayOfMonth(1);
        };
    }
}
