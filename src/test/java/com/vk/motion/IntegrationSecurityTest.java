package com.vk.motion;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.viber.bot.api.ViberBot;
import com.vk.motion.configuration.ApplicationConfig;
import com.vk.motion.configuration.MqttConfiguration;
import com.vk.motion.configuration.SecurityConfig;
import com.vk.motion.configuration.ViberBotConfiguration;
import com.vk.motion.configuration.ViberCredentialConfig;
import com.vk.motion.service.ViberService;
import com.vk.motion.testconfigs.TestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@SpringBootTest(
    classes = {
      TestConfig.class,
      ApplicationConfig.class,
      MqttConfiguration.class,
      SecurityConfig.class,
      ViberBotConfiguration.class,
      ViberCredentialConfig.class
    })
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class IntegrationSecurityTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private MqttPahoClientFactory clientFactory;
  @MockBean private MqttPahoMessageDrivenChannelAdapter mqttAdapter;
  @MockBean private ViberBot bot;
  @MockBean private ViberService viberService;

  @Test
  public void shouldPassAuthOnPublicEndpoint() throws Exception {
    // controllers aren't mocked so expect HTTP 404 response
    mockMvc.perform((get("/error"))).andExpect(status().isNotFound());
    mockMvc.perform((get("/bot"))).andExpect(status().isNotFound());
    mockMvc.perform((get("/health"))).andExpect(status().isNotFound());
    mockMvc.perform((get("/favicon.ico"))).andExpect(status().isNotFound());
  }

  @Test
  public void shouldTriggerUnauthorized() throws Exception {
    mockMvc.perform(get("/v1/api")).andExpect(status().isUnauthorized());
    verify(viberService).sendTextWithKeyboard(anyString());
  }

  @Test
  public void shouldPassAuthorizationWithSignature() throws Exception {
    mockMvc
        .perform(
            get(
                "/v1/api/no-api?signature=0a7a03ca557576db4c0d2d2530e05418afc817f626416bba34c259fb958ac542"))
        .andExpect(status().isNotFound());
  }
}
