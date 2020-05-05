package com.vk.motion.configuration;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;

@Configuration
@Slf4j
public class MqttConfiguration {

  @Bean
  public MqttPahoClientFactory mqttClientFactory(
      @Value("${mqtt.transport}${mqtt.host}:${mqtt.port}") String mqttAddress,
      @Value("${mqtt.connection.timeout:10}") int timeOut) {
    DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
    MqttConnectOptions options = new MqttConnectOptions();
    options.setServerURIs(new String[] {mqttAddress});
    options.setConnectionTimeout(timeOut);
    factory.setConnectionOptions(options);
    return factory;
  }

  @Bean
  @Autowired
  public MqttPahoMessageDrivenChannelAdapter mqttAdapter(
      MqttPahoClientFactory factory,
      @Value("${mqtt.topics.detection}") String detectionTopic,
      @Value("${mqtt.topics.event}") String eventTopic,
      @Value("${mqtt.topics.camera}") String cameraTopic) {
    return new MqttPahoMessageDrivenChannelAdapter(
        UUID.randomUUID().toString(), factory, detectionTopic, eventTopic, cameraTopic);
  }
}
