package com.vk.motion.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ListenableFuture;
import com.viber.bot.Response;
import com.viber.bot.ViberSignatureValidator;
import com.viber.bot.api.MessageDestination;
import com.viber.bot.api.ViberBot;
import com.viber.bot.message.Message;
import com.viber.bot.profile.BotProfile;
import com.viber.bot.profile.UserProfile;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("viberbot")
@RequiredArgsConstructor
public class ViberBotConfiguration {

  private final ViberCredentialConfig viberCredentialConfig;

  @Getter @Setter private Map<String, String> adminProfile;

  @Value("${viberbot.enabled}")
  private Boolean viberBotEnabled;

  @Bean("viberBot")
  public ViberBot viberBot(
      @Value("${viberbot.name}") String name, @Value("${viberbot.avatar}") String avatar) {
    BotProfile profile = new BotProfile(name, avatar);
    return new ViberBot(profile, viberCredentialConfig.getToken());
  }

  @Bean("viberSignatureValidator")
  public ViberSignatureValidator signatureValidator() {
    return new ViberSignatureValidator(viberCredentialConfig.getToken());
  }

  @Bean("adminProfile")
  public UserProfile adminProfile() throws IOException {
    ObjectMapper om = new ObjectMapper();
    adminProfile.put("id", viberCredentialConfig.getAdminProfileId());
    return om.readValue(om.writeValueAsBytes(adminProfile), UserProfile.class);
  }

  @Bean("viberResponse")
  public Response viberResponse(
      @Qualifier("adminProfile") UserProfile userProfile,
      @Qualifier("viberBot") ViberBot viberBot) {
    if (viberBotEnabled) {
      return new Response(new MessageDestination(userProfile), viberBot);
    } else {
      class MockResponse extends Response {

        public MockResponse(MessageDestination md, ViberBot vb) {
          super(md, vb);
        }

        public ListenableFuture<Collection<String>> send(Message... messages) {
          return null;
        }
      }
      return new MockResponse(new MessageDestination(userProfile), viberBot);
    }
  }
}
