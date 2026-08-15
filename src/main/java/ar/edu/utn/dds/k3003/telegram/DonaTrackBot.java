package ar.edu.utn.dds.k3003.telegram;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(name = "telegram.bot.token", matchIfMissing = false)
public class DonaTrackBot extends TelegramLongPollingBot {

  private static final Logger log = LoggerFactory.getLogger(DonaTrackBot.class);

  private final String botUsername;
  private final Fachada fachada;
  private final Map<Long, ConversationState> conversations = new ConcurrentHashMap<>();

  public DonaTrackBot(
      @Value("${telegram.bot.token}") String botToken,
      @Value("${telegram.bot.username}") String botUsername,
      Fachada fachada) {
    super(botToken);
    this.botUsername = botUsername;
    this.fachada = fachada;
  }

  @Override
  public String getBotUsername() {
    return botUsername;
  }

  @Override
  public void onUpdateReceived(Update update) {
    if (!update.hasMessage() || !update.getMessage().hasText()) {
      return;
    }

    Long chatId = update.getMessage().getChatId();
    String texto = update.getMessage().getText().trim();

    try {
      if (texto.equals("/start") || texto.equals("/menu")) {
        conversations.remove(chatId);
        enviar(chatId, menuPrincipal());
        return;
      }

      ConversationState state = conversations.get(chatId);

      if (state == null) {
        procesarMenuPrincipal(chatId, texto);
      } else {
        procesarConversacion(chatId, texto, state);
      }
    } catch (Exception e) {
      log.error("Error procesando mensaje del chat {}: {}", chatId, e.getMessage());
      enviar(chatId, "❌ Error: " + e.getMessage() + "\nEnviá /menu para volver al menú.");
    }
  }

  private void procesarMenuPrincipal(Long chatId, String texto) {
    switch (texto) {
      case "1" -> {
        conversations.put(chatId, new ConversationState("MENU_DONADOR", new HashMap<>()));
        enviar(chatId, menuDonador());
      }
      case "2" -> {
        conversations.put(chatId, new ConversationState("MENU_ADMIN", new HashMap<>()));
        enviar(chatId, menuAdmin());
      }
      default -> enviar(chatId, "Opción no válida.\n" + menuPrincipal());
    }
  }

  private void procesarConversacion(Long chatId, String texto, ConversationState state) {
    String step = state.step();
    Map<String, String> data = state.data();

    switch (step) {
      // --- Menú Donador ---
      case "MENU_DONADOR" -> procesarMenuDonador(chatId, texto);

      // Registrar Donador
      case "DONADOR_NOMBRE" -> {
        data.put("nombre", texto);
        conversations.put(chatId, new ConversationState("DONADOR_APELLIDO", data));
        enviar(chatId, "Apellido:");
      }
      case "DONADOR_APELLIDO" -> {
        data.put("apellido", texto);
        conversations.put(chatId, new ConversationState("DONADOR_EDAD", data));
        enviar(chatId, "Edad:");
      }
      case "DONADOR_EDAD" -> {
        data.put("edad", texto);
        conversations.put(chatId, new ConversationState("DONADOR_EMAIL", data));
        enviar(chatId, "Email:");
      }
      case "DONADOR_EMAIL" -> {
        data.put("email", texto);
        conversations.put(chatId, new ConversationState("DONADOR_DOCUMENTO", data));
        enviar(chatId, "Nro de documento:");
      }
      case "DONADOR_DOCUMENTO" -> {
        data.put("documento", texto);
        conversations.put(chatId, new ConversationState("DONADOR_DOMICILIO", data));
        enviar(chatId, "Domicilio:");
      }
      case "DONADOR_DOMICILIO" -> {
        data.put("domicilio", texto);
        DonadorDTO dto = new DonadorDTO(null, data.get("nombre"), data.get("apellido"),
            Integer.parseInt(data.get("edad")), data.get("email"),
            data.get("documento"), texto, null, null);
        DonadorDTO creado = fachada.agregarDonador(dto);
        enviar(chatId, "✅ Donador registrado con ID: " + creado.id());
        conversations.remove(chatId);
      }

      // Consultar estadísticas
      case "DONADOR_STATS_ID" -> {
        DonadorStatsDTO stats = fachada.estadisticasDonador(texto);
        enviar(chatId, formatStats(stats));
        conversations.remove(chatId);
      }

      // Consultar donador por ID
      case "DONADOR_BUSCAR_ID" -> {
        DonadorDTO donador = fachada.buscarDonadorPorID(texto);
        enviar(chatId, formatDonador(donador));
        conversations.remove(chatId);
      }

      // --- Menú Admin ---
      case "MENU_ADMIN" -> procesarMenuAdmin(chatId, texto);

      // Crear entidad
      case "ENTIDAD_RAZON_SOCIAL" -> {
        data.put("razonSocial", texto);
        conversations.put(chatId, new ConversationState("ENTIDAD_DOMICILIO", data));
        enviar(chatId, "Domicilio:");
      }
      case "ENTIDAD_DOMICILIO" -> {
        data.put("domicilio", texto);
        conversations.put(chatId, new ConversationState("ENTIDAD_TELEFONO", data));
        enviar(chatId, "Teléfono:");
      }
      case "ENTIDAD_TELEFONO" -> {
        data.put("telefono", texto);
        conversations.put(chatId, new ConversationState("ENTIDAD_CORREO", data));
        enviar(chatId, "Correo electrónico:");
      }
      case "ENTIDAD_CORREO" -> {
        EntidadBeneficaDTO dto = new EntidadBeneficaDTO(null,
            data.get("razonSocial"), data.get("domicilio"), data.get("telefono"), texto);
        EntidadBeneficaDTO creada = fachada.agregarEntidad(dto);
        enviar(chatId, "✅ Entidad creada con ID: " + creada.id());
        conversations.remove(chatId);
      }

      // Editar entidad
      case "ENTIDAD_EDITAR_ID" -> {
        data.put("entidadId", texto);
        conversations.put(chatId, new ConversationState("ENTIDAD_EDITAR_CAMPO", data));
        enviar(chatId, "¿Qué campo desea editar?\n1. Razón Social\n2. Domicilio\n3. Teléfono\n4. Correo");
      }
      case "ENTIDAD_EDITAR_CAMPO" -> {
        data.put("campo", texto);
        conversations.put(chatId, new ConversationState("ENTIDAD_EDITAR_VALOR", data));
        enviar(chatId, "Nuevo valor:");
      }
      case "ENTIDAD_EDITAR_VALOR" -> {
        String entidadId = data.get("entidadId");
        String campo = data.get("campo");
        EntidadBeneficaDTO actual = fachada.buscarEntidadPorID(entidadId);
        EntidadBeneficaDTO actualizado = switch (campo) {
          case "1" -> new EntidadBeneficaDTO(entidadId, texto, actual.domicilio(), actual.telefono(), actual.correo());
          case "2" -> new EntidadBeneficaDTO(entidadId, actual.razonSocial(), texto, actual.telefono(), actual.correo());
          case "3" -> new EntidadBeneficaDTO(entidadId, actual.razonSocial(), actual.domicilio(), texto, actual.correo());
          case "4" -> new EntidadBeneficaDTO(entidadId, actual.razonSocial(), actual.domicilio(), actual.telefono(), texto);
          default -> throw new RuntimeException("Opción no válida");
        };
        fachada.actualizarEntidad(actualizado);
        enviar(chatId, "✅ Entidad actualizada correctamente.");
        conversations.remove(chatId);
      }

      // Consultar entidad por ID
      case "ENTIDAD_BUSCAR_ID" -> {
        EntidadBeneficaDTO entidad = fachada.buscarEntidadPorID(texto);
        enviar(chatId, formatEntidad(entidad));
        conversations.remove(chatId);
      }

      // Alta de necesidad
      case "NECESIDAD_ENTIDAD_ID" -> {
        data.put("entidadId", texto);
        conversations.put(chatId, new ConversationState("NECESIDAD_DESCRIPCION", data));
        enviar(chatId, "Descripción de la necesidad:");
      }
      case "NECESIDAD_DESCRIPCION" -> {
        data.put("descripcion", texto);
        conversations.put(chatId, new ConversationState("NECESIDAD_CANTIDAD", data));
        enviar(chatId, "Cantidad objetivo:");
      }
      case "NECESIDAD_CANTIDAD" -> {
        data.put("cantidad", texto);
        conversations.put(chatId, new ConversationState("NECESIDAD_PRODUCTO_ID", data));
        enviar(chatId, "ID del producto solicitado:");
      }
      case "NECESIDAD_PRODUCTO_ID" -> {
        data.put("productoId", texto);
        conversations.put(chatId, new ConversationState("NECESIDAD_URGENCIA", data));
        enviar(chatId, "Nivel de urgencia (número):");
      }
      case "NECESIDAD_URGENCIA" -> {
        data.put("urgencia", texto);
        conversations.put(chatId, new ConversationState("NECESIDAD_TIPO", data));
        enviar(chatId, "Tipo:\n1. EXTRAORDINARIA\n2. RECURRENTE");
      }
      case "NECESIDAD_TIPO" -> {
        TipoNecesidadMaterialEnum tipo = texto.equals("2")
            ? TipoNecesidadMaterialEnum.RECURRENTE
            : TipoNecesidadMaterialEnum.EXTRAORDINARIA;
        NecesidadMaterialDTO dto = new NecesidadMaterialDTO(null,
            data.get("entidadId"),
            Integer.parseInt(data.get("urgencia")),
            data.get("descripcion"),
            Integer.parseInt(data.get("cantidad")),
            data.get("productoId"),
            tipo);
        NecesidadMaterialDTO creada = fachada.registrarNecesidad(dto);
        enviar(chatId, "✅ Necesidad registrada con ID: " + creada.id()
            + "\nCantidad pendiente: " + creada.cantidadObjetivo());
        conversations.remove(chatId);
      }

      // Borrar necesidad
      case "NECESIDAD_BORRAR_ID" -> {
        fachada.borrarNecesidad(texto);
        enviar(chatId, "✅ Necesidad eliminada.");
        conversations.remove(chatId);
      }

      // Modificar necesidad
      case "NECESIDAD_MODIFICAR_ID" -> {
        data.put("necesidadId", texto);
        conversations.put(chatId, new ConversationState("NECESIDAD_MODIFICAR_CANTIDAD", data));
        enviar(chatId, "Nueva cantidad objetivo:");
      }
      case "NECESIDAD_MODIFICAR_CANTIDAD" -> {
        NecesidadMaterialDTO dtoMod = new NecesidadMaterialDTO(
            data.get("necesidadId"), null, null, null,
            Integer.parseInt(texto), null, null);
        fachada.actualizarNecesidad(dtoMod);
        enviar(chatId, "✅ Necesidad actualizada.");
        conversations.remove(chatId);
      }

      // Consultar necesidad por ID
      case "NECESIDAD_BUSCAR_ID" -> {
        NecesidadMaterialDTO necesidad = fachada.buscarNecesidadPorID(texto);
        enviar(chatId, formatNecesidad(necesidad));
        conversations.remove(chatId);
      }

      default -> {
        conversations.remove(chatId);
        enviar(chatId, "Estado no reconocido. Enviá /menu para reiniciar.");
      }
    }
  }

  private void procesarMenuDonador(Long chatId, String texto) {
    switch (texto) {
      case "1" -> {
        conversations.put(chatId, new ConversationState("DONADOR_NOMBRE", new HashMap<>()));
        enviar(chatId, "Nombre:");
      }
      case "2" -> {
        conversations.put(chatId, new ConversationState("DONADOR_STATS_ID", new HashMap<>()));
        enviar(chatId, "ID del donador:");
      }
      case "3" -> {
        conversations.put(chatId, new ConversationState("DONADOR_BUSCAR_ID", new HashMap<>()));
        enviar(chatId, "ID del donador:");
      }
      case "4" -> {
        List<DonadorDTO> donadores = fachada.listarDonadores();
        if (donadores.isEmpty()) {
          enviar(chatId, "No hay donadores registrados.");
        } else {
          StringBuilder sb = new StringBuilder("📋 Donadores:\n\n");
          donadores.forEach(d -> sb.append("• ID: ").append(d.id())
              .append(" | ").append(d.nombre()).append(" ").append(d.apellido())
              .append(" | ").append(d.estado()).append("\n"));
          enviar(chatId, sb.toString());
        }
        conversations.remove(chatId);
      }
      default -> enviar(chatId, "Opción no válida.\n" + menuDonador());
    }
  }

  private void procesarMenuAdmin(Long chatId, String texto) {
    switch (texto) {
      case "1" -> {
        conversations.put(chatId, new ConversationState("ENTIDAD_RAZON_SOCIAL", new HashMap<>()));
        enviar(chatId, "Razón social:");
      }
      case "2" -> {
        conversations.put(chatId, new ConversationState("ENTIDAD_EDITAR_ID", new HashMap<>()));
        enviar(chatId, "ID de la entidad a editar:");
      }
      case "3" -> {
        conversations.put(chatId, new ConversationState("ENTIDAD_BUSCAR_ID", new HashMap<>()));
        enviar(chatId, "ID de la entidad:");
      }
      case "4" -> {
        List<EntidadBeneficaDTO> entidades = fachada.listarEntidades();
        if (entidades.isEmpty()) {
          enviar(chatId, "No hay entidades registradas.");
        } else {
          StringBuilder sb = new StringBuilder("📋 Entidades:\n\n");
          entidades.forEach(e -> sb.append("• ID: ").append(e.id())
              .append(" | ").append(e.razonSocial()).append("\n"));
          enviar(chatId, sb.toString());
        }
        conversations.remove(chatId);
      }
      case "5" -> {
        conversations.put(chatId, new ConversationState("NECESIDAD_ENTIDAD_ID", new HashMap<>()));
        enviar(chatId, "ID de la entidad benefica:");
      }
      case "6" -> {
        conversations.put(chatId, new ConversationState("NECESIDAD_BORRAR_ID", new HashMap<>()));
        enviar(chatId, "ID de la necesidad a borrar:");
      }
      case "7" -> {
        conversations.put(chatId, new ConversationState("NECESIDAD_MODIFICAR_ID", new HashMap<>()));
        enviar(chatId, "ID de la necesidad a modificar:");
      }
      case "8" -> {
        conversations.put(chatId, new ConversationState("NECESIDAD_BUSCAR_ID", new HashMap<>()));
        enviar(chatId, "ID de la necesidad:");
      }
      default -> enviar(chatId, "Opción no válida.\n" + menuAdmin());
    }
  }

  // --- Helpers de formato ---

  private String menuPrincipal() {
    return """
        🤖 *DonaTrack Bot*
        
        ¿Qué tipo de usuario sos?
        1. Donador
        2. Admin
        
        Enviá el número de la opción.""";
  }

  private String menuDonador() {
    return """
        👤 *Menú Donador*
        
        1. Registrarse
        2. Consultar estadísticas
        3. Consultar donador por ID
        4. Consultar todos los donadores
        
        Enviá el número de la opción o /menu para volver.""";
  }

  private String menuAdmin() {
    return """
        🔧 *Menú Admin*
        
        1. Crear entidad
        2. Editar entidad
        3. Consultar entidad por ID
        4. Consultar todas las entidades
        5. Alta de necesidad
        6. Borrar necesidad por ID
        7. Modificar necesidad por ID
        8. Consultar necesidad por ID
        
        Enviá el número de la opción o /menu para volver.""";
  }

  private String formatDonador(DonadorDTO d) {
    return String.format("""
        👤 *Donador*
        ID: %s
        Nombre: %s %s
        Edad: %d
        Email: %s
        Documento: %s
        Domicilio: %s
        Estado: %s
        Categoría: %s""",
        d.id(), d.nombre(), d.apellido(), d.edad(),
        d.email(), d.nroDocumento(), d.domicilio(),
        d.estado(), d.categoria());
  }

  private String formatStats(DonadorStatsDTO s) {
    return String.format("""
        📊 *Estadísticas*
        ID: %s
        Nombre: %s %s
        Estado: %s
        Categoría: %s
        Misión actual: %s
        Insignias: %s""",
        s.id(), s.nombre(), s.apellido(),
        s.estado(), s.categoria(),
        s.misionActualID() != null ? s.misionActualID() : "Ninguna",
        s.insigniasID() != null && !s.insigniasID().isEmpty() ? String.join(", ", s.insigniasID()) : "Ninguna");
  }

  private String formatEntidad(EntidadBeneficaDTO e) {
    return String.format("""
        🏢 *Entidad Benéfica*
        ID: %s
        Razón Social: %s
        Domicilio: %s
        Teléfono: %s
        Correo: %s""",
        e.id(), e.razonSocial(), e.domicilio(), e.telefono(), e.correo());
  }

  private String formatNecesidad(NecesidadMaterialDTO n) {
    return String.format("""
        📦 *Necesidad Material*
        ID: %s
        Entidad ID: %s
        Descripción: %s
        Cantidad objetivo: %d
        Producto solicitado: %s
        Urgencia: %d
        Tipo: %s""",
        n.id(), n.entidadID(), n.descripcion(),
        n.cantidadObjetivo(), n.productoSolicitadoID(),
        n.nivelDeUrgencia(), n.tipo());
  }

  private void enviar(Long chatId, String texto) {
    SendMessage msg = new SendMessage();
    msg.setChatId(chatId.toString());
    msg.setText(texto);
    try {
      execute(msg);
    } catch (TelegramApiException e) {
      log.error("Error enviando mensaje a chat {}: {}", chatId, e.getMessage());
    }
  }
}
