package com.vk.motion.listener;

import com.viber.bot.api.ViberBot;
import com.viber.bot.message.Message;
import com.vk.motion.service.ViberService;
import java.util.LinkedList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViberListener implements ApplicationListener<ApplicationReadyEvent> {

  private final ViberService viberService;

  @Qualifier("viberBot")
  private final ViberBot bot;

  @Value("${viberbot.webhookUrl}")
  private String webhookUrl;

  @Value("${viberbot.echo:false}")
  private boolean botEcho;

  @Override
  public void onApplicationEvent(ApplicationReadyEvent appReadyEvent) {
    try {
      bot.setWebhook(webhookUrl).get();
    } catch (Exception e) {
      log.error("Setting WebHook error - {}", e.toString());
    }
    bot.onMessageReceived(
        (event, message, response) -> {
          List<Message> messages = new LinkedList<>();
          if (botEcho) {
            messages.add(message);
          }
          //          messages.add(viberService.modesKeyboardMessage());
          response.send(messages);
        });
  }
}
