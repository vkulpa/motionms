package com.vk.motion.service;

import static com.vk.motion.configuration.ApplicationConfig.MessageMode.RICH_MEDIA;

import com.google.common.util.concurrent.UncheckedExecutionException;
import com.viber.bot.Response;
import com.viber.bot.message.PictureMessage;
import com.viber.bot.message.RichMediaMessage;
import com.viber.bot.message.RichMediaObject;
import com.viber.bot.message.TextMessage;
import com.viber.bot.message.VideoMessage;
import com.vk.motion.configuration.ApplicationConfig;
import com.vk.motion.entity.MqttMessage;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ViberService {

  @Qualifier("viberResponse")
  private final Response viberResponse;

  private final ExternalPathCreator pathCreator;
  private final ApplicationConfig appConfig;
  private final ExecutorService executorService = Executors.newFixedThreadPool(3);

  public void sendEvents(String from, MqttMessage mqttMessage) {
    String to = pathCreator.getPath(mqttMessage);
    if (appConfig.getMessageMode() == RICH_MEDIA) {
      executorService.execute(() -> sendRichMedia(pathCreator.getURLs(from, to)));
    } else {
      for (String fileInfo : pathCreator.getURLs(from, to)) {
        String[] infoPaths = fileInfo.split(":::");
        switch (appConfig.getMessageMode()) {
          case IMAGE:
            log.info("Send image {}", infoPaths[0]);
            /*viberResponse.send(
            new PictureMessage(infoPaths[0], infoPaths[1], infoPaths[0]),
            modesKeyboardMessage());*/
            viberResponse.send(new PictureMessage(infoPaths[0], infoPaths[1], infoPaths[0]));

            break;
          case VIDEO:
            log.info("Send video {}", infoPaths[0]);
            viberResponse.send(new VideoMessage(infoPaths[0], 0, infoPaths[1], infoPaths[0], 0));
            /*viberResponse.send(
            new VideoMessage(infoPaths[0], 0, infoPaths[1], infoPaths[0], 0),
            modesKeyboardMessage());*/
            break;
        }
      }
    }
  }

  public void sendMqttFailed(ApplicationEvent event) {
    String message = String.format("MQTT failed event: %s", event.toString());
    // viberResponse.send(new TextMessage(message), modesKeyboardMessage());
    viberResponse.send(new TextMessage(message));
  }

  public void sendTextWithKeyboard(String message) {
    sendTextWithKeyboard(message, false);
  }

  public void sendTextWithKeyboard(String message, boolean logMessage) {
    if (logMessage) {
      log.info(message);
    }
    try {
      // viberResponse.send(new TextMessage(message), modesKeyboardMessage());
      viberResponse.send(new TextMessage(message));
    } catch (UncheckedExecutionException exception) {
      if (exception.getMessage().contains("message size")) {
        sendTextWithKeyboard(message.substring(0, message.length() / 2));
        sendTextWithKeyboard(message.substring(message.length() / 2));
      }
    }
  }

  /*public KeyboardMessage modesKeyboardMessage() {
    List<Map<String, Object>> buttons = new LinkedList<>();

    for (Map<String, String> buttonAction : pathCreator.getButtonActions()) {
      Map<String, Object> button = new HashMap<>();
      button.put("Columns", 3);
      button.put("Rows", 1);
      button.put("Text", buttonAction.get("Text"));
      button.put("ActionType", "open-url");
      button.put("ActionBody", buttonAction.get("ActionBody"));
      buttons.add(button);
    }

    Map<String, Object> keyboardMessage = new HashMap<>();
    keyboardMessage.put("Type", "keyboard");
    keyboardMessage.put("BgColor", "#000000");
    keyboardMessage.put("ButtonsGroupColumns", 2);
    keyboardMessage.put("ButtonsGroupRows", 1);
    keyboardMessage.put("InputFieldState", "hidden");
     keyboardMessage.put("Buttons", buttons);

    log.info("Sending keyboard");
    return new KeyboardMessage(new MessageKeyboard(keyboardMessage), null, null);
  }*/

  private void sendRichMedia(List<String> files) {
    List<Map<String, Object>> buttons = new LinkedList<>();

    for (String fileInfo : files) {
      Map<String, Object> button = new HashMap<>();
      String[] infoPaths = fileInfo.split(":::");
      button.put("Columns", 1);
      button.put("Rows", 1);
      button.put("ImageScaleType", "fit");
      button.put("ActionType", "open-url");
      button.put("ActionBody", infoPaths[0]);
      button.put("Image", infoPaths[0]);
      button.put("OpenURLMediaType", "picture");
      buttons.add(button);
    }

    Map<String, Object> richMediaMap = new HashMap<>();
    richMediaMap.put("Type", "rich_media");
    richMediaMap.put("BgColor", "#000000");
    richMediaMap.put("Buttons", buttons);
    RichMediaMessage richMediaMessage =
        new RichMediaMessage(new RichMediaObject(richMediaMap), null, null);

    try {
      log.info("Sending richMedia");
      // viberResponse.send(richMediaMessage, modesKeyboardMessage());
      viberResponse.send(richMediaMessage);
    } catch (UncheckedExecutionException exception) {
      sendTextWithKeyboard("Caught exception in RichMedia. Total files - " + files.size(), true);
      if (exception.getMessage().contains("message size")) {
        sendTextWithKeyboard("Trying to split", true);
        sendRichMedia(files.subList(0, files.size() / 2));
        sendTextWithKeyboard("Sending second half", true);
        sendRichMedia(files.subList(files.size() / 2, files.size()));
      }
    }
  }
}
