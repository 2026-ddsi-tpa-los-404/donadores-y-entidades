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

  @Operation(summary = "Buscar necesidad por ID", description = "Obtiene una necesidad material por su identificador")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Necesidad encontrada",
          content = @Content(schema = @Schema(implementation = NecesidadMaterialDTO.class))),
      @ApiResponse(responseCode = "404", description = "Necesidad no encontrada")
  })
  @GetMapping("/v2/{id}")
  public ResponseEntity<NecesidadMaterialDTO> getNecesidadByID(
      @Parameter(description = "ID de la necesidad material") @PathVariable String id) {
    return ResponseEntity.ok(fachada.buscarNecesidadPorID(id));
  }

  @Operation(summary = "Borrar necesidad por ID", description = "Elimina una necesidad material del sistema")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Necesidad eliminada exitosamente"),
      @ApiResponse(responseCode = "404", description = "Necesidad no encontrada")
  })
  @DeleteMapping("/v2/{id}")
  public ResponseEntity<Void> deleteNecesidad(
      @Parameter(description = "ID de la necesidad material") @PathVariable String id) {
    fachada.borrarNecesidad(id);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Modificar necesidad por ID", description = "Actualiza los datos de una necesidad material existente")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Necesidad actualizada",
          content = @Content(schema = @Schema(implementation = NecesidadMaterialDTO.class))),
      @ApiResponse(responseCode = "404", description = "Necesidad no encontrada")
  })
  @PutMapping("/v2/{id}")
  public ResponseEntity<NecesidadMaterialDTO> putNecesidad(
      @Parameter(description = "ID de la necesidad material") @PathVariable String id,
      @RequestBody NecesidadMaterialDTO necesidadDTO) {
    NecesidadMaterialDTO dtoConId = new NecesidadMaterialDTO(
        id,
        necesidadDTO.entidadID(),
        necesidadDTO.nivelDeUrgencia(),
        necesidadDTO.descripcion(),
        necesidadDTO.cantidadObjetivo(),
        necesidadDTO.productoSolicitadoID(),
        necesidadDTO.tipo());
    fachada.actualizarNecesidad(dtoConId);
    return ResponseEntity.ok(fachada.buscarNecesidadPorID(id));
  // Nuevos GET, PUT y DELETE para Obtener necesidad por ID, Modificar necesidad material y Borrar necesidad material

  @Operation(summary = "Obtener necesidad por ID", description = "Busca una necesidad material por su identificador")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Necesidad encontrada",
                  content = @Content(schema = @Schema(implementation = NecesidadMaterialDTO.class))),
          @ApiResponse(responseCode = "404", description = "Necesidad no encontrada")
  })
  @GetMapping("/{id}")
  public ResponseEntity<NecesidadMaterialDTO> getNecesidadByID(
          @Parameter(description = "ID de la necesidad") @PathVariable String id) {
    return ResponseEntity.ok(this.fachada.buscarNecesidadPorID(id));
  }

  @Operation(summary = "Modificar necesidad material", description = "Actualiza urgencia, descripción o cantidad de una necesidad")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Necesidad actualizada exitosamente",
                  content = @Content(schema = @Schema(implementation = NecesidadMaterialDTO.class))),
          @ApiResponse(responseCode = "404", description = "Necesidad no encontrada")
  })
  @PutMapping("/{id}")
  public ResponseEntity<NecesidadMaterialDTO> putNecesidad(
          @Parameter(description = "ID de la necesidad") @PathVariable String id,
          @RequestBody NecesidadMaterialDTO necesidadDTO) {
    NecesidadMaterialDTO necesidadActualizada = fachada.modificarNecesidad(id, necesidadDTO);
    return ResponseEntity.ok(necesidadActualizada);
  }

  @Operation(summary = "Borrar necesidad material", description = "Elimina una necesidad material del sistema")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Necesidad eliminada exitosamente"),
          @ApiResponse(responseCode = "404", description = "Necesidad no encontrada")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteNecesidad(
          @Parameter(description = "ID de la necesidad") @PathVariable String id) {
    fachada.eliminarNecesidad(id);
    return ResponseEntity.ok().build(); // Retorna 200 OK sin body
  }
}
