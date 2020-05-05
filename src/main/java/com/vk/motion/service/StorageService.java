package com.vk.motion.service;

import com.vk.motion.configuration.ApplicationConfig.MessageMode;
import java.io.InputStream;
import java.util.List;

public interface StorageService {

  InputStream getFileInputStream(String fileName, MessageMode mode);

  List<String> getFilesBetween(String from, String to);
}
