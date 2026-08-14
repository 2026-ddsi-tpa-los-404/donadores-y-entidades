package ar.edu.utn.dds.k3003;

import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.*;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.InsigniaDTO;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonadoresYEntidades;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaIncentivos;
import ar.edu.utn.dds.k3003.config.MetricsService;
import ar.edu.utn.dds.k3003.exceptions.DonadorNoEncontradoException;
import ar.edu.utn.dds.k3003.exceptions.DonadorYaExistenteException;
import ar.edu.utn.dds.k3003.model.Donador;
import ar.edu.utn.dds.k3003.model.EntidadBenefica;
import ar.edu.utn.dds.k3003.model.NecesidadMaterial;
import ar.edu.utn.dds.k3003.model.Queja;
import ar.edu.utn.dds.k3003.repositories.donadorStats.DonadorStatsDataMapper;
import ar.edu.utn.dds.k3003.repositories.donadores.DonadorJpaRepository;
import ar.edu.utn.dds.k3003.repositories.donadores.DonadoresYEntidadesDataMapper;
import ar.edu.utn.dds.k3003.repositories.entidad.EntidadBeneficaJpaRepository;
import ar.edu.utn.dds.k3003.repositories.entidad.EntidadesBeneficasDataMapper;
import ar.edu.utn.dds.k3003.repositories.necesidadMaterial.NecesidadMaterialJpaRepository;
import ar.edu.utn.dds.k3003.repositories.necesidadMaterial.NecesidadesMaterialesDataMapper;
import ar.edu.utn.dds.k3003.repositories.quejas.QuejaJpaRepository;
import ar.edu.utn.dds.k3003.repositories.quejas.QuejasDataMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class Fachada implements FachadaDonadoresYEntidades {

  private static final Logger log = LoggerFactory.getLogger(Fachada.class);

  private final DonadorJpaRepository donadoresRepository;
  private final QuejaJpaRepository quejasRepository;
  private final EntidadBeneficaJpaRepository entidadesBeneficasRepository;
  private final NecesidadMaterialJpaRepository necesidadesMaterialesRepository;
  private final MetricsService metricsService;
  private final FachadaIncentivos fachadaIncentivos;

  private final DonadoresYEntidadesDataMapper donadoresYEntidadesDataMapper =
      new DonadoresYEntidadesDataMapper();
  private final QuejasDataMapper quejasDataMapper = new QuejasDataMapper();
  private final EntidadesBeneficasDataMapper entidadesBeneficasDataMapper = new EntidadesBeneficasDataMapper();
  private final NecesidadesMaterialesDataMapper necesidadesMaterialesDataMapper = new NecesidadesMaterialesDataMapper();
  private final DonadorStatsDataMapper donadorStatsDataMapper = new DonadorStatsDataMapper();

  public Fachada(
      DonadorJpaRepository donadoresRepository,
      QuejaJpaRepository quejasRepository,
      EntidadBeneficaJpaRepository entidadesBeneficasRepository,
      NecesidadMaterialJpaRepository necesidadesMaterialesRepository,
      MetricsService metricsService,
      FachadaIncentivos fachadaIncentivos) {
    this.donadoresRepository = donadoresRepository;
    this.quejasRepository = quejasRepository;
    this.entidadesBeneficasRepository = entidadesBeneficasRepository;
    this.necesidadesMaterialesRepository = necesidadesMaterialesRepository;
    this.metricsService = metricsService;
    this.fachadaIncentivos = fachadaIncentivos;
  }

  @Override
  public DonadorDTO agregarDonador(DonadorDTO donadorDTO) {
    log.info("[METRICA] Operación: agregarDonador - nombre={} apellido={}", donadorDTO.nombre(), donadorDTO.apellido());
    if (donadorDTO.id() != null && this.donadoresRepository.findById(Long.parseLong(donadorDTO.id())).isPresent()) {
      throw new DonadorYaExistenteException("Ya existe un donador con ese ID");
    }

    Donador donador = donadoresYEntidadesDataMapper.toDonador(donadorDTO);
    Donador donadorGuardado = this.donadoresRepository.save(donador);

    metricsService.incrementarDonadorAgregado();
    metricsService.incrementarConsultaDB();
    log.info("[METRICA] Enviada: dds.donadores.agregados +1 | dds.consultas.db +1 | donadorId={}", donadorGuardado.getId());

    return donadoresYEntidadesDataMapper.toDonadorDTO(donadorGuardado);
  }

  @Override
  public DonadorDTO buscarDonadorPorID(String donadorID) throws NoSuchElementException {
    log.info("[METRICA] Operación: buscarDonadorPorID - id={}", donadorID);
    metricsService.incrementarConsultaDB();
    Optional<Donador> donadorOptional = this.donadoresRepository.findById(Long.parseLong(donadorID));

    if (donadorOptional.isEmpty()) {
      log.warn("[METRICA] Donador no encontrado - id={}", donadorID);
      throw new DonadorNoEncontradoException("No existe un donador con ese ID");
    }
    Donador donadorFinal = donadorOptional.get();

    return donadoresYEntidadesDataMapper.toDonadorDTO(donadorFinal);
  }

  @Override
  public Boolean puedeDonar(String donadorID) throws NoSuchElementException {
    log.info("[METRICA] Operación: puedeDonar - donadorId={}", donadorID);
    DonadorDTO donadorDTO = this.buscarDonadorPorID(donadorID);

    return switch (donadorDTO.estado()) {
      case VERIFICADO -> true;
      case SOSPECHOSO -> Math.random() < 0.5;
      case BANEADO -> false;
    };
  }

  @Override
  public DonadorDTO modificarEstado(String donadorID, EstadoDonadorEnum estado)
      throws NoSuchElementException {
    log.info("[METRICA] Operación: modificarEstado - donadorId={} nuevoEstado={}", donadorID, estado);

    if (estado == null) {
      throw new RuntimeException("El estado no puede ser nulo");
    }

    Optional<Donador> donadorOptional = this.donadoresRepository.findById(Long.parseLong(donadorID));

    if (donadorOptional.isEmpty()) {
      throw new DonadorNoEncontradoException("No existe un donador con ese ID");
    }

    Donador donadorFinal = donadorOptional.get();
    donadorFinal.setEstado(estado);

    this.donadoresRepository.save(donadorFinal);

    return donadoresYEntidadesDataMapper.toDonadorDTO(donadorFinal);
  }

  @Override
  public DonadorDTO modifcarCategoria(String donadorID, String categoria)
      throws NoSuchElementException {

    if (categoria == null) {
      throw new RuntimeException("La categoria no puede ser nula");
    }

    Optional<Donador> donadorOptional = this.donadoresRepository.findById(Long.parseLong(donadorID));
    if (donadorOptional.isEmpty()) {
      throw new DonadorNoEncontradoException("No existe un donador con ese ID");
    }
    Donador donadorFinal = donadorOptional.get();
    donadorFinal.setCategoria(categoria);

    this.donadoresRepository.save(donadorFinal);

    return donadoresYEntidadesDataMapper.toDonadorDTO(donadorFinal);
  }

  @Override
  public DonadorStatsDTO estadisticasDonador(String donadorID) {
    DonadorDTO donador = this.buscarDonadorPorID(donadorID);

    List<String> insigniasIDs = this.fachadaIncentivos.getInsigniasDeDonador(donadorID)
            .stream()
            .map(InsigniaDTO::id)
            .toList();

    String misionActualID = this.fachadaIncentivos.getMisionEnCursoDeDonador(donadorID).id();

    return donadorStatsDataMapper.toDonadorStatsDTO(donador, misionActualID, insigniasIDs);
  }

  @Override
  public QuejaDTO agregarQueja(QuejaDTO quejaDTO) throws NoSuchElementException {
    log.info("[METRICA] Operación: agregarQueja - donadorId={} donacionId={}", quejaDTO != null ? quejaDTO.donadorID() : null, quejaDTO != null ? quejaDTO.donacionID() : null);
    if (quejaDTO == null) {
      throw new RuntimeException("La queja no puede ser nula");
    }

    if (quejaDTO.id() != null && this.quejasRepository.findById(Long.parseLong(quejaDTO.id())).isPresent()) {
      throw new RuntimeException("Ya existe una queja con ese ID");
    }

    Queja queja = quejasDataMapper.toQueja(quejaDTO);
    queja.setFecha(LocalDate.now());

    Queja quejaGuardado = this.quejasRepository.save(queja);

    metricsService.incrementarQuejaRegistrada();
    metricsService.incrementarConsultaDB();
    log.info("[METRICA] Enviada: dds.quejas.registradas +1 | dds.consultas.db +1 | quejaId={}", quejaGuardado.getId());

    // Actualizar estado del donador segun cantidad de quejas
    String donadorId = quejaDTO.donadorID();
    Optional<Donador> donadorOptional = this.donadoresRepository.findById(Long.parseLong(donadorId));
    if (donadorOptional.isPresent()) {
      int totalQuejas = this.quejasRepository.findAllByDonadorId(donadorId).size();
      Donador donador = donadorOptional.get();

      if (totalQuejas >= 10) {
        donador.setEstado(EstadoDonadorEnum.BANEADO);
      } else if (totalQuejas >= 5) {
        donador.setEstado(EstadoDonadorEnum.SOSPECHOSO);
      }

      this.donadoresRepository.save(donador);
    }

    return quejasDataMapper.toQuejaDTO(quejaGuardado);
  }

  @Override
  public List<QuejaDTO> obtenerQuejasDe(String donadorID) throws NoSuchElementException {
    List<Queja> quejas = quejasRepository.findAllByDonadorId(donadorID);

    if (quejas.isEmpty()) {
      throw new DonadorNoEncontradoException("No existen quejas para el donador con ese ID");
    }

    return quejas.stream()
            .map(quejasDataMapper::toQuejaDTO)
            .toList();
  }

  @Override
  public EntidadBeneficaDTO agregarEntidad(EntidadBeneficaDTO entidadBeneficaDTO) {
    log.info("[METRICA] Operación: agregarEntidad - razonSocial={}", entidadBeneficaDTO != null ? entidadBeneficaDTO.razonSocial() : null);
    if (entidadBeneficaDTO == null) {
      throw new RuntimeException("La entidad benefica no puede ser nula");
    }

    if (entidadBeneficaDTO.id() != null && this.entidadesBeneficasRepository.findById(Long.parseLong(entidadBeneficaDTO.id())).isPresent()) {
      throw new RuntimeException("Ya existe una entidad benefica con ese ID");
    }

    EntidadBenefica entidadBenefica = entidadesBeneficasDataMapper.toEntidadBenefica(entidadBeneficaDTO);
    EntidadBenefica entidadBeneficaGuardado = this.entidadesBeneficasRepository.save(entidadBenefica);

    metricsService.incrementarEntidadAgregada();
    metricsService.incrementarConsultaDB();
    log.info("[METRICA] Enviada: dds.entidades.agregadas +1 | dds.consultas.db +1 | entidadId={}", entidadBeneficaGuardado.getId());

    return entidadesBeneficasDataMapper.toEntidadBeneficaDTO(entidadBeneficaGuardado);
  }

  @Override
  public EntidadBeneficaDTO buscarEntidadPorID(String entidadID) throws NoSuchElementException {
    metricsService.incrementarConsultaDB();
    Optional<EntidadBenefica> entidadBeneficaOptional = this.entidadesBeneficasRepository.findById(Long.parseLong(entidadID));
    if (entidadBeneficaOptional.isEmpty()) {
      throw new NoSuchElementException("No existe una entidad benefica con ese ID");
    }
    EntidadBenefica entidadBenefica = entidadBeneficaOptional.get();
    return entidadesBeneficasDataMapper.toEntidadBeneficaDTO(entidadBenefica);
  }

  @Override
  public NecesidadMaterialDTO registrarNecesidad(NecesidadMaterialDTO necesidadMaterialDTO) {
    log.info("[METRICA] Operación: registrarNecesidad - entidadId={} productoId={}", necesidadMaterialDTO != null ? necesidadMaterialDTO.entidadID() : null, necesidadMaterialDTO != null ? necesidadMaterialDTO.productoSolicitadoID() : null);
    if (necesidadMaterialDTO == null) {
      throw new RuntimeException("La necesidad material no puede ser nula");
    }

    if (necesidadMaterialDTO.id() != null && this.necesidadesMaterialesRepository.findById(Long.parseLong(necesidadMaterialDTO.id())).isPresent()) {
      throw new RuntimeException("Ya existe una necesidad material con ese ID");
    }

    NecesidadMaterial necesidadMaterial = necesidadesMaterialesDataMapper.toNecesidadMaterial(necesidadMaterialDTO);
    NecesidadMaterial necesidadMaterialGuardado = this.necesidadesMaterialesRepository.save(necesidadMaterial);

    metricsService.incrementarNecesidadRegistrada();
    metricsService.incrementarConsultaDB();
    log.info("[METRICA] Enviada: dds.necesidades.registradas +1 | dds.consultas.db +1 | necesidadId={}", necesidadMaterialGuardado.getId());

    return necesidadesMaterialesDataMapper.toNecesidadMaterialDTO(necesidadMaterialGuardado);
  }

  @Override
  public NecesidadMaterialDTO satisfacerNecesidad(String necesidadID, Integer cantidad)
      throws NoSuchElementException {
    if (cantidad <= 0) {
      throw new RuntimeException("La cantidad debe ser mayor a 0");
    }

    Optional<NecesidadMaterial> necesidadMaterial = this.necesidadesMaterialesRepository.findById(Long.parseLong(necesidadID));

    if (necesidadMaterial.isEmpty()) {
      throw new NoSuchElementException("No existe una necesidad con ese ID");
    }

    NecesidadMaterial necesidad = necesidadMaterial.get();

    if (necesidad.getTipo() == TipoNecesidadMaterialEnum.RECURRENTE) {
      if (necesidad.estaSatisfechaEnPeriodoActual()) {
        throw new RuntimeException(
            "La necesidad recurrente ya fue satisfecha en el período actual. "
            + "No puede recibir más donaciones hasta el próximo período");
      }

      necesidad.setFechaUltimaSatisfaccion(LocalDate.now());
    }

    int cantidadObjetivo = necesidad.getCantidadObjetivo() - cantidad;
    necesidad.setCantidadObjetivo(Math.max(cantidadObjetivo, 0));

    NecesidadMaterial necesidadActualizada = this.necesidadesMaterialesRepository.save(necesidad);

    metricsService.incrementarNecesidadSatisfecha();
    metricsService.incrementarConsultaDB();
    log.info("[METRICA] Enviada: dds.necesidades.satisfechas +1 | dds.consultas.db +1 | necesidadId={} cantidadRestante={}", necesidadID, necesidadActualizada.getCantidadObjetivo());

    return necesidadesMaterialesDataMapper.toNecesidadMaterialDTO(necesidadActualizada);
  }

  @Override
  public List<NecesidadMaterialDTO> obtenerNecesidadesInsatisfechasDe(String productoSolicitadoID) {
    return this.necesidadesMaterialesRepository
            .findAllByProductoSolicitadoIdAndCantidadObjetivoGreaterThan(productoSolicitadoID, 0)
            .stream()
            .map(necesidadesMaterialesDataMapper::toNecesidadMaterialDTO)
            .toList();
  }

  public List<NecesidadMaterialDTO> obtenerNecesidadesInsatisfechas() {
    return this.necesidadesMaterialesRepository.findAll()
            .stream()
            .filter(n -> n.getCantidadObjetivo() > 0)
            .map(necesidadesMaterialesDataMapper::toNecesidadMaterialDTO)
            .toList();
  }

  public List<DonadorDTO> listarDonadores() {
    return this.donadoresRepository.findAll()
        .stream()
        .map(donadoresYEntidadesDataMapper::toDonadorDTO)
        .toList();
  }

  public List<EntidadBeneficaDTO> listarEntidades() {
    return this.entidadesBeneficasRepository
            .findAll()
            .stream()
            .map(entidadesBeneficasDataMapper::toEntidadBeneficaDTO)
            .toList();
  }

  @Override
  public void setFachadaIncentivos(FachadaIncentivos fachadaIncentivos) {
    // Se inyecta por constructor, este método existe por compatibilidad con la interfaz
  }

  // Métodos para limpiar la base de datos
  public void limpiarDonadores() {
    this.donadoresRepository.deleteAll();
  }

  public void limpiarQuejas() {
    this.quejasRepository.deleteAll();
  }

  public void limpiarEntidades() {
    this.entidadesBeneficasRepository.deleteAll();
  }

  public void limpiarNecesidades() {
    this.necesidadesMaterialesRepository.deleteAll();
  }

  public void limpiarTodo() {
    this.quejasRepository.deleteAll();
    this.necesidadesMaterialesRepository.deleteAll();
    this.entidadesBeneficasRepository.deleteAll();
    this.donadoresRepository.deleteAll();
  }

  // =========================================================================
  // NUEVOS MÉTODOS QUE FALTABAN PARA EL BOT
  // =========================================================================

  public EntidadBeneficaDTO modificarEntidad(String id, EntidadBeneficaDTO dto) throws NoSuchElementException {
    Optional<EntidadBenefica> entidadOpt = this.entidadesBeneficasRepository.findById(Long.parseLong(id));
    if (entidadOpt.isEmpty()) {
      throw new NoSuchElementException("No existe una entidad benefica con ese ID");
    }
    EntidadBenefica entidad = entidadOpt.get();

    // Actualizamos solo si vienen en el DTO
    if (dto.razonSocial() != null) entidad.setRazonSocial(dto.razonSocial());
    if (dto.domicilio() != null) entidad.setDomicilio(dto.domicilio());
    if (dto.telefono() != null) entidad.setTelefono(dto.telefono());
    if (dto.correo() != null) entidad.setCorreo(dto.correo());

    EntidadBenefica guardada = this.entidadesBeneficasRepository.save(entidad);
    metricsService.incrementarConsultaDB();
    return entidadesBeneficasDataMapper.toEntidadBeneficaDTO(guardada);
  }

  public NecesidadMaterialDTO buscarNecesidadPorID(String id) throws NoSuchElementException {
    metricsService.incrementarConsultaDB();
    Optional<NecesidadMaterial> necesidadOpt = this.necesidadesMaterialesRepository.findById(Long.parseLong(id));
    if (necesidadOpt.isEmpty()) {
      throw new NoSuchElementException("No existe una necesidad material con ese ID");
    }
    return necesidadesMaterialesDataMapper.toNecesidadMaterialDTO(necesidadOpt.get());
  }

  public NecesidadMaterialDTO modificarNecesidad(String id, NecesidadMaterialDTO dto) throws NoSuchElementException {
    Optional<NecesidadMaterial> necesidadOpt = this.necesidadesMaterialesRepository.findById(Long.parseLong(id));
    if (necesidadOpt.isEmpty()) {
      throw new NoSuchElementException("No existe una necesidad material con ese ID");
    }
    NecesidadMaterial necesidad = necesidadOpt.get();

    // Modificamos solo urgencia, descripción y cantidad (como lo definimos en el bot)
    if (dto.nivelDeUrgencia() != null) necesidad.setNivelDeUrgencia(dto.nivelDeUrgencia());
    if (dto.descripcion() != null) necesidad.setDescripcion(dto.descripcion());
    if (dto.cantidadObjetivo() != null) necesidad.setCantidadObjetivo(dto.cantidadObjetivo());

    NecesidadMaterial guardada = this.necesidadesMaterialesRepository.save(necesidad);
    metricsService.incrementarConsultaDB();
    return necesidadesMaterialesDataMapper.toNecesidadMaterialDTO(guardada);
  }

  public void eliminarNecesidad(String id) throws NoSuchElementException {
    Optional<NecesidadMaterial> necesidadOpt = this.necesidadesMaterialesRepository.findById(Long.parseLong(id));
    if (necesidadOpt.isEmpty()) {
      throw new NoSuchElementException("No existe una necesidad material con ese ID");
    }
    this.necesidadesMaterialesRepository.delete(necesidadOpt.get());
    metricsService.incrementarConsultaDB();
  }
}
