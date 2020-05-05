package com.vk.motion.service;

import com.vk.motion.configuration.ApplicationConfig;
import com.vk.motion.configuration.ApplicationConfig.HmacEncoder;
import com.vk.motion.configuration.ApplicationConfig.MessageMode;
import com.vk.motion.entity.MqttMessage;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExternalPathCreator {

  private final HmacEncoder encoder;
  private final StorageService storageService;
  private final ApplicationConfig appConfig;

  @Value("${server.endpoint}")
  private String serverEndpoint;

  // List { "fullUrl:::filename" } -- images
  // List { "fullUrl:::filename:::image" } -- videos
  public List<String> getURLs(String from, String to) {
    return storageService.getFilesBetween(from, to).stream()
        .map(
            filename -> {
              String url = serverEndpoint + filename + "?signature=" + encoder.encode(filename);
              return url + ":::" + filename;
            })
        .sorted()
        .collect(Collectors.toList());
  }

  public String getPath(MqttMessage mqttMessage) {
    return String.format(
        "%s/%s-%s.%s",
        mqttMessage.getDate(),
        mqttMessage.getDate(),
        mqttMessage.getTime(),
        (appConfig.getMessageMode() == MessageMode.VIDEO ? "avi" : "jpg"));
  }

  /*public List<Map<String, String>> getButtonActions() {
    return Stream.of("image", "rich_media")
        .map(
            suffix -> {
              String path = "/settings/modes/" + suffix;
              Map<String, String> action = new HashMap<>();
              action.put("Text", suffix.toUpperCase());
              action.put(
                  "ActionBody", serverEndpoint + path + "?signature=" + encoder.encode(path));
              return action;
            })
        .collect(Collectors.toList());
  }*/
}
