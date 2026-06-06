package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.EntidadBeneficaDTO;
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
@RequestMapping("/entidades")
@Tag(name = "Entidades Benéficas", description = "Gestión de entidades benéficas")
public class EntidadesController {

  private final Fachada fachada;

  public EntidadesController(Fachada fachada) {
    this.fachada = fachada;
  }

  @Operation(summary = "Registrar entidad benéfica", description = "Crea una nueva entidad benéfica en el sistema")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Entidad creada exitosamente",
          content = @Content(schema = @Schema(implementation = EntidadBeneficaDTO.class))),
      @ApiResponse(responseCode = "400", description = "Body inválido o campos obligatorios faltantes"),
      @ApiResponse(responseCode = "409", description = "Ya existe una entidad con ese ID")
  })
  @PostMapping()
  public ResponseEntity<EntidadBeneficaDTO> postEntidades(@RequestBody EntidadBeneficaDTO entidadDTO) {
    EntidadBeneficaDTO entidadAgregada = fachada.agregarEntidad(entidadDTO);
    return ResponseEntity.ok(entidadAgregada);
  }

  @Operation(summary = "Listar entidades benéficas", description = "Obtiene todas las entidades benéficas registradas")
  @ApiResponse(responseCode = "200", description = "Lista de entidades benéficas")
  @GetMapping()
  public ResponseEntity<List<EntidadBeneficaDTO>> getEntidades() {
    return ResponseEntity.ok(this.fachada.listarEntidades());
  }

  @Operation(summary = "Obtener entidad por ID", description = "Busca una entidad benéfica por su identificador")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Entidad encontrada",
          content = @Content(schema = @Schema(implementation = EntidadBeneficaDTO.class))),
      @ApiResponse(responseCode = "404", description = "Entidad no encontrada")
  })
  @GetMapping("/{id}")
  public ResponseEntity<EntidadBeneficaDTO> getEntidadByID(
      @Parameter(description = "ID de la entidad benéfica") @PathVariable String id) {
    return ResponseEntity.ok(this.fachada.buscarEntidadPorID(id));
  }
}
