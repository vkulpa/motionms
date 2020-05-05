package com.vk.motion.configuration;

import com.vk.motion.service.StorageService;
import com.vk.motion.service.impl.FileStorageService;
import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.codec.Hex;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class ApplicationConfig {

  @Value("${storage.basePath}${storage.images}/")
  private String baseImagePath;

  @Value("${storage.basePath}${storage.videos}/")
  private String baseVideoPath;

  private final ViberCredentialConfig viberCredentialConfig;

  @Getter @Setter private MessageMode messageMode = MessageMode.RICH_MEDIA;

  @Bean
  public StorageService getStorage() {
    return new FileStorageService(baseImagePath, baseVideoPath, this);
  }

  @Bean
  public HmacEncoder encodeValue() {
    return new HmacEncoder();
  }

  public class HmacEncoder {
    public String encode(String data) {
      try {
        String macType = "HmacSHA256";
        Mac mac = Mac.getInstance(macType);
        mac.init(
            new SecretKeySpec(
                (viberCredentialConfig.getToken() + viberCredentialConfig.getAdminProfileId())
                    .getBytes(StandardCharsets.UTF_8),
                macType));
        return String.copyValueOf(Hex.encode(mac.doFinal(data.getBytes(StandardCharsets.UTF_8))));
      } catch (Exception e) {
        log.error("Failed create HMAC with {}", e.toString());
      }
      return "";
    }
  }

  public enum MessageMode {
    IMAGE,
    VIDEO,
    RICH_MEDIA
  }
}
