package ar.edu.utn.dds.k3003.model;

import static org.mockito.Mockito.*;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.*;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaIncentivos;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.InsigniaDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.MisionDTO;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DonadoresYEntidadesTest {

  Fachada instancia;
  @Mock FachadaIncentivos fachadaIncentivos;

  DonadorDTO donadorEjemplo;
  EntidadBeneficaDTO entidadEjemplo;
  NecesidadMaterialDTO necesidadEjemplo;
  QuejaDTO quejaEjemplo;

  @BeforeEach
  void setUp() {
    instancia = new Fachada();
    instancia.setFachadaIncentivos(fachadaIncentivos);

    donadorEjemplo =
        new DonadorDTO(
            null, "Juan", "Perez", 30, "juan@mail.com",
            "12345678", "Calle Falsa 123",
            EstadoDonadorEnum.VERIFICADO, "Ocasional");

    entidadEjemplo =
        new EntidadBeneficaDTO(null, "Fundacion ABC", "Av Siempre Viva 742", "1234-5678", "info@abc.org");

    necesidadEjemplo =
        new NecesidadMaterialDTO(null, "entidad1", 5, "Necesitamos alimentos", 10, "producto1", TipoNecesidadMaterialEnum.RECURRENTE);

    quejaEjemplo =
        new QuejaDTO(null, "donacion1", "donador1", null, "Mala experiencia");
  }

  // ==================== Donador ====================

  @Test
  void agregarDonadorAsignaId() {
    DonadorDTO resultado = instancia.agregarDonador(donadorEjemplo);

    Assertions.assertNotNull(resultado.id());
    Assertions.assertEquals("Juan", resultado.nombre());
    Assertions.assertEquals("Perez", resultado.apellido());
    Assertions.assertEquals(30, resultado.edad());
  }

  @Test
  void agregarDonadorConservaDatos() {
    DonadorDTO resultado = instancia.agregarDonador(donadorEjemplo);

    Assertions.assertEquals(donadorEjemplo.email(), resultado.email());
    Assertions.assertEquals(donadorEjemplo.nroDocumento(), resultado.nroDocumento());
    Assertions.assertEquals(donadorEjemplo.domicilio(), resultado.domicilio());
  }

  @Test
  void agregarDonadorNuloTiraExcepcion() {
    Assertions.assertThrows(RuntimeException.class, () -> instancia.agregarDonador(null));
  }

  @Test
  void agregarDonadorDuplicadoTiraExcepcion() {
    DonadorDTO guardado = instancia.agregarDonador(donadorEjemplo);
    Assertions.assertThrows(RuntimeException.class, () -> instancia.agregarDonador(guardado));
  }

  @Test
  void buscarDonadorPorIdExistente() {
    DonadorDTO guardado = instancia.agregarDonador(donadorEjemplo);
    DonadorDTO encontrado = instancia.buscarDonadorPorID(guardado.id());

    Assertions.assertEquals(guardado.id(), encontrado.id());
    Assertions.assertEquals(guardado.nombre(), encontrado.nombre());
  }

  @Test
  void buscarDonadorPorIdInexistenteTiraExcepcion() {
    Assertions.assertThrows(RuntimeException.class, () -> instancia.buscarDonadorPorID("999"));
  }

  // ==================== Estado y Categoria ====================

  @Test
  void modificarEstadoCambiaElEstado() {
    DonadorDTO guardado = instancia.agregarDonador(donadorEjemplo);

    DonadorDTO actualizado = instancia.modificarEstado(guardado.id(), EstadoDonadorEnum.BANEADO);

    Assertions.assertEquals(EstadoDonadorEnum.BANEADO, actualizado.estado());
  }

  @Test
  void modificarEstadoInexistenteTiraExcepcion() {
    Assertions.assertThrows(
        RuntimeException.class,
        () -> instancia.modificarEstado("999", EstadoDonadorEnum.BANEADO));
  }

  @Test
  void modificarEstadoNuloTiraExcepcion() {
    DonadorDTO guardado = instancia.agregarDonador(donadorEjemplo);
    Assertions.assertThrows(
        RuntimeException.class,
        () -> instancia.modificarEstado(guardado.id(), null));
  }

  @Test
  void modificarCategoriaCambiaLaCategoria() {
    DonadorDTO guardado = instancia.agregarDonador(donadorEjemplo);

    instancia.modifcarCategoria(guardado.id(), "Premium");

    DonadorDTO actualizado = instancia.buscarDonadorPorID(guardado.id());
    Assertions.assertEquals("Premium", actualizado.categoria());
  }

  @Test
  void modificarCategoriaInexistenteTiraExcepcion() {
    Assertions.assertThrows(
        RuntimeException.class,
        () -> instancia.modifcarCategoria("999", "Premium"));
  }

  @Test
  void modificarCategoriaNulaTiraExcepcion() {
    DonadorDTO guardado = instancia.agregarDonador(donadorEjemplo);
    Assertions.assertThrows(
        RuntimeException.class,
        () -> instancia.modifcarCategoria(guardado.id(), null));
  }

  // ==================== Puede Donar ====================

  @Test
  void donadorVerificadoPuedeDonar() {
    DonadorDTO guardado = instancia.agregarDonador(donadorEjemplo);
    Assertions.assertTrue(instancia.puedeDonar(guardado.id()));
  }

  @Test
  void donadorBaneadoNoPuedeDonar() {
    DonadorDTO guardado = instancia.agregarDonador(donadorEjemplo);
    instancia.modificarEstado(guardado.id(), EstadoDonadorEnum.BANEADO);

    Assertions.assertFalse(instancia.puedeDonar(guardado.id()));
  }

  @Test
  void puedeDonarInexistenteTiraExcepcion() {
    Assertions.assertThrows(RuntimeException.class, () -> instancia.puedeDonar("999"));
  }

  // ==================== Entidad Benefica ====================

  @Test
  void agregarEntidadAsignaId() {
    EntidadBeneficaDTO resultado = instancia.agregarEntidad(entidadEjemplo);

    Assertions.assertNotNull(resultado.id());
    Assertions.assertEquals("Fundacion ABC", resultado.razonSocial());
  }

  @Test
  void agregarEntidadNulaTiraExcepcion() {
    Assertions.assertThrows(RuntimeException.class, () -> instancia.agregarEntidad(null));
  }

  @Test
  void agregarEntidadDuplicadaTiraExcepcion() {
    EntidadBeneficaDTO guardada = instancia.agregarEntidad(entidadEjemplo);
    Assertions.assertThrows(RuntimeException.class, () -> instancia.agregarEntidad(guardada));
  }

  @Test
  void buscarEntidadPorIdExistente() {
    EntidadBeneficaDTO guardada = instancia.agregarEntidad(entidadEjemplo);
    EntidadBeneficaDTO encontrada = instancia.buscarEntidadPorID(guardada.id());

    Assertions.assertEquals(guardada.id(), encontrada.id());
    Assertions.assertEquals(guardada.razonSocial(), encontrada.razonSocial());
  }

  @Test
  void buscarEntidadPorIdInexistenteTiraExcepcion() {
    Assertions.assertThrows(RuntimeException.class, () -> instancia.buscarEntidadPorID("999"));
  }

  // ==================== Necesidad Material ====================

  @Test
  void registrarNecesidadAsignaId() {
    NecesidadMaterialDTO resultado = instancia.registrarNecesidad(necesidadEjemplo);

    Assertions.assertNotNull(resultado.id());
    Assertions.assertEquals(necesidadEjemplo.entidadID(), resultado.entidadID());
    Assertions.assertEquals(necesidadEjemplo.cantidadObjetivo(), resultado.cantidadObjetivo());
    Assertions.assertEquals(necesidadEjemplo.productoSolicitadoID(), resultado.productoSolicitadoID());
  }

  @Test
  void registrarNecesidadNulaTiraExcepcion() {
    Assertions.assertThrows(RuntimeException.class, () -> instancia.registrarNecesidad(null));
  }

  @Test
  void registrarNecesidadDuplicadaTiraExcepcion() {
    NecesidadMaterialDTO guardada = instancia.registrarNecesidad(necesidadEjemplo);
    Assertions.assertThrows(RuntimeException.class, () -> instancia.registrarNecesidad(guardada));
  }

  @Test
  void satisfacerNecesidadReduceCantidad() {
    NecesidadMaterialDTO guardada = instancia.registrarNecesidad(necesidadEjemplo);

    NecesidadMaterialDTO resultado = instancia.satisfacerNecesidad(guardada.id(), 3);

    Assertions.assertEquals(7, resultado.cantidadObjetivo());
  }

  @Test
  void satisfacerNecesidadNoQuedaNegativa() {
    NecesidadMaterialDTO guardada = instancia.registrarNecesidad(necesidadEjemplo);

    NecesidadMaterialDTO resultado = instancia.satisfacerNecesidad(guardada.id(), 100);

    Assertions.assertEquals(0, resultado.cantidadObjetivo());
  }

  @Test
  void satisfacerNecesidadInexistenteTiraExcepcion() {
    Assertions.assertThrows(RuntimeException.class, () -> instancia.satisfacerNecesidad("999", 5));
  }

  @Test
  void satisfacerNecesidadConCantidadCeroTiraExcepcion() {
    NecesidadMaterialDTO guardada = instancia.registrarNecesidad(necesidadEjemplo);
    Assertions.assertThrows(
        RuntimeException.class,
        () -> instancia.satisfacerNecesidad(guardada.id(), 0));
  }

  @Test
  void satisfacerNecesidadConCantidadNegativaTiraExcepcion() {
    NecesidadMaterialDTO guardada = instancia.registrarNecesidad(necesidadEjemplo);
    Assertions.assertThrows(
        RuntimeException.class,
        () -> instancia.satisfacerNecesidad(guardada.id(), -1));
  }

  @Test
  void obtenerNecesidadesInsatisfechasDevuelveResultados() {
    NecesidadMaterialDTO guardada = instancia.registrarNecesidad(necesidadEjemplo);

    List<NecesidadMaterialDTO> resultado =
        instancia.obtenerNecesidadesInsatisfechasDe("producto1");

    Assertions.assertEquals(1, resultado.size());
    Assertions.assertEquals(guardada.id(), resultado.getFirst().id());
  }

  @Test
  void obtenerNecesidadesInsatisfechasVaciaSiNoHay() {
    List<NecesidadMaterialDTO> resultado =
        instancia.obtenerNecesidadesInsatisfechasDe("productoInexistente");

    Assertions.assertNotNull(resultado);
    Assertions.assertTrue(resultado.isEmpty());
  }

  // ==================== Queja ====================

  @Test
  void agregarQuejaAsignaId() {
    QuejaDTO resultado = instancia.agregarQueja(quejaEjemplo);

    Assertions.assertNotNull(resultado.id());
    Assertions.assertEquals(quejaEjemplo.descripcion(), resultado.descripcion());
  }

  @Test
  void agregarQuejaNulaTiraExcepcion() {
    Assertions.assertThrows(RuntimeException.class, () -> instancia.agregarQueja(null));
  }

  @Test
  void agregarQuejaDuplicadaTiraExcepcion() {
    QuejaDTO guardada = instancia.agregarQueja(quejaEjemplo);
    Assertions.assertThrows(RuntimeException.class, () -> instancia.agregarQueja(guardada));
  }

  @Test
  void obtenerQuejasDeDevuelveQuejas() {
    QuejaDTO guardada = instancia.agregarQueja(quejaEjemplo);

    List<QuejaDTO> resultado = instancia.obtenerQuejasDe(quejaEjemplo.donadorID());

    Assertions.assertEquals(1, resultado.size());
    Assertions.assertEquals(guardada.id(), resultado.getFirst().id());
  }

  @Test
  void obtenerQuejasDeSinResultadosTiraExcepcion() {
    Assertions.assertThrows(
        RuntimeException.class,
        () -> instancia.obtenerQuejasDe("donadorSinQuejas"));
  }

  // ==================== Estadisticas Donador ====================

  @Test
  void estadisticasDonadorDevuelveStats() {
    DonadorDTO guardado = instancia.agregarDonador(donadorEjemplo);

    when(fachadaIncentivos.getInsigniasDeDonador(guardado.id()))
        .thenReturn(List.of(
            new InsigniaDTO("ins1", "Insignia 1", "Desc 1"),
            new InsigniaDTO("ins2", "Insignia 2", "Desc 2")));
    when(fachadaIncentivos.getMisionEnCursoDeDonador(guardado.id()))
        .thenReturn(new MisionDTO("mis1", "Mision 1", "ins1", null, null, null));

    DonadorStatsDTO stats = instancia.estadisticasDonador(guardado.id());

    Assertions.assertNotNull(stats);
    Assertions.assertEquals(guardado.nombre(), stats.nombre());
    Assertions.assertEquals(guardado.apellido(), stats.apellido());
    Assertions.assertEquals(guardado.edad(), stats.edad());
    Assertions.assertEquals(guardado.estado(), stats.estado());
    Assertions.assertEquals(2, stats.insigniasID().size());
    Assertions.assertEquals("mis1", stats.misionActualID());

    verify(fachadaIncentivos, times(1)).getInsigniasDeDonador(guardado.id());
    verify(fachadaIncentivos, times(1)).getMisionEnCursoDeDonador(guardado.id());
  }

  @Test
  void estadisticasDonadorInexistenteTiraExcepcion() {
    Assertions.assertThrows(
        RuntimeException.class,
        () -> instancia.estadisticasDonador("999"));
  }
}
