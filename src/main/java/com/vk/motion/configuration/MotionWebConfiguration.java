package com.vk.motion.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("motionms.web")
@Data
public class MotionWebConfiguration {

  private String username;
  private String password;
}
