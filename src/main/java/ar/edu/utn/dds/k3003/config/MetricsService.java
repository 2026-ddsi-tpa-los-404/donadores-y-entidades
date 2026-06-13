package ar.edu.utn.dds.k3003.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class MetricsService {

  private final MeterRegistry registry;

  public MetricsService(MeterRegistry registry) {
    this.registry = registry;
  }

  // --- Counters de operaciones exitosas ---

  public void incrementarDonadorAgregado() {
    Counter.builder("dds.donadores.agregados")
        .description("Cantidad de donadores agregados")
        .tag("componente", "donadores-y-entidades")
        .register(registry)
        .increment();
  }

  public void incrementarEntidadAgregada() {
    Counter.builder("dds.entidades.agregadas")
        .description("Cantidad de entidades benéficas agregadas")
        .tag("componente", "donadores-y-entidades")
        .register(registry)
        .increment();
  }

  public void incrementarNecesidadRegistrada() {
    Counter.builder("dds.necesidades.registradas")
        .description("Cantidad de necesidades materiales registradas")
        .tag("componente", "donadores-y-entidades")
        .register(registry)
        .increment();
  }

  public void incrementarNecesidadSatisfecha() {
    Counter.builder("dds.necesidades.satisfechas")
        .description("Cantidad de necesidades satisfechas")
        .tag("componente", "donadores-y-entidades")
        .register(registry)
        .increment();
  }

  public void incrementarQuejaRegistrada() {
    Counter.builder("dds.quejas.registradas")
        .description("Cantidad de quejas registradas")
        .tag("componente", "donadores-y-entidades")
        .register(registry)
        .increment();
  }

  public void incrementarConsultaDB() {
    Counter.builder("dds.consultas.db")
        .description("Cantidad de consultas a la base de datos")
        .tag("componente", "donadores-y-entidades")
        .register(registry)
        .increment();
  }

  // --- Counters de errores ---

  public void incrementarError(String tipo) {
    Counter.builder("dds.errores")
        .description("Cantidad de errores")
        .tag("componente", "donadores-y-entidades")
        .tag("tipo", tipo)
        .register(registry)
        .increment();
  }

  // --- Counters de solicitudes HTTP ---

  public void incrementarSolicitud(String endpoint, String status) {
    Counter.builder("dds.solicitudes")
        .description("Cantidad de solicitudes HTTP")
        .tag("componente", "donadores-y-entidades")
        .tag("endpoint", endpoint)
        .tag("status", status)
        .register(registry)
        .increment();
  }
}
