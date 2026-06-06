package ar.edu.utn.dds.k3003.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.EntidadBeneficaDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
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
class EntidadesControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void postEntidades_creacionExitosa_retorna200() throws Exception {
    EntidadBeneficaDTO entidad =
        new EntidadBeneficaDTO(null, "Fundacion ABC", "Av Siempre Viva 742", "1234-5678", "info@abc.org");

    mockMvc.perform(post("/entidades")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(entidad)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").isNotEmpty())
        .andExpect(jsonPath("$.razonSocial").value("Fundacion ABC"))
        .andExpect(jsonPath("$.domicilio").value("Av Siempre Viva 742"))
        .andExpect(jsonPath("$.telefono").value("1234-5678"))
        .andExpect(jsonPath("$.correo").value("info@abc.org"));
  }

  @Test
  void postEntidades_duplicado_retornaError() throws Exception {
    EntidadBeneficaDTO entidad =
        new EntidadBeneficaDTO(null, "Fundacion ABC", "Av Siempre Viva 742", "1234-5678", "info@abc.org");

    MvcResult result = mockMvc.perform(post("/entidades")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(entidad)))
        .andExpect(status().isOk())
        .andReturn();

    EntidadBeneficaDTO creada = objectMapper.readValue(
        result.getResponse().getContentAsString(), EntidadBeneficaDTO.class);

    EntidadBeneficaDTO duplicada =
        new EntidadBeneficaDTO(creada.id(), "Otra Fundacion", "Otra Dir", "9999-9999", "otra@mail.com");

    mockMvc.perform(post("/entidades")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(duplicada)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").isNotEmpty());
  }

  @Test
  void postEntidades_bodyNulo_retorna400() throws Exception {
    mockMvc.perform(post("/entidades")
            .contentType(MediaType.APPLICATION_JSON)
            .content(""))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getEntidades_listaVacia_retorna200ConArrayVacio() throws Exception {
    mockMvc.perform(get("/entidades"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  void getEntidades_conDatos_retorna200ConEntidades() throws Exception {
    EntidadBeneficaDTO entidad1 =
        new EntidadBeneficaDTO(null, "Fundacion ABC", "Av Siempre Viva 742", "1234-5678", "info@abc.org");
    EntidadBeneficaDTO entidad2 =
        new EntidadBeneficaDTO(null, "ONG XYZ", "Calle 123", "9876-5432", "contacto@xyz.org");

    mockMvc.perform(post("/entidades")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(entidad1)))
        .andExpect(status().isOk());

    mockMvc.perform(post("/entidades")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(entidad2)))
        .andExpect(status().isOk());

    // Listar todas
    mockMvc.perform(get("/entidades"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].razonSocial").value("Fundacion ABC"))
        .andExpect(jsonPath("$[1].razonSocial").value("ONG XYZ"));
  }

  @Test
  void getEntidadById_existente_retorna200() throws Exception {
    EntidadBeneficaDTO entidad =
        new EntidadBeneficaDTO(null, "Fundacion ABC", "Av Siempre Viva 742", "1234-5678", "info@abc.org");

    MvcResult result = mockMvc.perform(post("/entidades")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(entidad)))
        .andExpect(status().isOk())
        .andReturn();

    EntidadBeneficaDTO creada = objectMapper.readValue(
        result.getResponse().getContentAsString(), EntidadBeneficaDTO.class);

    mockMvc.perform(get("/entidades/{id}", creada.id()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(creada.id()))
        .andExpect(jsonPath("$.razonSocial").value("Fundacion ABC"))
        .andExpect(jsonPath("$.domicilio").value("Av Siempre Viva 742"))
        .andExpect(jsonPath("$.telefono").value("1234-5678"))
        .andExpect(jsonPath("$.correo").value("info@abc.org"));
  }

  @Test
  void getEntidadById_noExistente_retorna404() throws Exception {
    mockMvc.perform(get("/entidades/{id}", "id-inexistente"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").isNotEmpty());
  }
}
