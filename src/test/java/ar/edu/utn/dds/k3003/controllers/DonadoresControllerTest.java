package ar.edu.utn.dds.k3003.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.DonadorDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.EstadoDonadorEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.QuejaDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.CategoriaDonadorEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.InsigniaDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.MisionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.TipoMisionEnum;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaIncentivos;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class DonadoresControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private Fachada fachada;

  private FachadaIncentivos fachadaIncentivos;

  @BeforeEach
  void setUp() {
    fachadaIncentivos = Mockito.mock(FachadaIncentivos.class);
    fachada.setFachadaIncentivos(fachadaIncentivos);
  }

  private DonadorDTO crearDonadorDTO() {
    return new DonadorDTO(
        null,
        "Juan",
        "Perez",
        30,
        "juan@email.com",
        "12345678",
        "Calle Falsa 123",
        EstadoDonadorEnum.VERIFICADO,
        "BRONCE");
  }

  private String crearDonadorYObtenerID() throws Exception {
    DonadorDTO donador = crearDonadorDTO();
    MvcResult result =
        mockMvc
            .perform(
                post("/donadores")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(donador)))
            .andExpect(status().isOk())
            .andReturn();

    JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
    return json.get("id").asText();
  }

  @Test
  void postDonador_creacionExitosa() throws Exception {
    DonadorDTO donador = crearDonadorDTO();

    mockMvc
        .perform(
            post("/donadores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(donador)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.nombre").value("Juan"))
        .andExpect(jsonPath("$.apellido").value("Perez"))
        .andExpect(jsonPath("$.edad").value(30))
        .andExpect(jsonPath("$.email").value("juan@email.com"))
        .andExpect(jsonPath("$.nroDocumento").value("12345678"))
        .andExpect(jsonPath("$.domicilio").value("Calle Falsa 123"));
  }

  @Test
  void postDonador_duplicado_retorna409() throws Exception {
    String id = crearDonadorYObtenerID();

    DonadorDTO duplicado =
        new DonadorDTO(
            id,
            "Maria",
            "Lopez",
            25,
            "maria@email.com",
            "87654321",
            "Av. Siempreviva 742",
            EstadoDonadorEnum.VERIFICADO,
            "PLATA");

    mockMvc
        .perform(
            post("/donadores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicado)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").exists());
  }

  @Test
  void getDonadores_listarTodos() throws Exception {
    DonadorDTO donador1 = crearDonadorDTO();
    DonadorDTO donador2 =
        new DonadorDTO(
            null,
            "Maria",
            "Lopez",
            25,
            "maria@email.com",
            "87654321",
            "Av. Siempreviva 742",
            EstadoDonadorEnum.VERIFICADO,
            "PLATA");

    mockMvc
        .perform(
            post("/donadores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(donador1)))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            post("/donadores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(donador2)))
        .andExpect(status().isOk());

    mockMvc
        .perform(get("/donadores"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].nombre").value("Juan"))
        .andExpect(jsonPath("$[1].nombre").value("Maria"));
  }

  @Test
  void getDonadorById_encontrado() throws Exception {
    String id = crearDonadorYObtenerID();

    mockMvc
        .perform(get("/donadores/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id))
        .andExpect(jsonPath("$.nombre").value("Juan"));
  }

  @Test
  void getDonadorById_noEncontrado_retorna404() throws Exception {
    mockMvc
        .perform(get("/donadores/inexistente"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").exists());
  }

  @Test
  void patchEstado_valido() throws Exception {
    String id = crearDonadorYObtenerID();

    String estadoBody = "{\"estado\": \"SOSPECHOSO\"}";

    mockMvc
        .perform(
            patch("/donadores/" + id + "/estado")
                .contentType(MediaType.APPLICATION_JSON)
                .content(estadoBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id))
        .andExpect(jsonPath("$.estado").value("SOSPECHOSO"))
        .andExpect(jsonPath("$.nombre").value("Juan"));
  }

  @Test
  void patchEstado_invalido_retorna400() throws Exception {
    String id = crearDonadorYObtenerID();

    String estadoBody = "{\"estado\": \"INVALIDO\"}";

    mockMvc
        .perform(
            patch("/donadores/" + id + "/estado")
                .contentType(MediaType.APPLICATION_JSON)
                .content(estadoBody))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").exists());
  }

  @Test
  void patchCategoria_valida() throws Exception {
    String id = crearDonadorYObtenerID();

    String categoriaBody = "{\"categoria\": \"ORO\"}";

    mockMvc
        .perform(
            patch("/donadores/" + id + "/categoria")
                .contentType(MediaType.APPLICATION_JSON)
                .content(categoriaBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id))
        .andExpect(jsonPath("$.categoria").value("ORO"));
  }

  @Test
  void patchCategoria_nula_retorna400() throws Exception {
    String id = crearDonadorYObtenerID();

    String categoriaBody = "{\"categoria\": \"\"}";

    mockMvc
        .perform(
            patch("/donadores/" + id + "/categoria")
                .contentType(MediaType.APPLICATION_JSON)
                .content(categoriaBody))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").exists());
  }

  @Test
  void getPuedeDonar() throws Exception {
    String id = crearDonadorYObtenerID();

    mockMvc
        .perform(get("/donadores/" + id + "/puede-donar"))
        .andExpect(status().isOk())
        .andExpect(content().string("true"));
  }

  @Test
  void getEstadisticas() throws Exception {
    String id = crearDonadorYObtenerID();

    MisionDTO mision =
        new MisionDTO(
            "mision-1",
            "Mision Test",
            "insignia-1",
            CategoriaDonadorEnum.OCASIONAL,
            CategoriaDonadorEnum.COLABORADOR,
            TipoMisionEnum.COMPLETITUD);
    InsigniaDTO insignia = new InsigniaDTO("insignia-1", "Insignia Test", "Descripcion");

    Mockito.when(fachadaIncentivos.getMisionEnCursoDeDonador(id)).thenReturn(mision);
    Mockito.when(fachadaIncentivos.getInsigniasDeDonador(id)).thenReturn(List.of(insignia));

    mockMvc
        .perform(get("/donadores/" + id + "/estadisticas"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id))
        .andExpect(jsonPath("$.nombre").value("Juan"))
        .andExpect(jsonPath("$.misionActualID").value("mision-1"))
        .andExpect(jsonPath("$.insigniasID[0]").value("insignia-1"));
  }

  @Test
  void postQuejas_valida() throws Exception {
    String id = crearDonadorYObtenerID();

    QuejaDTO queja = new QuejaDTO(null, "donacion-1", id, LocalDate.now(), "Producto en mal estado");

    mockMvc
        .perform(
            post("/donadores/" + id + "/quejas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(queja)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.donadorID").value(id))
        .andExpect(jsonPath("$.donacionID").value("donacion-1"))
        .andExpect(jsonPath("$.descripcion").value("Producto en mal estado"));
  }

  @Test
  void getQuejas() throws Exception {
    String id = crearDonadorYObtenerID();

    QuejaDTO queja1 = new QuejaDTO(null, "donacion-1", id, LocalDate.now(), "Queja 1");
    QuejaDTO queja2 = new QuejaDTO(null, "donacion-2", id, LocalDate.now(), "Queja 2");

    mockMvc
        .perform(
            post("/donadores/" + id + "/quejas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(queja1)))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            post("/donadores/" + id + "/quejas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(queja2)))
        .andExpect(status().isOk());

    mockMvc
        .perform(get("/donadores/" + id + "/quejas"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].donadorID").value(id))
        .andExpect(jsonPath("$[1].donadorID").value(id));
  }
}
