package com.vk.motion.controller;

import com.vk.motion.configuration.ApplicationConfig.MessageMode;
import com.vk.motion.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class VideoController {

  private final StorageService service;

  @GetMapping(value = "/videos/{dirName}/{fileName}", produces = "video/mp4")
  public ResponseEntity<InputStreamResource> getImage(
      @PathVariable String dirName, @PathVariable String fileName) {
    return ResponseEntity.ok(
        new InputStreamResource(
            service.getFileInputStream(dirName + "/" + fileName, MessageMode.VIDEO)));
  }
}
