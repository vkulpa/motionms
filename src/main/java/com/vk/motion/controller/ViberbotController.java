package com.vk.motion.controller;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.CharStreams;
import com.viber.bot.Request;
import com.viber.bot.ViberSignatureValidator;
import com.viber.bot.api.ViberBot;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ViberbotController {

  @Qualifier("viberBot")
  private final ViberBot bot;

  @Qualifier("viberSignatureValidator")
  private final ViberSignatureValidator signatureValidator;

  @PostMapping(value = "/bot", produces = "application/json")
  public String incoming(
      @RequestBody String json,
      @RequestHeader("X-Viber-Content-Signature") String serverSideSignature)
      throws ExecutionException, InterruptedException, IOException {
    Preconditions.checkState(
        signatureValidator.isSignatureValid(serverSideSignature, json), "invalid signature");
    @Nullable InputStream response = bot.incoming(Request.fromJsonString(json)).get();
    return response != null
        ? CharStreams.toString(new InputStreamReader(response, Charsets.UTF_16))
        : null;
  }
}
