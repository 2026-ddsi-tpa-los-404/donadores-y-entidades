package ar.edu.utn.dds.k3003;

import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.*;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.InsigniaDTO;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonadoresYEntidades;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaIncentivos;
import ar.edu.utn.dds.k3003.exceptions.DonadorNoEncontradoException;
import ar.edu.utn.dds.k3003.exceptions.DonadorYaExistenteException;
import ar.edu.utn.dds.k3003.model.Donador;
import ar.edu.utn.dds.k3003.model.EntidadBenefica;
import ar.edu.utn.dds.k3003.model.NecesidadMaterial;
import ar.edu.utn.dds.k3003.model.Queja;
import ar.edu.utn.dds.k3003.repositories.Repository;
import ar.edu.utn.dds.k3003.repositories.donadorStats.DonadorStatsDataMapper;
import ar.edu.utn.dds.k3003.repositories.donadores.DonadoresYEntidadesDataMapper;
import ar.edu.utn.dds.k3003.repositories.donadores.InMemoryDonadoresRepo;
import ar.edu.utn.dds.k3003.repositories.entidad.EntidadesBeneficasDataMapper;
import ar.edu.utn.dds.k3003.repositories.entidad.InMemoryEntidadesBeneficasRepo;
import ar.edu.utn.dds.k3003.repositories.necesidadMaterial.InMemoryNecesidadesMaterialesRepo;
import ar.edu.utn.dds.k3003.repositories.necesidadMaterial.NecesidadMaterialRepository;
import ar.edu.utn.dds.k3003.repositories.necesidadMaterial.NecesidadesMaterialesDataMapper;
import ar.edu.utn.dds.k3003.repositories.quejas.InMemoryQuejasRepo;
import ar.edu.utn.dds.k3003.repositories.quejas.QuejaRepository;
import ar.edu.utn.dds.k3003.repositories.quejas.QuejasDataMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service()
public class Fachada implements FachadaDonadoresYEntidades {

  private final Repository<Donador> donadoresRepository;
  private final QuejaRepository quejasRepository;
  private final Repository<EntidadBenefica> entidadesBeneficasRepository;
  private final NecesidadMaterialRepository necesidadesMaterialesRepository;


  private FachadaIncentivos fachadaIncentivos;

  private final DonadoresYEntidadesDataMapper donadoresYEntidadesDataMapper =
      new DonadoresYEntidadesDataMapper();
  private final QuejasDataMapper quejasDataMapper = new QuejasDataMapper();
  private final EntidadesBeneficasDataMapper entidadesBeneficasDataMapper = new EntidadesBeneficasDataMapper();
  private final NecesidadesMaterialesDataMapper necesidadesMaterialesDataMapper = new NecesidadesMaterialesDataMapper();
  private final DonadorStatsDataMapper donadorStatsDataMapper = new DonadorStatsDataMapper();

  public Fachada() {
    this.donadoresRepository = new InMemoryDonadoresRepo();
    this.quejasRepository = new InMemoryQuejasRepo();
    this.entidadesBeneficasRepository = new InMemoryEntidadesBeneficasRepo();
    this.necesidadesMaterialesRepository = new InMemoryNecesidadesMaterialesRepo();
  }

  @Override
  public DonadorDTO agregarDonador(DonadorDTO donadorDTO) {
    if (this.donadoresRepository.findById(donadorDTO.id()).isPresent()) {
      throw new DonadorYaExistenteException("Ya existe un donador con ese ID");
    }

    Donador donador = donadoresYEntidadesDataMapper.toDonador(donadorDTO);

    Donador donadorGuardado = this.donadoresRepository.save(donador);

    return donadoresYEntidadesDataMapper.toDonadorDTO(donadorGuardado);
  }

  @Override
  public DonadorDTO buscarDonadorPorID(String donadorID) throws NoSuchElementException {
    Optional<Donador> donadorOptional = this.donadoresRepository.findById(donadorID);

    if (donadorOptional.isEmpty()) {
      throw new DonadorNoEncontradoException("No existe un donador con ese ID");
    }
    Donador donadorFinal = donadorOptional.get();

    return donadoresYEntidadesDataMapper.toDonadorDTO(donadorFinal);
  }

  @Override
  public Boolean puedeDonar(String donadorID) throws NoSuchElementException {
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

    if(estado == null){
      throw new RuntimeException("El estado no puede ser nulo");
    }

    Optional<Donador> donadorOptional = this.donadoresRepository.findById(donadorID);

    if (donadorOptional.isEmpty()) {
      throw new DonadorNoEncontradoException("No existe un donador con ese ID");
    }

    Donador donadorFinal = donadorOptional.get();
    donadorFinal.setEstado(estado);

    this.donadoresRepository.update(donadorFinal);

    return donadoresYEntidadesDataMapper.toDonadorDTO(donadorFinal);
  }

  @Override
  public DonadorDTO modifcarCategoria(String donadorID, String categoria)
      throws NoSuchElementException {

    if(categoria == null){
      throw new RuntimeException("La categoria no puede ser nula");
    }

    Optional<Donador> donadorOptional = this.donadoresRepository.findById(donadorID);
    if (donadorOptional.isEmpty()) {
      throw new DonadorNoEncontradoException("No existe un donador con ese ID");
    }
    Donador donadorFinal = donadorOptional.get();
    donadorFinal.setCategoria(categoria);

    this.donadoresRepository.update(donadorFinal);

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
    if(quejaDTO == null){
      throw new RuntimeException("La queja no puede ser nula");
    }

    if(quejaDTO.id() != null && this.quejasRepository.findById(quejaDTO.id()).isPresent()){
      throw new RuntimeException("Ya existe una queja con ese ID");
    }

    Queja queja = quejasDataMapper.toQueja(quejaDTO);
    Queja quejaGuardado = this.quejasRepository.save(queja);

    // Actualizar estado del donador segun cantidad de quejas
    Optional<Donador> donadorOptional = this.donadoresRepository.findById(quejaDTO.donadorID());
    if (donadorOptional.isPresent()) {
      int totalQuejas = this.quejasRepository.findAllByDonadorId(quejaDTO.donadorID()).size();
      Donador donador = donadorOptional.get();

      if (totalQuejas >= 10) {
        donador.setEstado(EstadoDonadorEnum.BANEADO);
      } else if (totalQuejas >= 5) {
        donador.setEstado(EstadoDonadorEnum.SOSPECHOSO);
      }

      this.donadoresRepository.update(donador);
    }

    return quejasDataMapper.toQuejaDTO(quejaGuardado);
  }

  @Override
  public List<QuejaDTO> obtenerQuejasDe(String donadorID) throws NoSuchElementException {
    // A implementar por el alumno

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
    if(entidadBeneficaDTO == null){
      throw new RuntimeException("La entidad benefica no puede ser nula");
    }

    if(entidadBeneficaDTO.id() != null && this.entidadesBeneficasRepository.findById(entidadBeneficaDTO.id()).isPresent()){
      throw new RuntimeException("Ya existe una entidad benefica con ese ID");
    }

    EntidadBenefica entidadBenefica = entidadesBeneficasDataMapper.toEntidadBenefica(entidadBeneficaDTO);
    EntidadBenefica entidadBeneficaGuardado = this.entidadesBeneficasRepository.save(entidadBenefica);

    return entidadesBeneficasDataMapper.toEntidadBeneficaDTO(entidadBeneficaGuardado);
  }

  @Override
  public EntidadBeneficaDTO buscarEntidadPorID(String entidadID) throws NoSuchElementException {
    // A implementar por el alumno
    Optional<EntidadBenefica> entidadBeneficaOptional = this.entidadesBeneficasRepository.findById(entidadID);
    if (entidadBeneficaOptional.isEmpty()) {
      throw new NoSuchElementException("No existe una entidad benefica con ese ID");
    }
    EntidadBenefica entidadBenefica = entidadBeneficaOptional.get();
    return entidadesBeneficasDataMapper.toEntidadBeneficaDTO(entidadBenefica);
  }

  @Override
  public NecesidadMaterialDTO registrarNecesidad(NecesidadMaterialDTO necesidadMaterialDTO) {
    if(necesidadMaterialDTO == null){
      throw new RuntimeException("La necesidad material no puede ser nula");
    }

    if(necesidadMaterialDTO.id() != null && this.necesidadesMaterialesRepository.findById(necesidadMaterialDTO.id()).isPresent()){
      throw new RuntimeException("Ya existe una necesidad material con ese ID");
    }

    NecesidadMaterial necesidadMaterial = necesidadesMaterialesDataMapper.toNecesidadMaterial(necesidadMaterialDTO);
    NecesidadMaterial necesidadMaterialGuardado = this.necesidadesMaterialesRepository.save(necesidadMaterial);

    return necesidadesMaterialesDataMapper.toNecesidadMaterialDTO(necesidadMaterialGuardado);
  }

  @Override
  public NecesidadMaterialDTO satisfacerNecesidad(String necesidadID, Integer cantidad)
      throws NoSuchElementException {
    if(cantidad <= 0){
      throw new RuntimeException("La cantidad debe ser mayor a 0");
    }

    Optional<NecesidadMaterial> necesidadMaterial = this.necesidadesMaterialesRepository.findById(necesidadID);

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

      necesidad.setFechaUltimaSatisfaccion(java.time.LocalDate.now());
    }

    int cantidadObjetivo = necesidad.getCantidadObjetivo() - cantidad;
    necesidad.setCantidadObjetivo(Math.max(cantidadObjetivo, 0));

    NecesidadMaterial necesidadActualizada = this.necesidadesMaterialesRepository.update(necesidad);

    return necesidadesMaterialesDataMapper.toNecesidadMaterialDTO(necesidadActualizada);
  }

  @Override
  public List<NecesidadMaterialDTO> obtenerNecesidadesInsatisfechasDe(String productoSolicitadoID) {
    return this.necesidadesMaterialesRepository.findAllNecesidadesInsatisfechasByProductId(productoSolicitadoID)
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
    this.fachadaIncentivos = fachadaIncentivos;
  }
}
