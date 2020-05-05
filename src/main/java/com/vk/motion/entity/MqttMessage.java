package com.vk.motion.entity;

import lombok.Data;

@Data
public class MqttMessage {
  private String date;
  private String time;
  private Integer timestamp;
  private String message;
}
