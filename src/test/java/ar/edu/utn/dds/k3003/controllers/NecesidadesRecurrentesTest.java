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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class NecesidadesRecurrentesTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  private NecesidadMaterialDTO crearNecesidadRecurrente(int cantidadObjetivo) {
    return new NecesidadMaterialDTO(
        null, "entidad-1", 3, "100 paquetes de fideos semanales",
        cantidadObjetivo, "producto-fideos", TipoNecesidadMaterialEnum.RECURRENTE
    );
  }

  private String registrarNecesidadYObtenerID(NecesidadMaterialDTO necesidad) throws Exception {
    MvcResult result = mockMvc.perform(post("/necesidades")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(necesidad)))
        .andExpect(status().isOk())
        .andReturn();

    NecesidadMaterialDTO creada = objectMapper.readValue(
        result.getResponse().getContentAsString(), NecesidadMaterialDTO.class);
    return creada.id();
  }

  @Test
  void recurrente_satisfaccionCompleta_retorna200() throws Exception {
    String id = registrarNecesidadYObtenerID(crearNecesidadRecurrente(100));

    SatisfaccionRequest satisfaccion = new SatisfaccionRequest(100);

    mockMvc.perform(post("/necesidades/" + id + "/satisfaccion")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(satisfaccion)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.cantidadObjetivo").value(0))
        .andExpect(jsonPath("$.tipo").value("RECURRENTE"));
  }

  @Test
  void recurrente_puedeSuperarCantidadObjetivo_retorna200() throws Exception {
    String id = registrarNecesidadYObtenerID(crearNecesidadRecurrente(100));

    // Satisfacer con más de la cantidad objetivo (permitido)
    SatisfaccionRequest satisfaccion = new SatisfaccionRequest(150);

    mockMvc.perform(post("/necesidades/" + id + "/satisfaccion")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(satisfaccion)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.cantidadObjetivo").value(0));
  }

  @Test
  void recurrente_noPermiteSegundaEntregaEnMismoPeriodo_retorna400() throws Exception {
    String id = registrarNecesidadYObtenerID(crearNecesidadRecurrente(100));

    SatisfaccionRequest satisfaccion = new SatisfaccionRequest(100);

    // Primera entrega: exitosa
    mockMvc.perform(post("/necesidades/" + id + "/satisfaccion")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(satisfaccion)))
        .andExpect(status().isOk());

    // Segunda entrega en el mismo período: rechazada
    mockMvc.perform(post("/necesidades/" + id + "/satisfaccion")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(satisfaccion)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(
            "La necesidad recurrente ya fue satisfecha en el período actual. "
            + "No puede recibir más donaciones hasta el próximo período"));
  }

  @Test
  void extraordinaria_satisfaccionParcialPermitida_retorna200() throws Exception {
    // Verificar que las necesidades EXTRAORDINARIAS siguen funcionando igual
    NecesidadMaterialDTO necesidad = new NecesidadMaterialDTO(
        null, "entidad-1", 3, "Necesidad extraordinaria",
        100, "producto-1", TipoNecesidadMaterialEnum.EXTRAORDINARIA
    );

    String id = registrarNecesidadYObtenerID(necesidad);

    // Satisfacción parcial permitida para EXTRAORDINARIA
    SatisfaccionRequest satisfaccion = new SatisfaccionRequest(30);

    mockMvc.perform(post("/necesidades/" + id + "/satisfaccion")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(satisfaccion)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.cantidadObjetivo").value(70));
  }

  @Test
  void extraordinaria_multiplasEntregasPermitidas_retorna200() throws Exception {
    NecesidadMaterialDTO necesidad = new NecesidadMaterialDTO(
        null, "entidad-1", 3, "Necesidad extraordinaria",
        100, "producto-1", TipoNecesidadMaterialEnum.EXTRAORDINARIA
    );

    String id = registrarNecesidadYObtenerID(necesidad);

    SatisfaccionRequest satisfaccion = new SatisfaccionRequest(30);

    // Primera entrega
    mockMvc.perform(post("/necesidades/" + id + "/satisfaccion")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(satisfaccion)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.cantidadObjetivo").value(70));

    // Segunda entrega (permitida para EXTRAORDINARIA)
    mockMvc.perform(post("/necesidades/" + id + "/satisfaccion")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(satisfaccion)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.cantidadObjetivo").value(40));
  }
}
