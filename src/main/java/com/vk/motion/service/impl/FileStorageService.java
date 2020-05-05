package com.vk.motion.service.impl;

import com.vk.motion.configuration.ApplicationConfig;
import com.vk.motion.configuration.ApplicationConfig.MessageMode;
import com.vk.motion.service.StorageService;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class FileStorageService implements StorageService {

  private final String baseImagePath;
  private final String baseVideoPath;
  private final ApplicationConfig appConfig;

  @Override
  public InputStream getFileInputStream(String fileName, MessageMode mode) {
    try {
      return new DataInputStream(new FileInputStream(basePath(mode) + fileName));
    } catch (Exception e) {
      log.error("Couldn't get inputStream {}", e.toString());
      return new InputStream() {
        @Override
        public int read() {
          return -1;
        }
      };
    }
  }

  @Override
  public List<String> getFilesBetween(String from, String to) {
    List<String> fileNames = new ArrayList<>();
    final String basePath = basePath(appConfig.getMessageMode());
    final String fullFrom = basePath + from;
    final String fullTo = basePath + to;

    try (Stream<Path> pathStream =
        Files.walk(Paths.get(basePath), 2, FileVisitOption.FOLLOW_LINKS)) {
      fileNames.addAll(
          pathStream
              .filter(
                  entry ->
                      entry.toString().compareTo(fullFrom) >= 0
                          && entry.toString().compareTo(fullTo) <= 0)
              .map(entry -> entry.toString().replaceAll(basePath, filesSuffix()))
              .collect(Collectors.toList()));
    } catch (Exception e) {
      log.error("Error getting directory listing {}", e.toString());
    }
    log.info(
        "Found {} {} files", fileNames.size(), appConfig.getMessageMode().toString().toLowerCase());
    return fileNames;
  }

  private String basePath(MessageMode mode) {
    return MessageMode.VIDEO == mode ? baseVideoPath : baseImagePath;
  }

  private String filesSuffix() {
    return MessageMode.VIDEO == appConfig.getMessageMode() ? "/videos/" : "/images/";
  }
}
