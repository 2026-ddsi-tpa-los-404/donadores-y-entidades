package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.DonadorDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.DonadorStatsDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.QuejaDTO;
import ar.edu.utn.dds.k3003.controllers.dtos.CategoriaRequest;
import ar.edu.utn.dds.k3003.controllers.dtos.EstadoRequest;
import ar.edu.utn.dds.k3003.controllers.dtos.QuejaRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/donadores")
@Tag(name = "Donadores", description = "Gestión de donadores del sistema")
public class DonadoresController {

  private final Fachada fachada;

  public DonadoresController(Fachada fachada) {
    this.fachada = fachada;
  }

  @Operation(summary = "Registrar donador", description = "Crea un nuevo donador en el sistema")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Donador creado exitosamente",
          content = @Content(schema = @Schema(implementation = DonadorDTO.class))),
      @ApiResponse(responseCode = "400", description = "Body inválido o campos obligatorios faltantes"),
      @ApiResponse(responseCode = "409", description = "Ya existe un donador con ese ID")
  })
  @PostMapping()
  public ResponseEntity<DonadorDTO> postDonador(@RequestBody DonadorDTO donadorDTO) {
    DonadorDTO donadorAgregado = fachada.agregarDonador(donadorDTO);
    return ResponseEntity.ok(donadorAgregado);
  }

  @Operation(summary = "Listar donadores", description = "Obtiene todos los donadores registrados")
  @ApiResponse(responseCode = "200", description = "Lista de donadores")
  @GetMapping()
  public ResponseEntity<List<DonadorDTO>> getDonadores() {
    return ResponseEntity.ok(this.fachada.listarDonadores());
  }

  @Operation(summary = "Obtener donador por ID", description = "Busca un donador por su identificador")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Donador encontrado",
          content = @Content(schema = @Schema(implementation = DonadorDTO.class))),
      @ApiResponse(responseCode = "404", description = "Donador no encontrado")
  })
  @GetMapping("/{id}")
  public ResponseEntity<DonadorDTO> getDonadorByID(
      @Parameter(description = "ID del donador") @PathVariable String id) {
    return ResponseEntity.ok(this.fachada.buscarDonadorPorID(id));
  }

  @Operation(summary = "Modificar estado de donador", description = "Actualiza el estado de confianza del donador (VERIFICADO, SOSPECHOSO, BANEADO)")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Estado actualizado",
          content = @Content(schema = @Schema(implementation = DonadorDTO.class))),
      @ApiResponse(responseCode = "400", description = "Estado inválido o nulo"),
      @ApiResponse(responseCode = "404", description = "Donador no encontrado")
  })
  @PatchMapping("/{id}/estado")
  public ResponseEntity<DonadorDTO> updateDonadorEstado(
      @Parameter(description = "ID del donador") @PathVariable String id,
      @RequestBody EstadoRequest estadoRequest) {
    return ResponseEntity.ok(this.fachada.modificarEstado(id, estadoRequest.estado()));
  }

  @Operation(summary = "Modificar categoría de donador", description = "Actualiza la categoría de participación del donador")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Categoría actualizada",
          content = @Content(schema = @Schema(implementation = DonadorDTO.class))),
      @ApiResponse(responseCode = "400", description = "Categoría nula o vacía"),
      @ApiResponse(responseCode = "404", description = "Donador no encontrado")
  })
  @PatchMapping("/{id}/categoria")
  public ResponseEntity<DonadorDTO> updateDonadorCategoria(
      @Parameter(description = "ID del donador") @PathVariable String id,
      @RequestBody CategoriaRequest categoriaRequest) {
    if (categoriaRequest == null
        || categoriaRequest.categoria() == null
        || categoriaRequest.categoria().isBlank()) {
      throw new RuntimeException("La categoría no puede ser nula o vacía");
    }
    return ResponseEntity.ok(this.fachada.modifcarCategoria(id, categoriaRequest.categoria()));
  }

  @Operation(summary = "Consultar si puede donar", description = "Verifica si un donador está habilitado para realizar donaciones")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Resultado de la consulta (true/false)"),
      @ApiResponse(responseCode = "404", description = "Donador no encontrado")
  })
  @GetMapping("/{id}/puede-donar")
  public ResponseEntity<Boolean> getPuedeDonar(
      @Parameter(description = "ID del donador") @PathVariable String id) {
    return ResponseEntity.ok(this.fachada.puedeDonar(id));
  }

  @Operation(summary = "Obtener estadísticas de donador", description = "Retorna las estadísticas del donador incluyendo misión actual e insignias")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Estadísticas del donador",
          content = @Content(schema = @Schema(implementation = DonadorStatsDTO.class))),
      @ApiResponse(responseCode = "404", description = "Donador no encontrado")
  })
  @GetMapping("/{id}/estadisticas")
  public ResponseEntity<DonadorStatsDTO> getEstadisticas(
      @Parameter(description = "ID del donador") @PathVariable String id) {
    return ResponseEntity.ok(this.fachada.estadisticasDonador(id));
  }

  @Operation(summary = "Registrar queja contra donador", description = "Registra una queja asociada a un donador. Si acumula 5+ quejas pasa a SOSPECHOSO, 10+ a BANEADO")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Queja registrada",
          content = @Content(schema = @Schema(implementation = QuejaDTO.class))),
      @ApiResponse(responseCode = "400", description = "Body inválido o campos faltantes"),
      @ApiResponse(responseCode = "404", description = "Donador no encontrado")
  })
  @PostMapping("/{id}/quejas")
  public ResponseEntity<QuejaDTO> postQuejas(
      @Parameter(description = "ID del donador") @PathVariable String id,
      @RequestBody QuejaRequest quejaBody) {
    QuejaDTO quejaDTO = new QuejaDTO(
            null,
        quejaBody.donacionID(), id,
        null,
        quejaBody.descripcion()
    );
    return ResponseEntity.ok(this.fachada.agregarQueja(quejaDTO));
  }

  @Operation(summary = "Obtener quejas de un donador", description = "Lista todas las quejas registradas contra un donador")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Lista de quejas"),
      @ApiResponse(responseCode = "404", description = "Donador no encontrado o sin quejas")
  })
  @GetMapping("/{id}/quejas")
  public ResponseEntity<List<QuejaDTO>> getQuejas(
      @Parameter(description = "ID del donador") @PathVariable String id) {
    return ResponseEntity.ok(this.fachada.obtenerQuejasDe(id));
  }
}
