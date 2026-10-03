package ar.edu.utn.dds.k3003.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.AppenderBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Appender de Logback minimo para mandar logs a Datadog Logs, usando la cuenta
 * de logs del grupo (API key propia, variable DATADOG_LOGS_API_KEY), separada
 * de la cuenta de metricas.
 *
 * <p>Pensado para envolverse en un {@code AsyncAppender} en logback-spring.xml:
 * este appender hace un POST HTTP sincrono por evento, y solo se activa si
 * hay una API key configurada (ver logback-spring.xml), igual que el appender
 * de Betterstack del repo de referencia de la catedra.
 */
public class DatadogLogsAppender extends AppenderBase<ILoggingEvent> {

  private String apiKey;
  private String site = "us5.datadoghq.com";
  private String service = "donadores-y-entidades";
  private String env = "local";

  private HttpClient httpClient;
  private ObjectMapper objectMapper;
  private URI intakeUri;

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  public void setSite(String site) {
    this.site = site;
  }

  public void setService(String service) {
    this.service = service;
  }

  public void setEnv(String env) {
    this.env = env;
  }

  @Override
  public void start() {
    if (apiKey == null || apiKey.isBlank()) {
      addError("DatadogLogsAppender sin apiKey configurada, no se inicia.");
      return;
    }
    this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    this.objectMapper = new ObjectMapper();
    this.intakeUri = URI.create("https://http-intake.logs." + site + "/api/v2/logs");
    super.start();
  }

  @Override
  protected void append(ILoggingEvent event) {
    try {
      Map<String, Object> body = new LinkedHashMap<>();
      body.put("ddsource", "java");
      body.put("ddtags", "env:" + env + ",application:" + service);
      body.put("service", service);
      body.put("hostname", event.getMDCPropertyMap().getOrDefault("instanceId", "unknown"));
      body.put("message", buildMessage(event));
      body.put("level", event.getLevel().toString());
      body.put("logger", event.getLoggerName());
      body.putAll(event.getMDCPropertyMap());

      String json = objectMapper.writeValueAsString(body);
      HttpRequest request =
          HttpRequest.newBuilder(intakeUri)
              .timeout(Duration.ofSeconds(5))
              .header("Content-Type", "application/json")
              .header("DD-API-KEY", apiKey)
              .POST(BodyPublishers.ofString(json))
              .build();

      httpClient.send(request, BodyHandlers.discarding());
    } catch (Exception e) {
      addError("No se pudo enviar el log a Datadog: " + e.getMessage());
    }
  }

  private String buildMessage(ILoggingEvent event) {
    String message = event.getFormattedMessage();
    IThrowableProxy throwableProxy = event.getThrowableProxy();
    if (throwableProxy != null) {
      return message + "\n" + ThrowableProxyUtil.asString(throwableProxy);
    }
    return message;
  }
}
