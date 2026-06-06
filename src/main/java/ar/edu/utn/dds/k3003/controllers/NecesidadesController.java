package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.controllers.dtos.SatisfaccionRequest;
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
@RequestMapping("/necesidades")
@Tag(name = "Necesidades Materiales", description = "Gestión de necesidades materiales de entidades benéficas")
public class NecesidadesController {

  private final Fachada fachada;

  public NecesidadesController(Fachada fachada) {
    this.fachada = fachada;
  }

  @Operation(summary = "Registrar necesidad material", description = "Crea una nueva necesidad material asociada a una entidad benéfica")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Necesidad registrada exitosamente",
          content = @Content(schema = @Schema(implementation = NecesidadMaterialDTO.class))),
      @ApiResponse(responseCode = "400", description = "Body inválido, campos faltantes o tipo inválido"),
      @ApiResponse(responseCode = "409", description = "Ya existe una necesidad con ese ID")
  })
  @PostMapping
  public ResponseEntity<NecesidadMaterialDTO> postNecesidades(@RequestBody NecesidadMaterialDTO necesidadDTO) {
    NecesidadMaterialDTO necesidadRegistrada = fachada.registrarNecesidad(necesidadDTO);
    return ResponseEntity.ok(necesidadRegistrada);
  }

  @Operation(summary = "Consultar necesidades insatisfechas", description = "Obtiene necesidades materiales con cantidadObjetivo > 0. Opcionalmente filtra por productoSolicitadoID")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Lista de necesidades insatisfechas")
  })
  @GetMapping
  public ResponseEntity<List<NecesidadMaterialDTO>> getNecesidades(
      @Parameter(description = "ID del producto solicitado para filtrar (opcional)")
      @RequestParam(required = false) String productoSolicitadoID) {
    List<NecesidadMaterialDTO> necesidades;
    if (productoSolicitadoID != null) {
      necesidades = fachada.obtenerNecesidadesInsatisfechasDe(productoSolicitadoID);
    } else {
      necesidades = fachada.obtenerNecesidadesInsatisfechas();
    }
    return ResponseEntity.ok(necesidades);
  }

  @Operation(summary = "Satisfacer necesidad material", description = "Registra la satisfacción parcial o total de una necesidad, reduciendo la cantidadObjetivo. Si excede, se establece en 0")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Necesidad actualizada",
          content = @Content(schema = @Schema(implementation = NecesidadMaterialDTO.class))),
      @ApiResponse(responseCode = "400", description = "Cantidad inválida (menor o igual a 0)"),
      @ApiResponse(responseCode = "404", description = "Necesidad no encontrada")
  })
  @PostMapping("/{necesidadId}/satisfaccion")
  public ResponseEntity<NecesidadMaterialDTO> postSatisfaccion(
      @Parameter(description = "ID de la necesidad material") @PathVariable String necesidadId,
      @RequestBody SatisfaccionRequest satisfaccionRequest) {
    NecesidadMaterialDTO necesidadActualizada = fachada.satisfacerNecesidad(necesidadId, satisfaccionRequest.cantidad());
    return ResponseEntity.ok(necesidadActualizada);
  }
}
