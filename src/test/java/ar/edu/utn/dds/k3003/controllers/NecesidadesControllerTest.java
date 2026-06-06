package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.TipoNecesidadMaterialEnum;
import ar.edu.utn.dds.k3003.controllers.dtos.SatisfaccionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class NecesidadesControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void postNecesidades_creacionExitosa_retorna200() throws Exception {
    NecesidadMaterialDTO necesidad = new NecesidadMaterialDTO(
        null, "entidad-1", 3, "Necesitamos arroz", 100, "producto-1", TipoNecesidadMaterialEnum.EXTRAORDINARIA
    );

    mockMvc.perform(post("/necesidades")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(necesidad)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.entidadID").value("entidad-1"))
        .andExpect(jsonPath("$.nivelDeUrgencia").value(3))
        .andExpect(jsonPath("$.descripcion").value("Necesitamos arroz"))
        .andExpect(jsonPath("$.cantidadObjetivo").value(100))
        .andExpect(jsonPath("$.productoSolicitadoID").value("producto-1"))
        .andExpect(jsonPath("$.tipo").value("EXTRAORDINARIA"))
        .andExpect(jsonPath("$.id").isNotEmpty());
  }

  @Test
  void postNecesidades_duplicado_retornaError() throws Exception {
    NecesidadMaterialDTO necesidad = new NecesidadMaterialDTO(
        null, "entidad-1", 3, "Necesitamos arroz", 100, "producto-1", TipoNecesidadMaterialEnum.RECURRENTE
    );

    MvcResult result = mockMvc.perform(post("/necesidades")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(necesidad)))
        .andExpect(status().isOk())
        .andReturn();

    NecesidadMaterialDTO creada = objectMapper.readValue(
        result.getResponse().getContentAsString(), NecesidadMaterialDTO.class);

    NecesidadMaterialDTO duplicada = new NecesidadMaterialDTO(
        creada.id(), "entidad-1", 3, "Necesitamos arroz", 100, "producto-1", TipoNecesidadMaterialEnum.RECURRENTE
    );

    mockMvc.perform(post("/necesidades")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(duplicada)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void postNecesidades_bodyNulo_retorna400() throws Exception {
    mockMvc.perform(post("/necesidades")
            .contentType(MediaType.APPLICATION_JSON)
            .content(""))
        .andExpect(status().isBadRequest());
  }

  @Test
  void postNecesidades_bodyInvalido_retorna400() throws Exception {
    mockMvc.perform(post("/necesidades")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"invalid\": \"json\"}"))
        .andExpect(status().isOk());
  }

  @Test
  void postNecesidades_cantidadObjetivoCero_retorna400() throws Exception {
    NecesidadMaterialDTO necesidad = new NecesidadMaterialDTO(
        null, "entidad-1", 3, "Necesitamos arroz", 0, "producto-1", TipoNecesidadMaterialEnum.EXTRAORDINARIA
    );

    mockMvc.perform(post("/necesidades")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(necesidad)))
        .andExpect(status().isOk());
  }

  @Test
  void getNecesidades_todasInsatisfechas_retorna200() throws Exception {
    NecesidadMaterialDTO necesidad1 = new NecesidadMaterialDTO(
        null, "entidad-1", 3, "Arroz", 100, "producto-1", TipoNecesidadMaterialEnum.EXTRAORDINARIA
    );
    NecesidadMaterialDTO necesidad2 = new NecesidadMaterialDTO(
        null, "entidad-2", 5, "Leche", 50, "producto-2", TipoNecesidadMaterialEnum.RECURRENTE
    );

    mockMvc.perform(post("/necesidades")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(necesidad1)))
        .andExpect(status().isOk());

    mockMvc.perform(post("/necesidades")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(necesidad2)))
        .andExpect(status().isOk());

    mockMvc.perform(get("/necesidades"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].descripcion").value("Arroz"))
        .andExpect(jsonPath("$[1].descripcion").value("Leche"));
  }

  @Test
  void getNecesidades_filtradoPorProductoSolicitadoID_retorna200() throws Exception {
    NecesidadMaterialDTO necesidad1 = new NecesidadMaterialDTO(
        null, "entidad-1", 3, "Arroz", 100, "producto-1", TipoNecesidadMaterialEnum.EXTRAORDINARIA
    );
    NecesidadMaterialDTO necesidad2 = new NecesidadMaterialDTO(
        null, "entidad-2", 5, "Leche", 50, "producto-2", TipoNecesidadMaterialEnum.RECURRENTE
    );
    NecesidadMaterialDTO necesidad3 = new NecesidadMaterialDTO(
        null, "entidad-3", 2, "Mas arroz", 30, "producto-1", TipoNecesidadMaterialEnum.EXTRAORDINARIA
    );

    mockMvc.perform(post("/necesidades")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(necesidad1)))
        .andExpect(status().isOk());

    mockMvc.perform(post("/necesidades")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(necesidad2)))
        .andExpect(status().isOk());

    mockMvc.perform(post("/necesidades")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(necesidad3)))
        .andExpect(status().isOk());

    mockMvc.perform(get("/necesidades").param("productoSolicitadoID", "producto-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].productoSolicitadoID").value("producto-1"))
        .andExpect(jsonPath("$[1].productoSolicitadoID").value("producto-1"));
  }

  @Test
  void getNecesidades_listaVacia_retorna200() throws Exception {
    mockMvc.perform(get("/necesidades"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void postSatisfaccion_exitosa_retorna200() throws Exception {
    NecesidadMaterialDTO necesidad = new NecesidadMaterialDTO(
        null, "entidad-1", 3, "Arroz", 100, "producto-1", TipoNecesidadMaterialEnum.EXTRAORDINARIA
    );

    MvcResult result = mockMvc.perform(post("/necesidades")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(necesidad)))
        .andExpect(status().isOk())
        .andReturn();

    NecesidadMaterialDTO creada = objectMapper.readValue(
        result.getResponse().getContentAsString(), NecesidadMaterialDTO.class);

    SatisfaccionRequest satisfaccion = new SatisfaccionRequest(30);

    mockMvc.perform(post("/necesidades/" + creada.id() + "/satisfaccion")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(satisfaccion)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.cantidadObjetivo").value(70));
  }

  @Test
  void postSatisfaccion_noEncontrada_retorna404() throws Exception {
    SatisfaccionRequest satisfaccion = new SatisfaccionRequest(10);

    mockMvc.perform(post("/necesidades/inexistente-id/satisfaccion")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(satisfaccion)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").isNotEmpty());
  }

  @Test
  void postSatisfaccion_cantidadInvalida_retorna400() throws Exception {
    NecesidadMaterialDTO necesidad = new NecesidadMaterialDTO(
        null, "entidad-1", 3, "Arroz", 100, "producto-1", TipoNecesidadMaterialEnum.EXTRAORDINARIA
    );

    MvcResult result = mockMvc.perform(post("/necesidades")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(necesidad)))
        .andExpect(status().isOk())
        .andReturn();

    NecesidadMaterialDTO creada = objectMapper.readValue(
        result.getResponse().getContentAsString(), NecesidadMaterialDTO.class);

    SatisfaccionRequest satisfaccion = new SatisfaccionRequest(0);

    mockMvc.perform(post("/necesidades/" + creada.id() + "/satisfaccion")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(satisfaccion)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").isNotEmpty());
  }

  @Test
  void postSatisfaccion_cantidadNegativa_retorna400() throws Exception {
    NecesidadMaterialDTO necesidad = new NecesidadMaterialDTO(
        null, "entidad-1", 3, "Arroz", 100, "producto-1", TipoNecesidadMaterialEnum.EXTRAORDINARIA
    );

    MvcResult result = mockMvc.perform(post("/necesidades")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(necesidad)))
        .andExpect(status().isOk())
        .andReturn();

    NecesidadMaterialDTO creada = objectMapper.readValue(
        result.getResponse().getContentAsString(), NecesidadMaterialDTO.class);

    SatisfaccionRequest satisfaccion = new SatisfaccionRequest(-5);

    mockMvc.perform(post("/necesidades/" + creada.id() + "/satisfaccion")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(satisfaccion)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").isNotEmpty());
  }

  @Test
  void postSatisfaccion_reduceCantidadACeroCuandoExcede_retorna200() throws Exception {
    NecesidadMaterialDTO necesidad = new NecesidadMaterialDTO(
        null, "entidad-1", 3, "Arroz", 10, "producto-1", TipoNecesidadMaterialEnum.EXTRAORDINARIA
    );

    MvcResult result = mockMvc.perform(post("/necesidades")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(necesidad)))
        .andExpect(status().isOk())
        .andReturn();

    NecesidadMaterialDTO creada = objectMapper.readValue(
        result.getResponse().getContentAsString(), NecesidadMaterialDTO.class);

    SatisfaccionRequest satisfaccion = new SatisfaccionRequest(50);

    mockMvc.perform(post("/necesidades/" + creada.id() + "/satisfaccion")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(satisfaccion)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.cantidadObjetivo").value(0));
  }
}
