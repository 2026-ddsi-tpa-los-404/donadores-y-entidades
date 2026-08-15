package ar.edu.utn.dds.k3003.telegram;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
@ConditionalOnBean(DonaTrackBot.class)
public class BotConfig {

  private static final Logger log = LoggerFactory.getLogger(BotConfig.class);

  private final DonaTrackBot bot;

  public BotConfig(DonaTrackBot bot) {
    this.bot = bot;
  }

  @PostConstruct
  public void registerBot() {
    try {
      TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
      botsApi.registerBot(bot);
      log.info("Bot de Telegram registrado exitosamente: {}", bot.getBotUsername());
    } catch (TelegramApiException e) {
      log.error("Error al registrar el bot de Telegram: {}", e.getMessage());
    }
  }
}
