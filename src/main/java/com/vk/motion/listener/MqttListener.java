package com.vk.motion.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vk.motion.entity.MqttMessage;
import com.vk.motion.service.ExternalPathCreator;
import com.vk.motion.service.ViberService;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.mqtt.event.MqttConnectionFailedEvent;
import org.springframework.integration.mqtt.event.MqttIntegrationEvent;
import org.springframework.integration.mqtt.event.MqttSubscribedEvent;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MqttListener implements ApplicationListener<MqttIntegrationEvent> {

  private ObjectMapper om = new ObjectMapper();
  private AtomicBoolean sendFailedNotification = new AtomicBoolean(true);
  private AtomicReference<String> eventStartedFlag = new AtomicReference<>("");
  private AtomicReference<Integer> eventStartedTime = new AtomicReference<>(0);

  private final ViberService viberService;
  private final ExternalPathCreator pathCreator;
  private final MqttPahoMessageDrivenChannelAdapter mqttAdapter;

  @Value("${motion.events.ignore-less-that}")
  private Integer ignoreEventsSeconds;

  @Bean
  public IntegrationFlow mqttInbound() {
    return IntegrationFlows.from(mqttAdapter).handle(this::handleMessage).get();
  }

  @Override
  public void onApplicationEvent(MqttIntegrationEvent event) {
    if (event instanceof MqttConnectionFailedEvent) {
      if (sendFailedNotification.getAndSet(false)) {
        viberService.sendMqttFailed(event);
      }
      log.error(event.toString());
    } else if (event instanceof MqttSubscribedEvent) {
      sendFailedNotification.set(true);
    }
  }

  private void handleMessage(Message<?> message) {
    try {
      String mqttTopic =
          Objects.requireNonNull(message.getHeaders().get("mqtt_receivedTopic")).toString();
      MqttMessage mqttMessage = om.readValue(message.getPayload().toString(), MqttMessage.class);
      if (mqttTopic.contains("event/start")) {
        eventStartedFlag.compareAndSet("", pathCreator.getPath(mqttMessage));
        eventStartedTime.compareAndSet(0, mqttMessage.getTimestamp());
      } else if (mqttTopic.contains("event/end")) {
        log.info(
            "Distance between start/end - {} seconds. Threshold is {}",
            mqttMessage.getTimestamp() - eventStartedTime.get(),
            ignoreEventsSeconds);
        if (eventStartedTime.get() > 0
            && ignoreEventsSeconds <= mqttMessage.getTimestamp() - eventStartedTime.get()) {
          viberService.sendEvents(eventStartedFlag.get(), mqttMessage);
        }

        eventStartedFlag.set("");
        eventStartedTime.set(0);
      }
    } catch (IOException e) {
      log.error("Exception", e);
    }

    log.trace(message.toString());
  }
}
