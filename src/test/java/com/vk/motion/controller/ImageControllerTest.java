package com.vk.motion.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.vk.motion.configuration.ApplicationConfig.MessageMode;
import com.vk.motion.service.StorageService;
import com.vk.motion.testconfigs.TestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestConfig.class, ImageController.class})
@AutoConfigureMockMvc(addFilters = false)
public class ImageControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private StorageService service;

  @Test
  public void getImage() throws Exception {

    when(service.getFileInputStream(eq("day/file"), any(MessageMode.class)))
        .thenReturn(getClass().getClassLoader().getResourceAsStream("mock-img.jpg"));
    mockMvc.perform(get("/images/day/file.jpg")).andExpect(status().isOk());
  }
}
