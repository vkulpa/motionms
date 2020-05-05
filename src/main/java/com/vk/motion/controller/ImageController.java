package com.vk.motion.controller;

import com.vk.motion.configuration.ApplicationConfig.MessageMode;
import com.vk.motion.service.StorageService;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ImageController {

  private final StorageService service;

  @GetMapping(value = "/images/{dirName}/{fileName}", produces = MediaType.IMAGE_JPEG_VALUE)
  public byte[] getImage(@PathVariable String dirName, @PathVariable String fileName)
      throws IOException {
    return new InputStreamResource(
            service.getFileInputStream(dirName + "/" + fileName, MessageMode.IMAGE))
        .getInputStream()
        .readAllBytes();
  }
}
