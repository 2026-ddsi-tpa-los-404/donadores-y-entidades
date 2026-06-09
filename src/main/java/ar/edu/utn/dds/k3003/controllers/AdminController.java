package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.DonadorDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.EntidadBeneficaDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.QuejaDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@Tag(name = "Admin", description = "Endpoints de administración para consultar estado y limpiar la base de datos")
public class AdminController {

  private final Fachada fachada;

  public AdminController(Fachada fachada) {
    this.fachada = fachada;
  }

  @Operation(summary = "Limpiar toda la base de datos", description = "Elimina todos los registros de todas las tablas")
  @ApiResponse(responseCode = "200", description = "Base de datos limpiada exitosamente")
  @DeleteMapping("/limpiar")
  public ResponseEntity<Map<String, String>> limpiarBaseDeDatos() {
    fachada.limpiarTodo();
    return ResponseEntity.ok(Map.of("message", "Base de datos limpiada exitosamente"));
  }

  @Operation(summary = "Consultar todos los donadores", description = "Retorna todos los donadores persistidos en la base de datos")
  @ApiResponse(responseCode = "200", description = "Lista de donadores")
  @GetMapping("/donadores")
  public ResponseEntity<List<DonadorDTO>> getDonadores() {
    return ResponseEntity.ok(fachada.listarDonadores());
  }

  @Operation(summary = "Consultar todas las entidades", description = "Retorna todas las entidades benéficas persistidas en la base de datos")
  @ApiResponse(responseCode = "200", description = "Lista de entidades")
  @GetMapping("/entidades")
  public ResponseEntity<List<EntidadBeneficaDTO>> getEntidades() {
    return ResponseEntity.ok(fachada.listarEntidades());
  }

  @Operation(summary = "Consultar todas las necesidades", description = "Retorna todas las necesidades materiales persistidas en la base de datos")
  @ApiResponse(responseCode = "200", description = "Lista de necesidades")
  @GetMapping("/necesidades")
  public ResponseEntity<List<NecesidadMaterialDTO>> getNecesidades() {
    return ResponseEntity.ok(fachada.obtenerNecesidadesInsatisfechas());
  }
}
